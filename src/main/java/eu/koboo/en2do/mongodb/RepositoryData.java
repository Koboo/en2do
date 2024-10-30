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
        findIterable = sortDirection(findIterable, sortOptions.getFieldDirectionMap());

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
            Map<String, Boolean> fieldSortMap = new LinkedHashMap<>();
            for (SortBy sortBy : sortAnnotations) {
                fieldSortMap.put(sortBy.field(), sortBy.ascending());
            }
            findIterable = sortDirection(findIterable, fieldSortMap);
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
        // Pagination should always be the last parameter of the method.
        // But of whatever reason, we could do something in the validation wrong,
        // so we catch the class casting exception anyway.
        Object parameterObject = args[args.length - 1];
        Pagination pagination;
        try {
            pagination = (Pagination) parameterObject;
        } catch (ClassCastException e) {
            throw new RuntimeException("Invalid Pagination object " + parameterObject.getClass() + ": ", e);
        }

        // We do not allow pages lower or equal to zero. The results
        // would just be empty, so we throw an exception to not allow that.
        if (pagination.getPage() <= 0) {
            throw new MethodInvalidPageException(method, repositoryClass);
        }

        // Let's apply the sorting direction of the pagination object.
        findIterable = sortDirection(findIterable, pagination.getPageDirectionMap());

        int limit = pagination.getEntitiesPerPage();
        int skip = (int) ((pagination.getPage() - 1) * limit);

        findIterable = findIterable
            .limit(limit)
            .skip(skip)
            .allowDiskUse(mongoManager.getSettingsBuilder().isAllowDiskUse());
        return findIterable;
    }

    private FindIterable<E> sortDirection(FindIterable<E> findIterable, Map<String, Boolean> fieldSortMap) {
        if (findIterable == null) {
            return findIterable;
        }

        if (fieldSortMap == null || fieldSortMap.isEmpty()) {
            return findIterable;
        }

        for (String sortKey : fieldSortMap.keySet()) {
            Boolean ascending = fieldSortMap.get(sortKey);
            if (ascending == null) {
                continue;
            }
            int direction = ascending ? 1 : -1;
            findIterable = findIterable.sort(new BasicDBObject(sortKey, direction));
        }
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
        Document rootDocument = new Document();
        Document documentSetFields = new Document();
        Document documentRenameFields = new Document();
        Document documentUnsetFields = new Document();
        for (FieldUpdate fieldUpdate : updateBatch.getUpdateList()) {
            String fieldBsonName = fieldUpdate.getFieldName();
            UpdateType updateType = fieldUpdate.getUpdateType();

            Object filterableValue = null;
            if (fieldUpdate.getValue() != null && (updateType == UpdateType.SET || updateType == UpdateType.RENAME)) {
                filterableValue = getFilterableValue(fieldUpdate.getValue());
            }

            switch (updateType) {
                case SET:
                    documentSetFields.append(fieldBsonName, filterableValue);
                    break;
                case RENAME:
                    documentRenameFields.append(fieldBsonName, filterableValue);
                    break;
                case REMOVE:
                    documentUnsetFields.append(fieldBsonName, 0);
                    break;
            }
        }
        if (!documentUnsetFields.isEmpty()) {
            rootDocument.append(UpdateType.REMOVE.getDocumentType(), documentUnsetFields);
        }
        if (!documentSetFields.isEmpty()) {
            rootDocument.append(UpdateType.SET.getDocumentType(), documentSetFields);
        }
        if (!documentRenameFields.isEmpty()) {
            rootDocument.append(UpdateType.RENAME.getDocumentType(), documentRenameFields);
        }
        return rootDocument;
    }
}
