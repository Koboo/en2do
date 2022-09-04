package eu.koboo.en2do;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import eu.koboo.en2do.repository.exception.*;
import eu.koboo.en2do.sort.ByField;
import eu.koboo.en2do.sort.Sort;
import eu.koboo.en2do.sort.annotation.Limit;
import eu.koboo.en2do.sort.annotation.Skip;
import eu.koboo.en2do.sort.annotation.SortBy;
import eu.koboo.en2do.repository.FilterType;
import eu.koboo.en2do.repository.MethodOperator;
import eu.koboo.en2do.utility.GenericUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class RepositoryInvocationHandler<E, ID> implements InvocationHandler {

    RepositoryFactory factory;
    String entityCollectionName;
    MongoCollection<E> collection;
    Class<Repository<E, ID>> repoClass;
    Class<E> entityClass;
    Class<ID> entityUniqueIdClass;
    Field entityUniqueIdField;

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
            checkArguments(method, args, 1);
            List<E> entityList = checkEntityList(method, args[0]);
            for (E entity : entityList) {
                checkArguments(method, args, 1);
                ID uniqueId = checkUniqueId(method, getUniqueId(entity));
                Bson idFilter = createIdFilter(uniqueId);
                DeleteResult result = collection.deleteOne(idFilter);
            }
            return true;
        }
        if (methodName.equalsIgnoreCase("drop")) {
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
        if (methodName.equalsIgnoreCase("saveAll")) {
            checkArguments(method, args, 1);
            List<E> entityList = checkEntityList(method, args[0]);
            for (E entity : entityList) {
                ID uniqueId = checkUniqueId(method, getUniqueId(entity));
                Bson idFilter = createIdFilter(uniqueId);
                if (createIterable(idFilter).first() != null) {
                    UpdateResult result = collection.replaceOne(idFilter, entity, new ReplaceOptions().upsert(true));
                    return result.wasAcknowledged();
                }
                collection.insertOne(entity);
            }
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

        // Start of the dynamic methods

        MethodOperator methodOperator = MethodOperator.parseMethodStartsWith(methodName);
        if (methodOperator == null) {
            throw new MethodNoMethodOperatorException(method, repoClass);
        }
        String operatorRootString = methodOperator.removeOperatorFrom(methodName);
        if (operatorRootString == null) {
            throw new MethodInvalidSignatureException(method, entityClass);
        }

        // Bson filter conversion from method name
        Bson filter = null;
        if (operatorRootString.contains("And") || operatorRootString.contains("Or")) {
            List<Bson> filterList = new ArrayList<>();
            String[] operatorStringArray = operatorRootString.contains("And") ?
                    operatorRootString.split("And") : operatorRootString.split("Or");
            int nextIndex = 0;
            for (int i = 0; i < operatorStringArray.length; i++) {
                String operatorString = operatorStringArray[i];
                FilterType filterType = factory.createFilterType(entityClass, repoClass, method, operatorString);
                boolean isNot = operatorString.replaceFirst(filterType.field().getName(), "").startsWith("Not");
                filterList.add(createBsonFilter(method, filterType, isNot, nextIndex, args));
                nextIndex = i + filterType.operator().getExpectedParameterCount();
            }
            if (operatorRootString.contains("And")) {
                filter = Filters.and(filterList);
            } else {
                filter = Filters.or(filterList);
            }
        } else {
            FilterType filterType = factory.createFilterType(entityClass, repoClass, method, operatorRootString);
            boolean isNot = operatorRootString.toLowerCase(Locale.ROOT)
                    .replaceFirst(filterType.field().getName().toLowerCase(Locale.ROOT), "").startsWith("not");
            filter = createBsonFilter(method, filterType, isNot, 0, args);
        }
        Debugger.print("BsonFilter: " + filter);

        Class<?> returnTypeClass = method.getReturnType();
        if (methodOperator == MethodOperator.FIND) {
            if (GenericUtils.isTypeOf(List.class, returnTypeClass)) {
                FindIterable<E> findIterable = collection.find(filter);
                findIterable = applySortObject(method, findIterable, args);
                findIterable = applySortAnnotations(method, findIterable);
                return findIterable.into(new ArrayList<>());
            }
            if (GenericUtils.isTypeOf(entityClass, returnTypeClass)) {
                FindIterable<E> findIterable = collection.find(filter);
                findIterable = applySortObject(method, findIterable, args);
                findIterable = applySortAnnotations(method, findIterable);
                return findIterable.first();
            }
        }
        if (methodOperator == MethodOperator.DELETE) {
            if (GenericUtils.isTypeOf(Boolean.class, returnTypeClass)) {
                DeleteResult deleteResult = collection.deleteMany(filter);
                return deleteResult.wasAcknowledged();
            }
        }
        if (methodOperator == MethodOperator.EXISTS) {
            if (GenericUtils.isTypeOf(Boolean.class, returnTypeClass)) {
                FindIterable<E> findIterable = collection.find(filter);
                findIterable = applySortAnnotations(method, findIterable);
                E entity = findIterable.first();
                return entity != null;
            }
        }
        if (methodOperator == MethodOperator.COUNT) {
            if (GenericUtils.isTypeOf(Long.class, returnTypeClass)) {
                return collection.countDocuments(filter);
            }
        }
        throw new RepositoryInvalidCallException(method, repoClass);
    }

    private void checkArguments(Method method, Object[] args, int expectedLength) throws Exception {
        if (args == null && expectedLength == 0) {
            return;
        }
        if (args == null || args.length != expectedLength) {
            int length = args == null ? -1 : args.length;
            throw new MethodParameterCountException(method, repoClass, expectedLength, length);
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

    @SuppressWarnings("all")
    private List<E> checkEntityList(Method method, Object argument) {
        List<E> entity = (List<E>) argument;
        if (entity == null) {
            throw new NullPointerException("entityList argument of method " + method.getName() + " from " +
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

    private FindIterable<E> applySortObject(Method method, FindIterable<E> findIterable, Object[] args) {
        int parameterCount = method.getParameterCount();
        if(parameterCount <= 0) {
            return findIterable;
        }
        Class<?> lastParam = method.getParameterTypes()[method.getParameterCount() - 1];
        if (!lastParam.isAssignableFrom(Sort.class)) {
            return findIterable;
        }
        Object lastParamObject = args == null ? null : args[args.length - 1];
        if (!(lastParamObject instanceof Sort sortOptions)) {
            return findIterable;
        }
        if (!sortOptions.getByFieldList().isEmpty()) {
            for (ByField byField : sortOptions.getByFieldList()) {
                int orderType = byField.ascending() ? 1 : -1;
                findIterable = findIterable.sort(new BasicDBObject(byField.fieldName(), orderType));
            }
        }
        if (sortOptions.getLimit() != -1) {
            findIterable = findIterable.limit(sortOptions.getLimit());
        }
        if (sortOptions.getSkip() != -1) {
            findIterable = findIterable.skip(sortOptions.getSkip());
        }
        return findIterable;
    }

    private FindIterable<E> applySortAnnotations(Method method, FindIterable<E> findIterable) {
        SortBy[] sortAnnotations = method.getAnnotationsByType(SortBy.class);
        if(sortAnnotations != null && sortAnnotations.length > 0) {
            for (SortBy sortBy : sortAnnotations) {
                int orderType = sortBy.ascending() ? 1 : -1;
                findIterable = findIterable.sort(new BasicDBObject(sortBy.field(), orderType));
            }
        }
        if (method.isAnnotationPresent(Limit.class)) {
            Limit limit = method.getAnnotation(Limit.class);
            findIterable = findIterable.limit(limit.value());
        }
        if (method.isAnnotationPresent(Skip.class)) {
            Skip skip = method.getAnnotation(Skip.class);
            findIterable = findIterable.skip(skip.value());
        }
        return findIterable;
    }

    @SuppressWarnings("all")
    private Bson createBsonFilter(Method method, FilterType filterType, boolean isNot, int paramsIndexAt, Object[] args) throws Exception {
        // NameEqualsIgnoreCase (String name);
        // NumberGreaterThan (String name, Double number);
        String fieldName = filterType.field().getName();
        Bson retFilter = null;
        switch (filterType.operator()) {
            case EQUALS -> {
                retFilter = Filters.eq(fieldName, args[paramsIndexAt]);
            }
            case EQUALS_IGNORE_CASE -> {
                String patternString = "(?i)^" + args[paramsIndexAt] + "$";
                Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
                retFilter = Filters.regex(fieldName, pattern);
            }
            case CONTAINS -> {
                String patternString = ".*" + args[paramsIndexAt] + ".*";
                Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
                retFilter = Filters.regex(fieldName, pattern);
            }
            case GREATER_THAN -> {
                retFilter = Filters.gt(fieldName, args[paramsIndexAt]);
            }
            case LESS_THAN -> {
                retFilter = Filters.lt(fieldName, args[paramsIndexAt]);
            }
            case GREATER_EQUALS -> {
                retFilter = Filters.gte(fieldName, args[paramsIndexAt]);
            }
            case LESS_EQUALS -> {
                retFilter = Filters.lte(fieldName, args[paramsIndexAt]);
            }
            case REGEX -> {
                Object value = args[paramsIndexAt];
                if (value instanceof String patternString) {
                    retFilter = Filters.regex(fieldName, patternString);
                }
                if (value instanceof Pattern pattern) {
                    retFilter = Filters.regex(fieldName, pattern);
                }
                if (retFilter == null) {
                    throw new MethodInvalidRegexParameterException(method, repoClass, value.getClass());
                }
            }
            case EXISTS -> {
                retFilter = Filters.exists(fieldName);
            }
            case BETWEEN -> {
                Object from = args[paramsIndexAt];
                Object to = args[paramsIndexAt + 1];
                retFilter = Filters.and(Filters.gt(fieldName, from), Filters.lt(fieldName, to));
            }
            case BETWEEN_EQUALS -> {
                Object from = args[paramsIndexAt];
                Object to = args[paramsIndexAt + 1];
                retFilter = Filters.and(Filters.gte(fieldName, from), Filters.lte(fieldName, to));
            }
            case IN -> {
                // MongoDB expects a Array and not a List, but for easier usage
                // the framework wants a list. So just convert the list to an array and pass it to the filter
                List<Object> objectList = (List<Object>) args[paramsIndexAt];
                Object[] objectArray = objectList.toArray(new Object[]{});
                retFilter = Filters.in(fieldName, objectArray);
            }
            default -> {
                throw new MethodUnsupportedFilterException(method, repoClass);
            }
        }
        if (isNot) {
            return Filters.not(retFilter);
        }
        return retFilter;
    }
}