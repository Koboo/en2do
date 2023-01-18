package eu.koboo.en2do.repository.internal;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import eu.koboo.en2do.exception.MethodInvalidPageException;
import eu.koboo.en2do.exception.MethodInvalidSortLimitException;
import eu.koboo.en2do.exception.MethodInvalidSortSkipException;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.internal.methods.dynamic.DynamicMethod;
import eu.koboo.en2do.repository.internal.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.options.AppendMethodAsComment;
import eu.koboo.en2do.repository.options.methods.paging.Pager;
import eu.koboo.en2do.repository.options.methods.sort.Limit;
import eu.koboo.en2do.repository.options.methods.sort.Skip;
import eu.koboo.en2do.repository.options.methods.sort.Sort;
import eu.koboo.en2do.repository.options.methods.sort.SortBy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class RepositoryMeta<E, ID, R extends Repository<E, ID>> {

    String collectionName;
    MongoCollection<E> collection;

    Class<R> repositoryClass;
    Class<E> entityClass;

    Set<Field> entityFieldSet;

    Class<ID> entityUniqueIdClass;
    Field entityUniqueIdField;

    @Getter(AccessLevel.NONE)
    boolean appendMethodAsComment;

    @Getter(AccessLevel.NONE)
    Map<String, PredefinedMethod<E, ID, R>> methodRegistry;

    @Getter(AccessLevel.NONE)
    Map<String, DynamicMethod<E, ID, R>> dynamicMethodRegistry;

    public RepositoryMeta(Class<R> repositoryClass, Class<E> entityClass,
                          Set<Field> entityFieldSet,
                          Class<ID> entityUniqueIdClass, Field entityUniqueIdField,
                          MongoCollection<E> collection, String collectionName) {
        this.collectionName = collectionName;
        this.collection = collection;

        this.repositoryClass = repositoryClass;
        this.entityClass = entityClass;

        this.entityFieldSet = entityFieldSet;

        this.entityUniqueIdClass = entityUniqueIdClass;
        this.entityUniqueIdField = entityUniqueIdField;

        this.appendMethodAsComment = repositoryClass.isAnnotationPresent(AppendMethodAsComment.class);

        this.methodRegistry = new HashMap<>();
        this.dynamicMethodRegistry = new HashMap<>();
    }

    public void destroy() {
        methodRegistry.clear();
        dynamicMethodRegistry.clear();
    }

    public boolean isRepositoryMethod(String methodName) {
        return methodRegistry.containsKey(methodName);
    }

    public void registerPredefinedMethod(PredefinedMethod<E, ID, R> methodHandler) {
        String methodName = methodHandler.getMethodName();
        if (methodRegistry.containsKey(methodName)) {
            throw new RuntimeException("Already registered method with name \"" + methodName + "\".");
        }
        methodRegistry.put(methodName, methodHandler);
    }

    public PredefinedMethod<E, ID, R> lookupPredefinedMethod(String methodName) {
        return methodRegistry.get(methodName);
    }

    public void registerDynamicMethod(String methodName, DynamicMethod<E, ID, R> dynamicMethod) {
        if (dynamicMethodRegistry.containsKey(methodName)) {
            // Removed regex condition, because the hashmap couldn't handle methods with the same name.
            throw new RuntimeException("Already registered dynamicMethod with name \"" + methodName + "\".");
        }
        dynamicMethodRegistry.put(methodName, dynamicMethod);
    }

    public DynamicMethod<E, ID, R> lookupDynamicMethod(String methodName) {
        return dynamicMethodRegistry.get(methodName);
    }

    @SuppressWarnings("unchecked")
    public E checkEntity(Method method, Object argument) {
        E entity = (E) argument;
        if (entity == null) {
            throw new NullPointerException("Entity of type " + entityClass.getName() + " as parameter of method " +
                    method.getName() + " is null.");
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    public ID checkUniqueId(Method method, Object argument) {
        ID uniqueId = (ID) argument;
        if (uniqueId == null) {
            throw new NullPointerException("UniqueId of Entity of type " + entityClass.getName() + " as parameter of method " +
                    method.getName() + " is null.");
        }
        return uniqueId;
    }

    @SuppressWarnings("unchecked")
    public List<E> checkEntityList(Method method, Object argument) {
        List<E> entity = (List<E>) argument;
        if (entity == null) {
            throw new NullPointerException("List of Entities of type " + entityClass.getName() + " as parameter of method " +
                    method.getName() + " is null.");
        }
        return entity;
    }

    public ID getUniqueId(E entity) throws IllegalAccessException {
        return entityUniqueIdClass.cast(entityUniqueIdField.get(entity));
    }

    public Bson createIdFilter(ID uniqueId) {
        return Filters.eq(entityUniqueIdField.getName(), uniqueId);
    }

    public FindIterable<E> createIterable(Bson filter, String methodName) {
        FindIterable<E> findIterable;
        if (filter != null) {
            findIterable = collection.find(filter);
        } else {
            findIterable = collection.find();
        }
        if (appendMethodAsComment) {
            findIterable.comment("en2do \"" + methodName + "\"");
        }
        return findIterable;
    }

    public FindIterable<E> applySortObject(Method method, FindIterable<E> findIterable, Object[] args) throws Exception {
        int parameterCount = method.getParameterCount();
        if (parameterCount <= 0) {
            return findIterable;
        }
        Class<?> lastParamType = method.getParameterTypes()[method.getParameterCount() - 1];
        if (!lastParamType.isAssignableFrom(Sort.class)) {
            return findIterable;
        }
        Object lastParamObject = args == null ? null : args[args.length - 1];
        if (!(lastParamObject instanceof Sort sortOptions)) {
            return findIterable;
        }
        if (!sortOptions.getFieldDirectionMap().isEmpty()) {
            for (Map.Entry<String, Integer> byField : sortOptions.getFieldDirectionMap().entrySet()) {
                findIterable = findIterable.sort(new BasicDBObject(byField.getKey(), byField.getValue()));
            }
        }
        int limit = sortOptions.getLimit();
        if (limit != -1) {
            if (limit < -1 || limit == 0) {
                throw new MethodInvalidSortLimitException(method, repositoryClass);
            }
            findIterable = findIterable.limit(limit);
        }
        int skip = sortOptions.getSkip();
        if (skip != -1) {
            if (skip < -1 || skip == 0) {
                throw new MethodInvalidSortSkipException(method, repositoryClass);
            }
            findIterable = findIterable.skip(skip);
        }
        findIterable.allowDiskUse(true);
        return findIterable;
    }

    public FindIterable<E> applySortAnnotations(Method method, FindIterable<E> findIterable) throws Exception {
        SortBy[] sortAnnotations = method.getAnnotationsByType(SortBy.class);
        if (sortAnnotations != null) {
            for (SortBy sortBy : sortAnnotations) {
                int orderType = sortBy.ascending() ? 1 : -1;
                findIterable = findIterable.sort(new BasicDBObject(sortBy.field(), orderType));
            }
        }
        if (method.isAnnotationPresent(Limit.class)) {
            Limit limit = method.getAnnotation(Limit.class);
            int value = limit.value();
            if (value <= 0) {
                throw new MethodInvalidSortLimitException(method, repositoryClass);
            }
            findIterable = findIterable.limit(value);
        }
        if (method.isAnnotationPresent(Skip.class)) {
            Skip skip = method.getAnnotation(Skip.class);
            int value = skip.value();
            if (value <= 0) {
                throw new MethodInvalidSortSkipException(method, repositoryClass);
            }
            findIterable = findIterable.skip(value);
        }
        findIterable.allowDiskUse(true);
        return findIterable;
    }

    public FindIterable<E> applyPageObject(Method method, FindIterable<E> findIterable, Object[] args) throws Exception {
        int parameterCount = method.getParameterCount();
        if (parameterCount <= 0) {
            return findIterable;
        }
        Class<?> lastParamType = method.getParameterTypes()[method.getParameterCount() - 1];
        if (!lastParamType.isAssignableFrom(Pager.class)) {
            return findIterable;
        }
        Object lastParamObject = args == null ? null : args[args.length - 1];
        if (!(lastParamObject instanceof Pager pageObject)) {
            return findIterable;
        }
        if (pageObject.getPage() <= 0) {
            throw new MethodInvalidPageException(method, repositoryClass);
        }
        if (!pageObject.getPageDirectionMap().isEmpty()) {
            for (Map.Entry<String, Integer> byField : pageObject.getPageDirectionMap().entrySet()) {
                findIterable = findIterable.sort(new BasicDBObject(byField.getKey(), byField.getValue()));
            }
        }
        int skip = (int) ((pageObject.getPage() - 1) * pageObject.getEntitiesPerPage());
        findIterable = findIterable.limit(pageObject.getEntitiesPerPage()).skip(skip);
        findIterable.allowDiskUse(true);
        return findIterable;
    }
}
