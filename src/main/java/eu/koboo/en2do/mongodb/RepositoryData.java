package eu.koboo.en2do.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidPageException;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidSortLimitException;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidSortSkipException;
import eu.koboo.en2do.mongodb.indexer.RepositoryIndexer;
import eu.koboo.en2do.mongodb.methods.dynamic.IndexedMethod;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.fields.FieldUpdate;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.fields.UpdateType;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.repository.methods.sort.Sort;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class RepositoryData<E, ID, R extends Repository<E, ID>> {

    MongoManager mongoManager;
    RepositoryIndexer<E, ID, R> indexer;
    String collectionName;
    MongoCollection<E> entityCollection;
    Class<R> repositoryClass;
    Class<E> entityClass;
    Class<ID> entityUniqueIdClass;
    Field entityUniqueIdField;

    @Getter(AccessLevel.NONE)
    Map<String, IndexedMethod<E, ID, R>> dynamicMethodRegistry;

    public RepositoryData(MongoManager mongoManager,
                          RepositoryIndexer<E, ID, R> indexer,
                          MongoCollection<E> entityCollection) {
        this.mongoManager = mongoManager;
        this.indexer = indexer;
        this.collectionName = indexer.getCollectionName();
        this.entityCollection = entityCollection;

        this.repositoryClass = indexer.getRepositoryClass();
        this.entityClass = indexer.getEntityClass();

        this.entityUniqueIdClass = indexer.getIdClass();
        this.entityUniqueIdField = indexer.getIdField();

        this.dynamicMethodRegistry = new HashMap<>();
    }

    public void destroy() {
        dynamicMethodRegistry.clear();
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

    public FindIterable<E> createFindIterableBase(Bson filter, String methodName) {
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
        int limit = sortOptions.getLimit();
        int skip = sortOptions.getSkip();
        String fieldName = sortOptions.getFieldName();
        boolean ascending = sortOptions.isAscending();
        return sort(method, findIterable, fieldName, ascending, limit, skip);
    }

    public FindIterable<E> applyPageObject(Method method,
                                           FindIterable<E> findIterable,
                                           Object[] args) {
        // Check if the last parameter of the method is a Pagination object,
        // and if so, we apply the pagination options to the findIterable.
        if (args == null || args.length == 0) {
            return findIterable;
        }
        Object parameterObject = args[args.length - 1];
        if (!(parameterObject instanceof Pagination)) {
            return findIterable;
        }
        Pagination pagination = (Pagination) parameterObject;

        // We do not allow pages lower or equal to zero. The results
        // would just be empty, so we throw an exception to not allow that.
        if (pagination.getPage() <= 0) {
            throw new MethodInvalidPageException(repositoryClass, method);
        }

        // Let's apply the sorting direction of the pagination object.
        int limit = pagination.getEntitiesPerPage();
        int skip = (int) ((pagination.getPage() - 1) * limit);
        String fieldName = pagination.getFieldName();
        boolean ascending = pagination.isAscending();

        return sort(method, findIterable, fieldName, ascending, limit, skip);
    }

    public FindIterable<E> sort(Method method, FindIterable<E> findIterable,
                                String fieldName, boolean ascending, int limit, int skip) {
        if (fieldName != null && !fieldName.isEmpty()) {
            int direction = ascending ? 1 : -1;
            findIterable = findIterable.sort(new BasicDBObject(fieldName, direction));
        }

        if (skip != -1) {
            if (skip < 0) {
                throw new MethodInvalidSortSkipException(repositoryClass, method);
            }
            findIterable = findIterable.skip(skip);
        }

        if (limit != -1) {
            if (limit < 0) {
                throw new MethodInvalidSortLimitException(repositoryClass, method);
            }
            findIterable = findIterable.limit(limit);
        }

        return findIterable.allowDiskUse(mongoManager.getSettingsBuilder().isAllowDiskUse());
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
