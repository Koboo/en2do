package eu.koboo.en2do.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidPageException;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidSortLimitException;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidSortSkipException;
import eu.koboo.en2do.mongodb.methods.dynamic.IndexedMethod;
import eu.koboo.en2do.repository.Repository;
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class RepositoryData<E, ID, R extends Repository<E, ID>> {

    private static final String UPDATE_FIELD_SET = "$set";
    private static final String UPDATE_FIELD_UNSET = "$unset";
    private static final String UPDATE_FIELD_RENAME = "$rename";

    MongoManager mongoManager;
    String collectionName;
    MongoCollection<E> entityCollection;
    Class<R> repositoryClass;
    Class<E> entityClass;
    Set<Field> entityFieldSet;
    Class<ID> entityUniqueIdClass;
    Field entityUniqueIdField;

    @Getter(AccessLevel.NONE)
    Map<String, IndexedMethod<E, ID, R>> dynamicMethodRegistry;

    public RepositoryData(MongoManager mongoManager, Class<R> repositoryClass, Class<E> entityClass,
                          Set<Field> entityFieldSet,
                          Class<ID> entityUniqueIdClass, Field entityUniqueIdField,
                          MongoCollection<E> entityCollection, String collectionName) {
        this.mongoManager = mongoManager;
        this.collectionName = collectionName;
        this.entityCollection = entityCollection;

        this.repositoryClass = repositoryClass;
        this.entityClass = entityClass;

        this.entityFieldSet = entityFieldSet;

        this.entityUniqueIdClass = entityUniqueIdClass;
        this.entityUniqueIdField = entityUniqueIdField;

        this.dynamicMethodRegistry = new HashMap<>();
    }

    public void destroy() {
        dynamicMethodRegistry.clear();
        entityFieldSet.clear();
    }

    public void registerDynamicMethod(String methodName, IndexedMethod<E, ID, R> dynamicMethod) {
        if (dynamicMethodRegistry.containsKey(methodName)) {
            // Removed regex condition, because the hashmap couldn't handle methods with the same name.
            throw new RuntimeException("Already registered dynamic method with name \"" + methodName + "\".");
        }
        dynamicMethodRegistry.put(methodName, dynamicMethod);
    }

    public IndexedMethod<E, ID, R> lookupDynamicMethod(String methodName) {
        return dynamicMethodRegistry.get(methodName);
    }

    public FindIterable<E> createIterable(Bson filter, String methodName) {
        FindIterable<E> findIterable;
        if (filter != null) {
            findIterable = entityCollection.find(filter);
        } else {
            findIterable = entityCollection.find();
        }
        if (mongoManager.getSettingsBuilder().isAppendMethodAsComment()) {
            findIterable.comment("en2do \"" + methodName + "\"");
        }
        return findIterable;
    }

    public FindIterable<E> applySortObject(Method method,
                                           FindIterable<E> findIterable,
                                           Object[] args) throws Exception {
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
        findIterable = findIterable.allowDiskUse(mongoManager.getSettingsBuilder().isAllowDiskUse());
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
        findIterable = findIterable.allowDiskUse(mongoManager.getSettingsBuilder().isAllowDiskUse());
        return findIterable;
    }

    public FindIterable<E> applyPageObject(Method method,
                                           FindIterable<E> findIterable, Object[] args) throws Exception {
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
        findIterable = findIterable
            .limit(pagination.getEntitiesPerPage())
            .skip(skip)
            .allowDiskUse(mongoManager.getSettingsBuilder().isAllowDiskUse());
        return findIterable;
    }

    public String stripAsyncName(String asyncName) {
        String predefinedName = asyncName.replaceFirst("async", "");
        return predefinedName.substring(0, 1).toLowerCase(Locale.ROOT) + predefinedName.substring(1);
    }

    public Object getFilterableValue(Object object) {
        return getFilterableValue(object, false);
    }

    public Object getFilterableValue(Object object, boolean isMapKey) {
        if (object instanceof Enum<?>) {
            return ((Enum<?>) object).name();
        }
        if (isMapKey) {
            if (object instanceof UUID) {
                return object.toString();
            }
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
            document.append(UPDATE_FIELD_UNSET, fieldUnsetDocument);
        }
        if (!fieldSetDocument.isEmpty()) {
            document.append(UPDATE_FIELD_SET, fieldSetDocument);
        }
        if (!fieldRenameDocument.isEmpty()) {
            document.append(UPDATE_FIELD_RENAME, fieldRenameDocument);
        }
        return document;
    }
}
