package eu.koboo.en2do;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import eu.koboo.en2do.exception.*;
import eu.koboo.en2do.utility.MethodNameUtil;
import eu.koboo.en2do.utility.GenericUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RepoInvocation<E, ID> implements InvocationHandler {

    String entityCollectionName;
    MongoCollection<E> collection;
    Class<Repo<E, ID>> repoClass;
    Class<E> entityClass;
    Class<ID> entityUniqueIdClass;
    Field entityUniqueIdField;

    public RepoInvocation(String entityCollectionName, MongoCollection<E> collection,
                          Class<Repo<E, ID>> repoClass, Class<E> entityClass, Class<ID> entityUniqueIdClass,
                          Field entityUniqueIdField) {
        this.entityCollectionName = entityCollectionName;
        this.collection = collection;
        this.entityClass = entityClass;
        this.repoClass = repoClass;
        this.entityUniqueIdClass = entityUniqueIdClass;
        this.entityUniqueIdField = entityUniqueIdField;
    }

    @Override
    @SuppressWarnings("all")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (methodName.equalsIgnoreCase("getCollectionName")) {
            checkArguments(method, args, 0);
            return entityCollectionName;
        }
        if (methodName.equalsIgnoreCase("getUniqueId")) {
            checkArguments(method, args, 1);
            E entity = checkEntity(method, args[0]);
            return checkUniqueId(method, getUniqueId(entity));
        }
        if (methodName.equalsIgnoreCase("getClass")) {
            return repoClass;
        }
        if (methodName.equalsIgnoreCase("getEntityClass")) {
            return entityClass;
        }
        if (methodName.equalsIgnoreCase("getEntityUniqueIdClass")) {
            return entityUniqueIdClass;
        }
        if (methodName.equalsIgnoreCase("findById")) {
            checkArguments(method, args, 1);
            ID uniqueId = checkUniqueId(method, args[0]);
            Bson idFilter = createIdFilter(uniqueId);
            return collection.find(idFilter).first();
        }
        if (methodName.equalsIgnoreCase("findAll")) {
            checkArguments(method, args, 0);
            return collection.find().into(new ArrayList<>());
        }
        if (methodName.equalsIgnoreCase("delete")) {
            checkArguments(method, args, 1);
            E entity = checkEntity(method, args[0]);
            ID uniqueId = checkUniqueId(method, getUniqueId(entity));
            Bson idFilter = createIdFilter(uniqueId);
            DeleteResult result = collection.deleteOne(idFilter);
            return result.wasAcknowledged();
        }
        if (methodName.equalsIgnoreCase("deleteById")) {
            checkArguments(method, args, 1);
            ID uniqueId = checkUniqueId(method, args[0]);
            Bson idFilter = createIdFilter(uniqueId);
            DeleteResult result = collection.deleteOne(idFilter);
            return result.wasAcknowledged();
        }
        if (methodName.equalsIgnoreCase("deleteAll")) {
            checkArguments(method, args, 0);
            collection.drop();
            return true;
        }
        if (methodName.equalsIgnoreCase("save")) {
            checkArguments(method, args, 1);
            E entity = checkEntity(method, args[0]);
            ID uniqueId = checkUniqueId(method, getUniqueId(entity));
            Bson idFilter = createIdFilter(uniqueId);
            if (createIterable(idFilter).first() != null) {
                UpdateResult result = collection.replaceOne(idFilter, entity, new ReplaceOptions().upsert(true));
                return result.wasAcknowledged();
            }
            collection.insertOne(entity);
            return true;
        }
        if (methodName.equalsIgnoreCase("exists")) {
            checkArguments(method, args, 1);
            E entity = checkEntity(method, args[0]);
            ID uniqueId = checkUniqueId(method, getUniqueId(entity));
            Bson idFilter = createIdFilter(uniqueId);
            return collection.find(idFilter).first() != null;
        }
        if (methodName.equalsIgnoreCase("existsById")) {
            checkArguments(method, args, 1);
            ID uniqueId = checkUniqueId(method, args[0]);
            Bson idFilter = createIdFilter(uniqueId);
            return collection.find(idFilter).first() != null;
        }
        if(methodName.startsWith("findBy") || methodName.startsWith("deleteBy")) {
            Class<?> returnTypeClass = method.getReturnType();
            String fieldFilterName = MethodNameUtil.removeLeadingOperator(methodName);
            if (fieldFilterName == null) {
                throw new InvalidMethodOperationException(method, entityClass);
            }
            Bson filter = null;
            if(fieldFilterName.contains("And")) {
                List<Bson> filterList = new ArrayList<>();
                String[] fieldFilterPart = fieldFilterName.split("And");
                for(int i = 0; i < fieldFilterPart.length; i++) {
                    String fieldFilterPartIndexed = fieldFilterPart[i];
                    filterList.add(createBsonFilter(method, args, fieldFilterPartIndexed, i));
                }
                filter = Filters.and(filterList);
            } else if(fieldFilterName.contains("Or")) {
                List<Bson> filterList = new ArrayList<>();
                String[] fieldFilterPart = fieldFilterName.split("Or");
                for(int i = 0; i < fieldFilterPart.length; i++) {
                    String fieldFilterPartIndexed = fieldFilterPart[i];
                    filterList.add(createBsonFilter(method, args, fieldFilterPartIndexed, i));
                }
                filter = Filters.or(filterList);
            } else {
                filter = createBsonFilter(method, args, fieldFilterName, 0);
            }
            if (GenericUtils.isTypeOf(List.class, returnTypeClass)) { // Find with List<Entity>
                return collection.find(filter).into(new ArrayList<>());
            }
            if (GenericUtils.isTypeOf(entityClass, returnTypeClass)) { // Find with Entity
                return collection.find(filter).first();
            }
            if (GenericUtils.isTypeOf(Boolean.class, returnTypeClass)) { // Delete with Entity
                DeleteResult deleteResult = collection.deleteOne(filter);
                return deleteResult.wasAcknowledged();
            }
        }
        throw new InvalidMethodCallException(method, entityClass);
    }

    private void checkArguments(Method method, Object[] args, int expectedLength) {
        if(args == null && expectedLength == 0) {
            return;
        }
        if (args == null || args.length != expectedLength) {
            throw new IllegalArgumentException("argument length of method " + method.getName() + " from " +
                    entityClass.getName() + " not matching! (" +
                    "expected=" + expectedLength + ", " +
                    "length=" + (args == null ? "null" : args.length) +
                    ")");
        }
    }

    private E checkEntity(Method method, Object argument) {
        E entity = entityClass.cast(argument);
        if (entity == null) {
            throw new NullPointerException("entity argument of method " + method.getName() + " from " +
                    entityClass.getName() + " is null.");
        }
        return entity;
    }

    private ID checkUniqueId(Method method, Object argument) {
        ID uniqueId = entityUniqueIdClass.cast(argument);
        if (uniqueId == null) {
            throw new NullPointerException("uniqueId argument of method " + method.getName() + " from " +
                    entityClass.getName() + " is null.");
        }
        return uniqueId;
    }

    private ID getUniqueId(E entity) throws IllegalAccessException {
        return entityUniqueIdClass.cast(entityUniqueIdField.get(entity));
    }

    private Bson createIdFilter(ID uniqueId) {
        return Filters.eq(entityUniqueIdField.getName(), uniqueId);
    }

    private FindIterable<E> createIterable(Bson filter) {
        return collection.find(filter);
    }


    private Bson createBsonFilter(Method method, Object[] params, String methodFilterPart, int paramIndex) throws Exception {
        // NameEqualsIgnoreCase (String name);
        // NumberGreaterThan (String name, Double number);
        String expectedField = MethodNameUtil.replaceEndingFilter(methodFilterPart);
        if (expectedField == null) {
            throw new NoFilterException(method, entityClass);
        }
        expectedField = expectedField.endsWith("Not") ? expectedField.replaceFirst("Not", "") : expectedField;
        Object objectParameter = params[paramIndex];
        if(objectParameter == null) {
            throw new MissingParameterException(method, entityClass);
        }
        String endingFilter = methodFilterPart.replaceFirst(expectedField, "");
        if(endingFilter.equalsIgnoreCase("")) {
            throw new NoFilterException(method, entityClass);
        }
        // Name
        for (Field field : entityClass.getDeclaredFields()) {
            if (!field.getName().equalsIgnoreCase(expectedField)) {
                continue;
            }
            Bson filter = matchBsonByFilterName(method, endingFilter, field.getName(), objectParameter);
            if(endingFilter.startsWith("Not")) {
                return Filters.not(filter);
            }
            return filter;
        }
        return null;
    }

    private Bson matchBsonByFilterName(Method method, String filter, String fieldName, Object value) throws Exception {
        filter = filter.startsWith("Not") ? filter.replaceFirst("Not", "") : filter;
        if(filter.equalsIgnoreCase("Equals")) {
            return Filters.eq(fieldName, value);
        }
        if(filter.equalsIgnoreCase("EqualsIgnoreCase")) {
            String patternString = "(?i)^" + value + "$";
            Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
            return Filters.regex(fieldName, pattern);
        }
        if(filter.equalsIgnoreCase("GreaterThan")) {
            return Filters.gt(fieldName, value);
        }
        if(filter.equalsIgnoreCase("LessThan")) {
            return Filters.lt(fieldName, value);
        }
        if(filter.equalsIgnoreCase("Has")) {
            return Filters.exists(fieldName);
        }
        if(filter.equalsIgnoreCase("Regex")) {
            if(value instanceof String patternString) {
                return Filters.regex(fieldName, patternString);
            }
            if(value instanceof Pattern pattern) {
                return Filters.regex(fieldName, pattern);
            }
            throw new InvalidRegexParameterException(method, entityClass);
        }
        if(filter.equalsIgnoreCase("GreaterEquals")) {
            return Filters.gte(fieldName, value);
        }
        if(filter.equalsIgnoreCase("LessEquals")) {
            return Filters.lte(fieldName, value);
        }
        throw new UnsupportedFilterException(method, entityClass);
    }
}