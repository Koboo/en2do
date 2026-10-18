package eu.koboo.en2do.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.factory.mapping.RepositoryMapping;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidQueryLimitException;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidQuerySkipException;
import eu.koboo.en2do.mongodb.methods.dynamic.IndexedMethod;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.QueryOptions;
import eu.koboo.en2do.repository.methods.fields.FieldUpdate;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.fields.UpdateType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public final class RepositoryData<E, ID, R extends Repository<E, ID>> {

    MongoManager mongoManager;
    RepositoryMapping mapping;
    MongoCollection<E> entityCollection;

    @Getter(AccessLevel.NONE)
    Map<String, IndexedMethod<E, ID, R>> dynamicMethodRegistry;

    public RepositoryData(MongoManager mongoManager,
                          RepositoryMapping mapping,
                          MongoCollection<E> entityCollection) {
        this.mongoManager = mongoManager;
        this.mapping = mapping;
        this.entityCollection = entityCollection;
        this.dynamicMethodRegistry = new HashMap<>();
    }

    public void close() {
        dynamicMethodRegistry.clear();
    }

    public void registerDynamicMethod(String methodName, IndexedMethod<E, ID, R> dynamicMethod) {
        if (dynamicMethodRegistry.containsKey(methodName)) {
            // Removed regex condition, because the hashmap couldn't handle methods with the same name.
            throw new RuntimeException("Already registered dynamic method with name \"" + methodName + "\".");
        }
        dynamicMethodRegistry.put(methodName, dynamicMethod);
    }

    public IndexedMethod<E, ID, R> getDynamicMethod(String methodName) {
        return dynamicMethodRegistry.get(methodName);
    }

    public FindIterable<E> createFindIterableBase(Bson filter, String methodName) {
        FindIterable<E> findIterable;
        if (filter != null) {
            findIterable = entityCollection.find(filter);
        } else {
            findIterable = entityCollection.find();
        }
        // FIXME: SignatureUtils
        if (mongoManager.getMongoSettings().appendMethodAsComment()) {
            findIterable.comment("en2do \"" + methodName + "\"");
        }
        return findIterable;
    }

    public FindIterable<E> applyQueryOptions(Method method,
                                             FindIterable<E> findIterable,
                                             Object[] args) {
        Class<?> repositoryClass = mapping.getRepositoryClass();
        int parameterCount = method.getParameterCount();
        if (parameterCount <= 0) {
            return findIterable;
        }
        Class<?> lastParamType = method.getParameterTypes()[method.getParameterCount() - 1];
        if (!lastParamType.isAssignableFrom(QueryOptions.class)) {
            return findIterable;
        }
        Object lastParameter;
        if (args == null) {
            lastParameter = null;
        } else {
            lastParameter = args[args.length - 1];
        }
        if (!(lastParameter instanceof QueryOptions queryOptions)) {
            return findIterable;
        }
        int limit = queryOptions.getLimit();
        int skip = queryOptions.getSkip();
        String fieldName = queryOptions.getFieldName();
        boolean ascending = queryOptions.isAscending();
        if (fieldName != null && !fieldName.isEmpty()) {
            int direction = ascending ? 1 : -1;
            findIterable = findIterable.sort(new BasicDBObject(fieldName, direction));
        }

        if (skip != -1) {
            if (skip < 0) {
                throw new MethodInvalidQuerySkipException(repositoryClass, method);
            }
            findIterable = findIterable.skip(skip);
        }

        if (limit != -1) {
            if (limit < 0) {
                throw new MethodInvalidQueryLimitException(repositoryClass, method);
            }
            findIterable = findIterable.limit(limit);
        }

        return findIterable.allowDiskUse(mongoManager.getMongoSettings().allowDiskUse());
    }

    public Object getFilterableValue(Object object) {
        return getFilterableValue(object, false);
    }

    public Object getFilterableValue(Object object, boolean isMapKey) {
        // FIXME: Ordinal or name? -> Setting
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
