package eu.koboo.en2do;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import eu.koboo.en2do.exception.*;
import eu.koboo.en2do.methods.FilterType;
import eu.koboo.en2do.methods.MethodOperator;
import eu.koboo.en2do.methods.registry.MethodHandler;
import eu.koboo.en2do.methods.registry.RepositoryMeta;
import eu.koboo.en2do.sort.annotation.Limit;
import eu.koboo.en2do.sort.annotation.Skip;
import eu.koboo.en2do.sort.annotation.SortBy;
import eu.koboo.en2do.sort.object.ByField;
import eu.koboo.en2do.sort.object.Sort;
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
import java.util.Set;
import java.util.regex.Pattern;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class RepositoryInvocationHandler<E, ID, R extends Repository<E, ID>> implements InvocationHandler {

    MongoManager manager;
    RepositoryMeta<E, ID, R> repositoryMeta;
    String entityCollectionName;
    MongoCollection<E> collection;
    Class<R> repoClass;
    Class<E> entityClass;
    Set<Field> entityFieldSet;
    Class<ID> entityUniqueIdClass;
    Field entityUniqueIdField;

    @Override
    @SuppressWarnings("all")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        MethodHandler<E> methodHandler = repositoryMeta.lookupHandler(methodName);
        if(methodHandler != null) {
            return methodHandler.handle(method, args);
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
                FilterType filterType = manager.createFilterType(entityClass, repoClass, method, operatorString, entityFieldSet);
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
            FilterType filterType = manager.createFilterType(entityClass, repoClass, method, operatorRootString, entityFieldSet);
            boolean isNot = operatorRootString.toLowerCase(Locale.ROOT)
                    .replaceFirst(filterType.field().getName().toLowerCase(Locale.ROOT), "").startsWith("not");
            filter = createBsonFilter(method, filterType, isNot, 0, args);
        }

        switch (methodOperator) {
            case FIND_FIRST -> {
                FindIterable<E> findIterable = collection.find(filter);
                findIterable = applySortObject(method, findIterable, args);
                findIterable = applySortAnnotations(method, findIterable);
                return findIterable.limit(1).first();
            }
            case FIND_MANY -> {
                FindIterable<E> findIterable = collection.find(filter);
                findIterable = applySortObject(method, findIterable, args);
                findIterable = applySortAnnotations(method, findIterable);
                return findIterable.into(new ArrayList<>());
            }
            case DELETE -> {
                DeleteResult deleteResult = collection.deleteMany(filter);
                return deleteResult.wasAcknowledged();
            }
            case EXISTS -> {
                return collection.countDocuments(filter) > 0;
            }
            case COUNT -> {
                return collection.countDocuments(filter);
            }
        }
        throw new RepositoryInvalidCallException(method, repoClass);
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

    private FindIterable<E> applySortObject(Method method, FindIterable<E> findIterable, Object[] args) {
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
        if (sortAnnotations != null && sortAnnotations.length > 0) {
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
        String fieldName = filterType.field().getName();
        Bson retFilter = null;
        switch (filterType.operator()) {
            case EQUALS -> {
                retFilter = Filters.eq(fieldName, getFilterableValue(args[paramsIndexAt]));
            }
            case EQUALS_IGNORE_CASE -> {
                String patternString = "(?i)^" + getFilterableValue(args[paramsIndexAt]) + "$";
                Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
                retFilter = Filters.regex(fieldName, pattern);
            }
            case CONTAINS -> {
                String patternString = ".*" + getFilterableValue(args[paramsIndexAt]) + ".*";
                Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
                retFilter = Filters.regex(fieldName, pattern);
            }
            case GREATER_THAN -> {
                retFilter = Filters.gt(fieldName, getFilterableValue(args[paramsIndexAt]));
            }
            case LESS_THAN -> {
                retFilter = Filters.lt(fieldName, getFilterableValue(args[paramsIndexAt]));
            }
            case GREATER_EQUALS -> {
                retFilter = Filters.gte(fieldName, getFilterableValue(args[paramsIndexAt]));
            }
            case LESS_EQUALS -> {
                retFilter = Filters.lte(fieldName, getFilterableValue(args[paramsIndexAt]));
            }
            case REGEX -> {
                // MongoDB supports multiple types of regex filtering, so check which type is provided.
                Object value = getFilterableValue(args[paramsIndexAt]);
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
                Object from = getFilterableValue(args[paramsIndexAt]);
                Object to = args[paramsIndexAt + 1];
                retFilter = Filters.and(Filters.gt(fieldName, from), Filters.lt(fieldName, to));
            }
            case BETWEEN_EQUALS -> {
                Object from = getFilterableValue(args[paramsIndexAt]);
                Object to = args[paramsIndexAt + 1];
                retFilter = Filters.and(Filters.gte(fieldName, from), Filters.lte(fieldName, to));
            }
            case IN -> {
                // MongoDB expects a Array and not a List, but for easier usage
                // the framework wants a list. So just convert the list to an array and pass it to the filter
                List<Object> objectList = (List<Object>) getFilterableValue(args[paramsIndexAt]);
                Object[] objectArray = objectList.toArray(new Object[]{});
                retFilter = Filters.in(fieldName, objectArray);
            }
            default -> {
                // This filter is not supported. Throw exception.
                throw new MethodUnsupportedFilterException(method, repoClass);
            }
        }
        // Applying negotiating of the filter, if needed
        if (isNot) {
            return Filters.not(retFilter);
        }
        return retFilter;
    }

    private Object getFilterableValue(Object object) {
        if (object instanceof Enum<?>) {
            return ((Enum<?>) object).name();
        }
        return object;
    }
}
