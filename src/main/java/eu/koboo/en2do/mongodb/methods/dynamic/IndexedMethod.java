package eu.koboo.en2do.mongodb.methods.dynamic;

import com.mongodb.client.model.Filters;
import eu.koboo.en2do.operators.Chain;
import eu.koboo.en2do.operators.MethodOperator;
import eu.koboo.en2do.mongodb.RepositoryMeta;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidRegexParameterException;
import eu.koboo.en2do.mongodb.exception.methods.MethodUnsupportedFilterException;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IndexedMethod<E, ID, R extends Repository<E, ID>> {

    @Getter
    Method method;
    @Getter
    MethodOperator methodOperator;
    Chain chain;

    @Getter
    Long methodDefinedEntityCount;

    List<IndexedFilter> indexedFilterList;

    RepositoryMeta<E, ID, R> repositoryMeta;

    @SuppressWarnings("unchecked")
    public <F> F createFilter(Object[] arguments) throws Exception {
        Bson filter;
        Set<Bson> bsonFilterSet = new LinkedHashSet<>();
        for (IndexedFilter indexedFilter : indexedFilterList) {
            int paramStartIndex = indexedFilter.getNextParameterIndex();
            Bson processedBsonFilter = processBson(indexedFilter, paramStartIndex, arguments);
            bsonFilterSet.add(processedBsonFilter);
        }
        switch (chain) {
            case OR:
                filter = Filters.or(bsonFilterSet);
                break;
            case AND:
                filter = Filters.and(bsonFilterSet);
                break;
            default:
            case NONE:
                filter = bsonFilterSet.stream().findFirst().orElse(null);
                break;
        }
        return (F) filter;
    }

    @SuppressWarnings("unchecked")
    private Bson processBson(IndexedFilter filter, int paramsIndexAt,
                             Object[] args) throws Exception {
        String queryFieldName = filter.getBsonName();
        // Check if the uniqueId field is used.
        // This is needed if uniqueId field and "_id" of documents are the same!
        if (queryFieldName.equalsIgnoreCase(repositoryMeta.getEntityUniqueIdField().getName())) {
            queryFieldName = "_id";
        }
        Bson retFilter = null;
        switch (filter.getOperator()) {
            case EQUALS:
                retFilter = Filters.eq(queryFieldName, repositoryMeta.getFilterableValue(args[paramsIndexAt]));
                break;
            case EQUALS_IGNORE_CASE:
                String ignCasePatternString = "(?i)^" + repositoryMeta.getFilterableValue(args[paramsIndexAt]) + "$";
                Pattern ignCasePattern = Pattern.compile(ignCasePatternString, Pattern.CASE_INSENSITIVE);
                retFilter = Filters.regex(queryFieldName, ignCasePattern);
                break;
            case CONTAINS:
                String containsPatternString = ".*" + repositoryMeta.getFilterableValue(args[paramsIndexAt]) + ".*";
                Pattern containsPattern = Pattern.compile(containsPatternString, Pattern.CASE_INSENSITIVE);
                retFilter = Filters.regex(queryFieldName, containsPattern);
                break;
            case GREATER_THAN:
                retFilter = Filters.gt(queryFieldName, repositoryMeta.getFilterableValue(args[paramsIndexAt]));
                break;
            case LESS_THAN:
                retFilter = Filters.lt(queryFieldName, repositoryMeta.getFilterableValue(args[paramsIndexAt]));
                break;
            case GREATER_EQUALS:
                retFilter = Filters.gte(queryFieldName, repositoryMeta.getFilterableValue(args[paramsIndexAt]));
                break;
            case LESS_EQUALS:
                retFilter = Filters.lte(queryFieldName, repositoryMeta.getFilterableValue(args[paramsIndexAt]));
                break;
            case REGEX:
                // MongoDB supports multiple types of regex filtering, so check which type is provided.
                Object value = repositoryMeta.getFilterableValue(args[paramsIndexAt]);
                if (value instanceof String) {
                    String regexPatternString = (String) value;
                    retFilter = Filters.regex(queryFieldName, regexPatternString);
                }
                if (value instanceof Pattern) {
                    Pattern regexPattern = (Pattern) value;
                    retFilter = Filters.regex(queryFieldName, regexPattern);
                }
                if (retFilter == null) {
                    throw new MethodInvalidRegexParameterException(method, repositoryMeta.getRepositoryClass(), value.getClass());
                }
                break;
            case EXISTS:
                retFilter = Filters.exists(queryFieldName);
                break;
            case BETWEEN:
                Object betweenStart = repositoryMeta.getFilterableValue(args[paramsIndexAt]);
                Object betweenEnd = args[paramsIndexAt + 1];
                retFilter = Filters.and(Filters.gt(queryFieldName, betweenStart), Filters.lt(queryFieldName, betweenEnd));
                break;
            case BETWEEN_EQUALS:
                Object betweenEqStart = repositoryMeta.getFilterableValue(args[paramsIndexAt]);
                Object betweenEqEnd = args[paramsIndexAt + 1];
                retFilter = Filters.and(Filters.gte(queryFieldName, betweenEqStart), Filters.lte(queryFieldName, betweenEqEnd));
                break;
            case IN:
                // MongoDB expects an Array and not a List, but for easier usage
                // the framework wants a list or an array, so just convert the given object to an array
                // and add it to the filter
                Object possibleObject = args[paramsIndexAt];
                Object[] objectArray = null;
                if (possibleObject instanceof Collection) {
                    Collection<Object> collection = (Collection<Object>) possibleObject;
                    objectArray = collection.toArray(new Object[]{});
                }
                if (possibleObject.getClass().isArray() && possibleObject instanceof Object[]) {
                    objectArray = (Object[]) possibleObject;
                }
                if (objectArray == null) {
                    throw new NullPointerException("Please report your code and other information to " +
                        "https://github.com/Koboo/en2do to ensure others don't get this bug.");
                }
                retFilter = Filters.in(queryFieldName, objectArray);
                break;
            case HAS_KEY:
                Object keyObject = repositoryMeta.getFilterableValue(args[paramsIndexAt], true);
                retFilter = Filters.exists(queryFieldName + "." + keyObject);
                break;
            case HAS:
                Object hasObject = repositoryMeta.getFilterableValue(args[paramsIndexAt]);
                retFilter = Filters.in(queryFieldName, hasObject);
                break;
            case IS_NULL:
                retFilter = Filters.eq(queryFieldName, null);
                break;
            case NON_NULL:
                retFilter = Filters.not(Filters.eq(queryFieldName, null));
                break;
            case IS_TRUE:
                retFilter = Filters.eq(queryFieldName, true);
                break;
            case IS_FALSE:
                retFilter = Filters.eq(queryFieldName, false);
                break;
            default: // This filter is not supported. Throw exception.
                throw new MethodUnsupportedFilterException(method, repositoryMeta.getRepositoryClass());
        }
        // Applying negotiating of the filter, if needed
        if (filter.isNotFilter()) {
            return Filters.not(retFilter);
        }
        return retFilter;
    }
}