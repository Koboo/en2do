package eu.koboo.en2do;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.annotation.Id;
import eu.koboo.en2do.annotation.Repository;
import eu.koboo.en2do.exception.*;
import eu.koboo.en2do.utility.MethodNameUtil;
import eu.koboo.en2do.utility.GenericUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.*;
import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RepoFactory {

    MongoManager manager;
    Map<Class<?>, Repo<?, ?>> repoRegistry;

    public RepoFactory(MongoManager manager) {
        this.manager = manager;
        this.repoRegistry = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    protected  <E, ID, R extends Repo<E, ID>> R create(Class<R> repoClass) throws Exception {
        if (repoRegistry.containsKey(repoClass)) {
            return (R) repoRegistry.get(repoClass);
        }

        // Parse Entity and UniqueId classes
        Type[] repoGenericTypeArray = repoClass.getGenericInterfaces();
        Type repoGenericTypeParams = null;
        for (Type type : repoGenericTypeArray) {
            if (type.getTypeName().split("<")[0].equalsIgnoreCase(Repo.class.getName())) {
                repoGenericTypeParams = type;
                break;
            }
        }
        if (repoGenericTypeParams == null) {
            throw new InvalidTypeParameterException(repoClass);
        }
        String entityClassName = repoGenericTypeParams.getTypeName().split("<")[1].split(",")[0];
        Class<E> entityClass;
        Class<ID> entityUniqueIdClass = null;
        Set<String> entityFieldNameSet = new HashSet<>();
        Field entityUniqueIdField = null;
        try {
            entityClass = (Class<E>) Class.forName(entityClassName);
            Field[] declaredFields = entityClass.getDeclaredFields();
            if (declaredFields.length == 0) {
                throw new NoFieldsException(entityClass);
            }
            for (Field field : declaredFields) {
                String lowerFieldName = field.getName().toLowerCase(Locale.ROOT);
                if (entityFieldNameSet.contains(lowerFieldName)) {
                    throw new DuplicateFieldException(field, entityClass);
                }
                entityFieldNameSet.add(lowerFieldName);
                if (Modifier.isFinal(field.getModifiers())) {
                    throw new FinalFieldException(field, entityClass);
                }
                if (!field.isAnnotationPresent(Id.class)) {
                    continue;
                }
                entityUniqueIdClass = (Class<ID>) field.getType();
                entityUniqueIdField = field;
                entityUniqueIdField.setAccessible(true);
            }
            if (entityUniqueIdClass == null) {
                throw new NoUniqueIdException(entityClass);
            }
        } catch (ClassNotFoundException e) {
            throw new EntityClassNotFoundException(repoClass, e);
        }

        // These methods are ignored by our methods processing.
        List<String> ignoredMethods = Arrays.asList(
                // Predefined methods by Repo interface
                "getCollectionName", "getUniqueId", "getEntityClass", "getEntityUniqueIdClass",
                "findById", "findAll", "delete", "deleteById", "deleteAll", "save", "saveAll", "exists", "existsById",
                // Predefined methods by Java
                "toString", "hashCode", "equals", "notify", "notifyAll", "wait", "finalize", "clone");
        for (Method method : repoClass.getMethods()) {
            if (ignoredMethods.contains(method.getName())) {
                continue;
            }
            // Check for the return-types of the methods, and their defined names to match our pattern.
            checkReturnTypes(method, entityClass);
            checkMethodOperation(method, entityClass);
        }

        // Parse predefined collection name and create mongo collection
        if (!repoClass.isAnnotationPresent(Repository.class)) {
            throw new NoCollectionNameException(repoClass);
        }
        String entityCollectionName = repoClass.getAnnotation(Repository.class).value();
        MongoCollection<E> entityCollection = manager.getDatabase().getCollection(entityCollectionName, entityClass);

        // Create dynamic repository proxy object
        ClassLoader repoClassLoader = repoClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{repoClass};
        Class<Repo<E, ID>> actualClass = (Class<Repo<E, ID>>) repoClass;
        Repo<E, ID> repo = (Repo<E, ID>) Proxy.newProxyInstance(repoClassLoader, interfaces,
                new RepoInvocation<>(entityCollectionName, entityCollection,
                        actualClass, entityClass, entityUniqueIdClass,
                        entityUniqueIdField));
        repoRegistry.put(repoClass, repo);
        return (R) repo;
    }

    private <E> void checkReturnTypes(Method method, Class<E> entityClass) throws Exception {
        Class<?> returnTypeClass = method.getReturnType();
        String methodName = method.getName();
        System.out.println("checkReturnTypes Method: " + methodName);
        if (GenericUtils.isTypeOf(List.class, returnTypeClass)) {
            Class<?> listType = GenericUtils.getGenericTypeOfReturnedList(method);
            if (listType.isAssignableFrom(entityClass)) {
                if (methodName.startsWith("find")) {
                    return;
                }
                throw new WrongFindMethodException(method, entityClass);
            }
            throw new WrongListTypeException(method, entityClass, listType);
        }
        if (GenericUtils.isTypeOf(entityClass, returnTypeClass)) {
            if (methodName.startsWith("find")) {
                return;
            }
            throw new WrongFindMethodException(method, entityClass);
        }
        if (GenericUtils.isTypeOf(Boolean.class, returnTypeClass)) {
            if (methodName.startsWith("delete")) {
                return;
            }
            throw new WrongDeleteMethodException(method, entityClass);
        }
        throw new InvalidReturnTypeException(returnTypeClass, methodName, entityClass);
    }

    private <E> void checkMethodOperation(Method method, Class<E> entityClass) throws Exception {
        String methodName = method.getName();
        String fieldFilterName = MethodNameUtil.removeLeadingOperator(methodName);
        if (fieldFilterName == null) {
            throw new InvalidMethodOperationException(method, entityClass);
        }
        if (!MethodNameUtil.containsAnyFilter(methodName)) {
            throw new NoFilterException(method, entityClass);
        }
        if (methodName.contains("And") && methodName.contains("Or")) {
            throw new DuplicateOperationException(method, entityClass);
        }
        if (!methodName.contains("And") && !methodName.contains("Or")) {
            checkParamType(method, fieldFilterName, 0, entityClass);
        } else if (methodName.contains("And") || methodName.contains("Or")) {
            String[] fieldFilterSplitByType = fieldFilterName.contains("And") ?
                    fieldFilterName.split("And") : fieldFilterName.split("Or");
            for (int i = 0; i < fieldFilterSplitByType.length; i++) {
                String fieldFilterPart = fieldFilterSplitByType[i];
                checkParamType(method, fieldFilterPart, i, entityClass);
            }
        }
    }

    private <E> void checkParamType(Method method, String operator, int paramIndex, Class<E> entityClass) throws Exception {
        // NameEqualsIgnoreCase (String name);
        // NumberGreaterThan (String name, Double number);
        String expectedField = MethodNameUtil.replaceEndingFilter(operator);
        if (expectedField == null) {
            throw new NoFilterException(method, entityClass);
        }
        expectedField = expectedField.endsWith("Not") ? expectedField.replaceFirst("Not", "") : expectedField;
        if(operator.endsWith("Has")) {
            boolean anyFieldFound = false;
            for (Field field : entityClass.getDeclaredFields()) {
                if (!field.getName().equalsIgnoreCase(expectedField)) {
                    continue;
                }
                anyFieldFound = true;
            }
            if(!anyFieldFound) {
                throw new InvalidFilterException(method, entityClass);
            }
            return;
        }
        //TODO: Check here for SortOptions
        if(method.getParameters().length - 1 < paramIndex) {
            throw new WrongParametersCountException(method, entityClass);
        }
        Class<?> paramClass = method.getParameters()[paramIndex].getType();
        if (paramClass == null) {
            throw new WrongParametersCountException(method, entityClass);
        }
        // Name
        boolean anyFieldFound = false;
        for (Field field : entityClass.getDeclaredFields()) {
            if (!field.getName().equalsIgnoreCase(expectedField)) {
                continue;
            }
            anyFieldFound = true;
            Class<?> fieldClass = field.getType();
            if (GenericUtils.isTypeOf(fieldClass, paramClass)) {
                continue;
            }
            throw new MismatchingParameterException(method, entityClass, fieldClass, paramClass);
        }
        if(!anyFieldFound) {
            throw new InvalidFilterException(method, entityClass);
        }
    }
}