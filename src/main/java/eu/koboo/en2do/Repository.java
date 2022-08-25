package eu.koboo.en2do;

import com.mongodb.BasicDBObject;
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
import lombok.experimental.FieldDefaults;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ExecutorService;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Repository<T, ID> {

    final MongoManager mongoManager;
    final ExecutorService executorService;
    final Class<T> entityClass;
    final String entityCollectionName;
    final Set<Field> fieldRegistry;
    Field entityIdField;

    @SuppressWarnings("unchecked")
    protected Repository(MongoManager mongoManager, ExecutorService executorService) {
        this.mongoManager = mongoManager;
        this.executorService = executorService;
        this.entityClass = ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new RuntimeException("No @Entity annotation present at entity " + entityClass.getName() + ". That's important, to create and get collections.");
        }
        this.entityCollectionName = entityClass.getAnnotation(Entity.class).value();
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

    private MongoCollection<Document> getCollection() {
        return mongoManager.getDatabase().getCollection(entityCollectionName);
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }

    public Scope<T, ID> createScope() {
        return new Scope<>(this);
    }

    public Document toDocument(T entity) {
        Document document = new Document();
        ID uniqueId = getIdFromEntity(entity);
        if (uniqueId == null) {
            throw new NullPointerException("No uniqueId found in entity of " + entityClass.getSimpleName());
        }
        for (Field field : fieldRegistry) {
            try {
                document.put(field.getName(), field.get(entity));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
        return document;
    }

    @SuppressWarnings("unchecked")
    public T toEntity(Document document) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        T entity = (T) entityClass.getDeclaredConstructors()[0].newInstance();
        for (Field field : fieldRegistry) {
            Object value = document.get(field.getName());
            try {
                field.set(entity, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    public ID getIdFromEntity(T entity) {
        try {
            return (ID) entityIdField.get(entity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteAll() {
        try {
            getCollection().drop();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(Bson filters) {
        try {
            DeleteResult result = getCollection().deleteOne(filters);
            return result.wasAcknowledged();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean save(T entity) {
        try {
            Document document = toDocument(entity);
            ID uniqueId = getIdFromEntity(entity);
            Bson idFilter = Filters.eq(entityIdField.getName(), uniqueId);
            if (document.isEmpty()) {
                return delete(idFilter);
            }
            UpdateOptions options = new UpdateOptions().upsert(true);
            UpdateResult result = getCollection().updateOne(idFilter, new BasicDBObject("$set", document), options);
            return result.wasAcknowledged();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exists(Bson filters) {
        try {
            Document document = getCollection().find(filters).first();
            return document != null && !document.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public T find(Bson filters) {
        try {
            Document document = getCollection().find(filters).first();
            if (document == null) {
                return null;
            }
            return toEntity(document);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<T> convertDocumentList(List<Document> documentList) throws Exception {
        if (documentList == null || documentList.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> entityList = new LinkedList<>();
        for (Document document : documentList) {
            T entity = toEntity(document);
            if (entity == null) {
                continue;
            }
            entityList.add(entity);
        }
        return entityList;
    }

    public List<T> findAll(Bson filters) {
        try {
            List<Document> documentList = getCollection().find(filters).into(new ArrayList<>());
            return convertDocumentList(documentList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<T> findSort(Bson filters, Bson sort) {
        try {
            List<Document> documentList = getCollection().find(filters).sort(sort).into(new LinkedList<>());
            return convertDocumentList(documentList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<T> findLimit(Bson filters, int maxDocuments) {
        try {
            List<Document> documentList = getCollection().find(filters).limit(maxDocuments).into(new LinkedList<>());
            return convertDocumentList(documentList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<T> findSortLimit(Bson filters, Bson sort, int maxDocuments) {
        try {
            List<Document> documentList = getCollection().find(filters).sort(sort).limit(maxDocuments).into(new LinkedList<>());
            return convertDocumentList(documentList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<T> all() {
        try {
            List<Document> documentList = getCollection().find().into(new ArrayList<>());
            return convertDocumentList(documentList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Result<Boolean> asyncDeleteAll() {
        return new Result<>(executorService, this::deleteAll);
    }

    public Result<Boolean> asyncDelete(Bson filters) {
        return new Result<>(executorService, () -> delete(filters));
    }

    public Result<Boolean> asyncSave(T entity) {
        return new Result<>(executorService, () -> save(entity));
    }

    public Result<Boolean> asyncExists(Bson filters) {
        return new Result<>(executorService, () -> exists(filters));
    }

    public Result<T> asyncFind(Bson filters) {
        return new Result<>(executorService, () -> find(filters));
    }

    public Result<List<T>> asyncFindAll(Bson filters) {
        return new Result<>(executorService, () -> findAll(filters));
    }

    public Result<List<T>> asyncFindSort(Bson filters, Bson sort) {
        return new Result<>(executorService, () -> findSort(filters, sort));
    }

    public Result<List<T>> asyncFindLimit(Bson filters, int maxDocuments) {
        return new Result<>(executorService, () -> findLimit(filters, maxDocuments));
    }

    public Result<List<T>> asyncFindSortLimit(Bson filters, Bson sort, int maxDocuments) {
        return new Result<>(executorService, () -> findSortLimit(filters, sort, maxDocuments));
    }

    public Result<List<T>> asyncAll() {
        return new Result<>(executorService, this::all);
    }
}