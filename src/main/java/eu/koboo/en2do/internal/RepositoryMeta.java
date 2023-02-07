package eu.koboo.en2do.internal;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import eu.koboo.en2do.internal.exception.methods.MethodInvalidPageException;
import eu.koboo.en2do.internal.exception.methods.MethodInvalidSortLimitException;
import eu.koboo.en2do.internal.exception.methods.MethodInvalidSortSkipException;
import eu.koboo.en2do.internal.methods.dynamic.DynamicMethod;
import eu.koboo.en2do.internal.methods.predefined.PredefinedMethod;
import eu.koboo.en2do.repository.AppendMethodAsComment;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.SeparateEntityId;
import eu.koboo.en2do.repository.methods.fields.FieldUpdate;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.fields.UpdateType;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.repository.methods.sort.Limit;
import eu.koboo.en2do.repository.methods.sort.Skip;
import eu.koboo.en2do.repository.methods.sort.Sort;
import eu.koboo.en2do.repository.methods.sort.SortBy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class RepositoryMeta<E, ID, R extends Repository<E, ID>> {

    @NotNull
    String collectionName;

    @NotNull
    MongoCollection<E> collection;

    @NotNull
    Class<R> repositoryClass;

    @NotNull
    Class<E> entityClass;

    @NotNull
    Set<Field> entityFieldSet;

    @NotNull
    Class<ID> entityUniqueIdClass;

    @NotNull
    Field entityUniqueIdField;

    @Getter(AccessLevel.NONE)
    boolean appendMethodAsComment;
    boolean separateEntityId;

    @Getter(AccessLevel.NONE)
    @NotNull
    Map<String, PredefinedMethod<E, ID, R>> methodRegistry;

    @Getter(AccessLevel.NONE)
    @NotNull
    Map<String, DynamicMethod<E, ID, R>> dynamicMethodRegistry;

    public RepositoryMeta(@NotNull Class<R> repositoryClass, @NotNull Class<E> entityClass,
                          @NotNull Set<Field> entityFieldSet,
                          @NotNull Class<ID> entityUniqueIdClass, @NotNull Field entityUniqueIdField,
                          @NotNull MongoCollection<E> collection, @NotNull String collectionName) {
        this.collectionName = collectionName;
        this.collection = collection;

        this.repositoryClass = repositoryClass;
        this.entityClass = entityClass;

        this.entityFieldSet = entityFieldSet;

        this.entityUniqueIdClass = entityUniqueIdClass;
        this.entityUniqueIdField = entityUniqueIdField;

        this.appendMethodAsComment = repositoryClass.isAnnotationPresent(AppendMethodAsComment.class);
        this.separateEntityId = repositoryClass.isAnnotationPresent(SeparateEntityId.class);

        this.methodRegistry = new HashMap<>();
        this.dynamicMethodRegistry = new HashMap<>();
    }

    public void destroy() {
        methodRegistry.clear();
        dynamicMethodRegistry.clear();
    }

    public boolean isRepositoryMethod(@NotNull String methodName) {
        return methodRegistry.containsKey(methodName);
    }

    public void registerPredefinedMethod(@NotNull PredefinedMethod<E, ID, R> methodHandler) {
        String methodName = methodHandler.getMethodName();
        if (methodRegistry.containsKey(methodName)) {
            throw new RuntimeException("Already registered method with name \"" + methodName + "\".");
        }
        methodRegistry.put(methodName, methodHandler);
    }

    public @Nullable PredefinedMethod<E, ID, R> lookupPredefinedMethod(@NotNull String methodName) {
        return methodRegistry.get(methodName);
    }

    public void registerDynamicMethod(@NotNull String methodName, @NotNull DynamicMethod<E, ID, R> dynamicMethod) {
        if (dynamicMethodRegistry.containsKey(methodName)) {
            // Removed regex condition, because the hashmap couldn't handle methods with the same name.
            throw new RuntimeException("Already registered dynamicMethod with name \"" + methodName + "\".");
        }
        dynamicMethodRegistry.put(methodName, dynamicMethod);
    }

    public @Nullable DynamicMethod<E, ID, R> lookupDynamicMethod(@NotNull String methodName) {
        return dynamicMethodRegistry.get(methodName);
    }

    @SuppressWarnings("unchecked")
    public @NotNull E checkEntity(@NotNull Method method, @Nullable Object argument) {
        E entity = (E) argument;
        if (entity == null) {
            throw new NullPointerException("Entity of type " + entityClass.getName() + " as parameter of method " +
                                           method.getName() + " is null.");
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    public @NotNull ID checkUniqueId(@NotNull Method method, @Nullable Object argument) {
        ID uniqueId = (ID) argument;
        if (uniqueId == null) {
            throw new NullPointerException("UniqueId of Entity of type " + entityClass.getName() + " as parameter of method " +
                                           method.getName() + " is null.");
        }
        return uniqueId;
    }

    @SuppressWarnings("unchecked")
    public @NotNull List<E> checkEntityList(@NotNull Method method, @Nullable Object argument) {
        List<E> entity = (List<E>) argument;
        if (entity == null) {
            throw new NullPointerException("List of Entities of type " + entityClass.getName() + " as parameter of method " +
                                           method.getName() + " is null.");
        }
        return entity;
    }

    public @Nullable ID getUniqueId(@NotNull E entity) throws IllegalAccessException {
        return entityUniqueIdClass.cast(entityUniqueIdField.get(entity));
    }

    public @NotNull Bson createIdFilter(@NotNull ID uniqueId) {
        if (!separateEntityId) {
            return Filters.eq("_id", uniqueId);
        } else {
            return Filters.eq(entityUniqueIdField.getName(), uniqueId);
        }
    }

    public @NotNull Bson createIdExistsFilter() {
        if (!separateEntityId) {
            return Filters.exists("_id");
        } else {
            return Filters.exists(entityUniqueIdField.getName());
        }
    }

    public @NotNull FindIterable<E> createIterable(@Nullable Bson filter, @NotNull String methodName) {
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

    public @NotNull FindIterable<E> applySortObject(@NotNull Method method,
                                                    @NotNull FindIterable<E> findIterable,
                                                    @NotNull Object[] args) throws Exception {
        int parameterCount = method.getParameterCount();
        if (parameterCount <= 0) {
            return findIterable;
        }
        Class<?> lastParamType = method.getParameterTypes()[method.getParameterCount() - 1];
        if (!lastParamType.isAssignableFrom(Sort.class)) {
            return findIterable;
        }
        Object lastParamObject = args == null ? null : args[args.length - 1];
        if (!(lastParamObject instanceof Sort)) {
            return findIterable;
        }
        Sort sortOptions = (Sort) lastParamObject;
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

    public @NotNull FindIterable<E> applySortAnnotations(@NotNull Method method,
                                                         @NotNull FindIterable<E> findIterable) throws Exception {
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

    public @NotNull FindIterable<E> applyPageObject(@NotNull Method method,
                                                    @NotNull FindIterable<E> findIterable, Object[] args) throws Exception {
        Pagination pagination = (Pagination) args[args.length - 1];
        if (pagination.getPage() <= 0) {
            throw new MethodInvalidPageException(method, repositoryClass);
        }
        if (!pagination.getPageDirectionMap().isEmpty()) {
            for (Map.Entry<String, Integer> byField : pagination.getPageDirectionMap().entrySet()) {
                findIterable = findIterable.sort(new BasicDBObject(byField.getKey(), byField.getValue()));
            }
        }
        int skip = (int) ((pagination.getPage() - 1) * pagination.getEntitiesPerPage());
        findIterable = findIterable.limit(pagination.getEntitiesPerPage()).skip(skip);
        findIterable.allowDiskUse(true);
        return findIterable;
    }

    public @NotNull String getPredefinedNameByAsyncName(@NotNull String asyncName) {
        String predefinedName = asyncName.replaceFirst("async", "");
        return predefinedName.substring(0, 1).toLowerCase(Locale.ROOT) + predefinedName.substring(1);
    }

    public @NotNull Object getFilterableValue(@NotNull Object object) {
        if (object instanceof Enum<?>) {
            return ((Enum<?>) object).name();
        }
        return object;
    }

    public Document createUpdateDocument(UpdateBatch updateBatch) {
        Document document = new Document();
        Document fieldSetDocument = new Document();
        Document fieldRenameDocument = new Document();
        Document fieldUnsetDocument = new Document();
        for (FieldUpdate fieldUpdate : updateBatch.getUpdateList()) {
            String field = fieldUpdate.getFieldName();
            UpdateType updateType = fieldUpdate.getUpdateType();
            Object filterableValue = null;
            if (fieldUpdate.getValue() != null && (updateType == UpdateType.SET || updateType == UpdateType.RENAME)) {
                filterableValue = getFilterableValue(fieldUpdate.getValue());
            }
            switch (updateType) {
                case SET:
                    fieldSetDocument.append(field, filterableValue);
                    break;
                case RENAME:
                    fieldRenameDocument.append(field, filterableValue);
                    break;
                case REMOVE:
                    fieldUnsetDocument.append(field, 0);
                    break;
            }
        }
        if (!fieldUnsetDocument.isEmpty()) {
            document.append("$unset", fieldUnsetDocument);
        }
        if (!fieldSetDocument.isEmpty()) {
            document.append("$set", fieldSetDocument);
        }
        if (!fieldRenameDocument.isEmpty()) {
            document.append("$rename", fieldRenameDocument);
        }
        return document;
    }
}
