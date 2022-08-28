package eu.koboo.en2do;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import eu.koboo.en2do.annotation.Entity;
import eu.koboo.en2do.annotation.Id;
import eu.koboo.en2do.exception.DuplicateFieldException;
import eu.koboo.en2do.exception.FinalFieldException;
import eu.koboo.en2do.exception.NoFieldsException;
import eu.koboo.en2do.exception.NoUniqueIdException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ExecutorService;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Repository<E, ID> {

    @Getter
    final Class<E> entityClass;

    @Getter
    final String collectionName;

    final MongoCollection<Document> collection;

    final ExecutorService executorService;
    final Set<Field> fieldRegistry;
    Field entityIdField;

    @SuppressWarnings("unchecked")
    protected Repository(MongoManager mongoManager, ExecutorService executorService) {
        this.entityClass = ((Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new RuntimeException("No @Entity annotation present at entity " + entityClass.getName() + ". That's important, to create and get collections.");
        }
        this.collectionName = entityClass.getAnnotation(Entity.class).value();
        this.collection = mongoManager.getDatabase().getCollection(collectionName);
        this.executorService = executorService;
        this.fieldRegistry = new HashSet<>();
        Set<String> fieldNames = new HashSet<>();
        Field[] declaredFields = entityClass.getDeclaredFields();
        try {
            if (declaredFields.length == 0) {
                throw new NoFieldsException("No fields found in entity " + entityClass.getName());
            }
            for (Field field : declaredFields) {
                if (Modifier.isFinal(field.getModifiers())) {
                    throw new FinalFieldException("Field \"" + field.getName() + "\" is final. That's forbidden, due to java module systems.");
                }
                String fieldName = field.getName().toLowerCase(Locale.ROOT);
                if (fieldNames.contains(fieldName)) {
                    throw new DuplicateFieldException("Duplicated field name \"" + fieldName + "\". That's forbidden, due to collisions in Documents.");
                }
                fieldNames.add(fieldName);
                fieldRegistry.add(field);
                field.setAccessible(true);
                if (!field.isAnnotationPresent(Id.class)) {
                    continue;
                }
                entityIdField = field;
            }
            if (entityIdField == null) {
                throw new NoUniqueIdException("No @Id annotation in entity " + entityClass.getName() + ". That's important, to reference entities.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        fieldNames.clear();
    }

    public void close() {
        // Remove references of everything possible.
        fieldRegistry.clear();
        entityIdField = null;
        executorService.shutdownNow();
    }

    public Document toDocument(E entity) {
        try {
            Document document = new Document();
            ID uniqueId = getIdFromEntity(entity);
            if (uniqueId == null) {
                throw new NullPointerException("No uniqueId found in entity of " + entityClass.getSimpleName());
            }
            for (Field field : fieldRegistry) {
                Object entityValue = field.get(entity);
                document.put(field.getName(), entityValue);
            }
            return document;
        } catch (Exception e) {
            throw new RuntimeException("Error while converting Entity to Document: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    public E toEntity(Document document) {
        try {
            E entity = (E) entityClass.getDeclaredConstructors()[0].newInstance();
            for (Field field : fieldRegistry) {
                Object value = document.get(field.getName());
                field.set(entity, value);
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Error while converting Document to Entity: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    public ID getIdFromEntity(E entity) {
        try {
            return (ID) entityIdField.get(entity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteAll() {
        try {
            collection.drop();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(Bson filters) {
        try {
            DeleteResult result = collection.deleteOne(filters);
            return result.wasAcknowledged();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean save(E entity) {
        try {
            Document document = toDocument(entity);
            ID uniqueId = getIdFromEntity(entity);
            Bson idFilter = Filters.eq(entityIdField.getName(), uniqueId);
            if (document.isEmpty()) {
                return delete(idFilter);
            }
            UpdateOptions options = new UpdateOptions().upsert(true);
            UpdateResult result = collection.updateOne(idFilter, new BasicDBObject("$set", document), options);
            return result.wasAcknowledged();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<E> convertDocumentList(List<Document> documentList) {
        if (documentList == null || documentList.isEmpty()) {
            return new ArrayList<>();
        }
        List<E> entityList = new LinkedList<>();
        for (Document document : documentList) {
            E entity = toEntity(document);
            if (entity == null) {
                continue;
            }
            entityList.add(entity);
        }
        return entityList;
    }

    public FindIterable<Document> iterable(Bson filters) {
        return collection.find(filters);
    }

    public boolean exists(Bson filters) {
        try {
            Document document = iterable(filters).first();
            return document != null && !document.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public E find(Bson filters) {
        try {
            Document document = iterable(filters).first();
            if (document == null) {
                return null;
            }
            return toEntity(document);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<E> findAll(Bson filters) {
        try {
            List<Document> documentList = iterable(filters).into(new ArrayList<>());
            return convertDocumentList(documentList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<E> findSort(Bson filters, Bson sort) {
        try {
            List<Document> documentList = iterable(filters).sort(sort).into(new LinkedList<>());
            return convertDocumentList(documentList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<E> findLimit(Bson filters, int maxDocuments) {
        try {
            List<Document> documentList = iterable(filters).limit(maxDocuments).into(new LinkedList<>());
            return convertDocumentList(documentList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<E> findSortLimit(Bson filters, Bson sort, int maxDocuments) {
        try {
            List<Document> documentList = iterable(filters).sort(sort).limit(maxDocuments).into(new LinkedList<>());
            return convertDocumentList(documentList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<E> all() {
        try {
            List<Document> documentList = collection.find().into(new ArrayList<>());
            return convertDocumentList(documentList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Result<Boolean> deleteAllAsync() {
        return new Result<>(executorService, this::deleteAll);
    }

    public Result<Boolean> deleteAsync(Bson filters) {
        return new Result<>(executorService, () -> delete(filters));
    }

    public Result<Boolean> saveAsync(E entity) {
        return new Result<>(executorService, () -> save(entity));
    }

    public Result<Boolean> existsAsync(Bson filters) {
        return new Result<>(executorService, () -> exists(filters));
    }

    public Result<E> findAsync(Bson filters) {
        return new Result<>(executorService, () -> find(filters));
    }

    public Result<List<E>> findAllAsync(Bson filters) {
        return new Result<>(executorService, () -> findAll(filters));
    }

    public Result<List<E>> findSortAsync(Bson filters, Bson sort) {
        return new Result<>(executorService, () -> findSort(filters, sort));
    }

    public Result<List<E>> findLimitAsync(Bson filters, int maxDocuments) {
        return new Result<>(executorService, () -> findLimit(filters, maxDocuments));
    }

    public Result<List<E>> findSortLimitAsync(Bson filters, Bson sort, int maxDocuments) {
        return new Result<>(executorService, () -> findSortLimit(filters, sort, maxDocuments));
    }

    public Result<List<E>> allAsync() {
        return new Result<>(executorService, this::all);
    }
}