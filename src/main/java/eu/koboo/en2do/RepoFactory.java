package eu.koboo.en2do;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.annotation.Id;
import eu.koboo.en2do.annotation.Repository;
import eu.koboo.en2do.exception.*;
import eu.koboo.en2do.misc.FilterOperator;
import eu.koboo.en2do.misc.FilterType;
import eu.koboo.en2do.utility.MethodNameUtil;
import eu.koboo.en2do.utility.GenericUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;

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
            throw new RepositoryNoTypeException(repoClass);
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
                throw new RepositoryNoFieldsException(repoClass);
            }
            for (Field field : declaredFields) {
                String lowerFieldName = field.getName().toLowerCase(Locale.ROOT);
                if (entityFieldNameSet.contains(lowerFieldName)) {
                    throw new RepositoryDuplicatedFieldException(field, repoClass);
                }
                entityFieldNameSet.add(lowerFieldName);
                if (Modifier.isFinal(field.getModifiers())) {
                    throw new RepositoryFinalFieldException(field, repoClass);
                }
                if (!field.isAnnotationPresent(Id.class)) {
                    continue;
                }
                entityUniqueIdClass = (Class<ID>) field.getType();
                entityUniqueIdField = field;
                entityUniqueIdField.setAccessible(true);
            }
            if (entityUniqueIdClass == null) {
                throw new RepositoryIdNotFoundException(entityClass);
            }
        } catch (ClassNotFoundException e) {
            throw new RepositoryEntityNotFoundException(repoClass, e);
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
            checkReturnTypes(method, entityClass, repoClass);
            checkMethodOperation(method, entityClass, repoClass);
        }

        // Parse predefined collection name and create mongo collection
        if (!repoClass.isAnnotationPresent(Repository.class)) {
            throw new RepositoryNameNotFoundException(repoClass);
        }
        String entityCollectionName = repoClass.getAnnotation(Repository.class).value();
        MongoCollection<E> entityCollection = manager.getDatabase().getCollection(entityCollectionName, entityClass);

        // Create dynamic repository proxy object
        ClassLoader repoClassLoader = repoClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{repoClass};
        Class<Repo<E, ID>> actualClass = (Class<Repo<E, ID>>) repoClass;
        Repo<E, ID> repo = (Repo<E, ID>) Proxy.newProxyInstance(repoClassLoader, interfaces,
                new RepoInvocation<>(this, entityCollectionName, entityCollection,
                        actualClass, entityClass, entityUniqueIdClass,
                        entityUniqueIdField));
        repoRegistry.put(repoClass, repo);
        return (R) repo;
    }

    private <E> void checkReturnTypes(Method method, Class<E> entityClass, Class<?> repoClass) throws Exception {
        Class<?> returnTypeClass = method.getReturnType();
        String methodName = method.getName();
        if (GenericUtils.isTypeOf(List.class, returnTypeClass)) {
            Class<?> listType = GenericUtils.getGenericTypeOfReturnedList(method);
            if (listType.isAssignableFrom(entityClass)) {
                if (methodName.startsWith("findBy")) {
                    return;
                }
                throw new MethodFindReturnTypeException(method, entityClass, repoClass);
            }
            throw new MethodFindListTypeException(method, entityClass, listType);
        }
        if (GenericUtils.isTypeOf(entityClass, returnTypeClass)) {
            if (methodName.startsWith("findBy")) {
                return;
            }
            throw new MethodFindReturnTypeException(method, entityClass, repoClass);
        }
        if (GenericUtils.isTypeOf(Boolean.class, returnTypeClass)) {
            if (methodName.startsWith("deleteBy")) {
                return;
            }
            throw new MethodDeleteReturnTypeException(method, repoClass);
        }
        throw new MethodUnsupportedReturnTypeException(returnTypeClass, methodName, entityClass);
    }

    // ReturnType already checked.
    private <E> void checkMethodOperation(Method method, Class<E> entityClass, Class<?> repoClass) throws Exception {
        String methodName = method.getName();
        String fieldFilterName = MethodNameUtil.removeLeadingOperator(methodName);
        if (fieldFilterName == null) {
            throw new MethodInvalidSignatureException(method, entityClass);
        }
        if (methodName.contains("And") && methodName.contains("Or")) {
            throw new MethodDuplicatedChainException(method, entityClass);
        }
        int expectedParameters = countExpectedParameters(entityClass, repoClass, method, fieldFilterName);
        System.out.println("[DEBUG] Method: " + method.getName());
        System.out.println("[DEBUG] ExpParams: " + expectedParameters);
        System.out.println("[DEBUG] ActParams: " + method.getParameterCount());
        if(expectedParameters != method.getParameterCount()) {
            // TODO: Check for "additional" SortOptions as last parameter if return type is list
            throw new MethodParameterCountException(method, repoClass, expectedParameters, method.getParameterCount());
        }
        String[] fieldFilterSplitByType = fieldFilterName.contains("And") ?
                fieldFilterName.split("And") : fieldFilterName.split("Or");
        int nextIndex = 0;
        for (int i = 0; i < fieldFilterSplitByType.length; i++) {
            String fieldFilterPart = fieldFilterSplitByType[i];
            FilterType filterType = createFilterType(entityClass, repoClass, method, fieldFilterPart);
            checkParameterType(entityClass, method, filterType, nextIndex, repoClass);
            nextIndex = i + filterType.operator().getExpectedParameterCount();
        }
    }

    private <E> int countExpectedParameters(Class<E> entityClass, Class<?> repoClass, Method method, String fieldFilterPart) throws Exception {
        String methodName = method.getName();
        if(!methodName.contains("And") && !methodName.contains("Or")) {
            FilterType filterType = createFilterType(entityClass, repoClass, method, fieldFilterPart);
            return filterType.operator().getExpectedParameterCount();
        }
        String[] fieldFilterPartSplitByType = fieldFilterPart.contains("And") ?
                fieldFilterPart.split("And") : fieldFilterPart.split("Or");
        int expectedCount = 0;
        for (String fieldFilterPartSplit : fieldFilterPartSplitByType) {
            FilterType filterType = createFilterType(entityClass, repoClass, method, fieldFilterPartSplit);
            expectedCount += filterType.operator().getExpectedParameterCount();
        }
        return expectedCount;
    }

    private <E> void checkParameterType(Class<E> entityClass, Method method, FilterType filterType, int startIndex, Class<?> repoClass) throws Exception {
        int expectedParamCount = filterType.operator().getExpectedParameterCount();
        System.out.println("[DEBUG] Check params for " + filterType.operator().name());
        for(int i = 0; i < expectedParamCount; i++) {
            Class<?> paramClass = method.getParameters()[startIndex + i].getType();
            if (paramClass == null) {
                throw new MethodParameterNotFoundException(method, repoClass, (startIndex + expectedParamCount),
                        method.getParameterCount());
            }
            // Special check for regex, because it has only String or Pattern parameters
            if(filterType.operator() == FilterOperator.REGEX) {
                if(!GenericUtils.isTypeOf(String.class, paramClass) && !GenericUtils.isTypeOf(Pattern.class, paramClass)) {
                    throw new MethodInvalidRegexParameterException(method, repoClass, paramClass);
                }
                return;
            }
            Class<?> fieldClass = filterType.field().getType();
            if(!GenericUtils.isTypeOf(fieldClass, paramClass)) {
                throw new MethodMismatchingTypeException(method, repoClass, fieldClass, paramClass);
            }
        }
    }

    protected <E> FilterType createFilterType(Class<E> entityClass, Class<?> repoClass, Method method, String operatorString) throws Exception {
        FilterOperator filterOperator = FilterOperator.parseFilterEndsWith(operatorString);
        if(filterOperator == null) {
            throw new MethodNoOperatorException(method, repoClass);
        }
        String expectedField = filterOperator.removeOperatorFrom(operatorString);
        expectedField = expectedField.endsWith("Not") ? expectedField.replaceFirst("Not", "") : expectedField;
        Field field = findExpectedField(entityClass, expectedField);
        if(field == null) {
            throw new MethodFieldNotFoundException(expectedField, method, entityClass, repoClass);
        }
        return new FilterType(field, filterOperator);
    }

    private <E> Field findExpectedField(Class<E> entityClass, String expectedField) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (!field.getName().equalsIgnoreCase(expectedField)) {
                continue;
            }
            return field;
        }
        return null;
    }
}