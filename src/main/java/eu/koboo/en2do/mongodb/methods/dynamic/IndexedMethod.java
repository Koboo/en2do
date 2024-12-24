package eu.koboo.en2do.mongodb.methods.dynamic;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.exception.methods.MethodInvalidRegexParameterException;
import eu.koboo.en2do.mongodb.exception.methods.MethodUnsupportedFilterException;
import eu.koboo.en2do.operators.Chain;
import eu.koboo.en2do.operators.MethodOperator;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.geo.Geo;
import eu.koboo.en2do.repository.methods.geo.GeoType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.*;
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

    RepositoryData<E, ID, R> repositoryData;

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
            case NONE:
            default:
                filter = bsonFilterSet.stream()
                    .findFirst()
                    .orElse(null);
                break;
        }
        return (F) filter;
    }

    @SuppressWarnings("unchecked")
    private Bson processBson(IndexedFilter filter, int paramsIndexAt,
                             Object[] args) throws Exception {
        String bsonFilterFieldKey = filter.getBsonFilterFieldKey();

        // Check if the id field of the entity is used.
        // We need to convert the actual field name to mongodb's "_id" of documents
        // to be able to filter the id field.
        if (bsonFilterFieldKey.equalsIgnoreCase(repositoryData.getEntityUniqueIdField().getName())) {
            bsonFilterFieldKey = "_id";
        }

        Bson retFilter = null;
        switch (filter.getOperator()) {
            case EQUALS:
                retFilter = Filters.eq(bsonFilterFieldKey, repositoryData.getFilterableValue(args[paramsIndexAt]));
                break;
            case EQUALS_IGNORE_CASE:
                String ignCasePatternString = "(?i)^" + repositoryData.getFilterableValue(args[paramsIndexAt]) + "$";
                Pattern ignCasePattern = Pattern.compile(ignCasePatternString, Pattern.CASE_INSENSITIVE);
                retFilter = Filters.regex(bsonFilterFieldKey, ignCasePattern);
                break;
            case CONTAINS:
                String containsPatternString = ".*" + repositoryData.getFilterableValue(args[paramsIndexAt]) + ".*";
                Pattern containsPattern = Pattern.compile(containsPatternString, Pattern.CASE_INSENSITIVE);
                retFilter = Filters.regex(bsonFilterFieldKey, containsPattern);
                break;
            case GREATER_THAN:
                retFilter = Filters.gt(bsonFilterFieldKey, repositoryData.getFilterableValue(args[paramsIndexAt]));
                break;
            case LESS_THAN:
                retFilter = Filters.lt(bsonFilterFieldKey, repositoryData.getFilterableValue(args[paramsIndexAt]));
                break;
            case GREATER_EQUALS:
                retFilter = Filters.gte(bsonFilterFieldKey, repositoryData.getFilterableValue(args[paramsIndexAt]));
                break;
            case LESS_EQUALS:
                retFilter = Filters.lte(bsonFilterFieldKey, repositoryData.getFilterableValue(args[paramsIndexAt]));
                break;
            case REGEX:
                // MongoDB supports multiple types of regex filtering, so check which type is provided.
                // We support plain Strings and Pattern types.
                Object value = repositoryData.getFilterableValue(args[paramsIndexAt]);
                if (value instanceof String) {
                    String regexPatternString = (String) value;
                    retFilter = Filters.regex(bsonFilterFieldKey, regexPatternString);
                }
                if (value instanceof Pattern) {
                    Pattern regexPattern = (Pattern) value;
                    retFilter = Filters.regex(bsonFilterFieldKey, regexPattern);
                }
                // No supported type provided, we can't do anything other than throwing exceptions.
                if (retFilter == null) {
                    throw new MethodInvalidRegexParameterException(
                        method,
                        repositoryData.getRepositoryClass(),
                        value.getClass()
                    );
                }
                break;
            case EXISTS:
                retFilter = Filters.exists(bsonFilterFieldKey);
                break;
            case BETWEEN:
                Object betweenStart = repositoryData.getFilterableValue(args[paramsIndexAt]);
                Object betweenEnd = args[paramsIndexAt + 1];
                retFilter = Filters.and(
                    Filters.gt(bsonFilterFieldKey, betweenStart), Filters.lt(bsonFilterFieldKey, betweenEnd)
                );
                break;
            case BETWEEN_EQUALS:
                Object betweenEqStart = repositoryData.getFilterableValue(args[paramsIndexAt]);
                Object betweenEqEnd = args[paramsIndexAt + 1];
                retFilter = Filters.and(
                    Filters.gte(bsonFilterFieldKey, betweenEqStart), Filters.lte(bsonFilterFieldKey, betweenEqEnd)
                );
                break;
            case IN:
                // MongoDB expects an Array in the Filter as argument. But for easier usage
                // we want to support lists and arrays, so let's just convert the given object to an array
                // no matter what type it is and pass it on to the filter parameters.
                Object possibleObject = args[paramsIndexAt];
                Object[] objectArray = null;
                if (possibleObject instanceof Collection) {
                    Collection<Object> collection = (Collection<Object>) possibleObject;
                    objectArray = collection.toArray(new Object[]{});
                }
                if (possibleObject.getClass().isArray() && possibleObject instanceof Object[]) {
                    objectArray = (Object[]) possibleObject;
                }
                // If you produce this exception, I'm very surprised then I see the code.
                if (objectArray == null) {
                    throw new NullPointerException("Your Object array of the \"In\" filter was null.\n" +
                        "This is a very rare case, since we do stuff, mongodb doesn't really support.\n" +
                        "Please report your code and other information to\n" +
                        "https://github.com/Koboo/en2do\n" +
                        "to ensure others don't get this bug and we can't look further into this issue.");
                }
                retFilter = Filters.in(bsonFilterFieldKey, objectArray);
                break;
            case HAS_KEY:
                // We need a separate getFilterableValue here, because we "faked" UUIDs map keys as strings
                // in the codec. That's why we need the explicit "true" parameter here.
                Object keyObject = repositoryData.getFilterableValue(args[paramsIndexAt], true);
                retFilter = Filters.exists(bsonFilterFieldKey + "." + keyObject);
                break;
            case HAS:
                Object hasObject = repositoryData.getFilterableValue(args[paramsIndexAt]);
                retFilter = Filters.in(bsonFilterFieldKey, hasObject);
                break;
            case GEO:
                // Do not ask about the geo filters.
                Geo geo = (Geo) args[paramsIndexAt];
                switch (geo.getType()) {
                    case NEAR:
                    case NEAR_SPHERE:
                        Point point = new Point(new Position(geo.getLongitude(), geo.getLatitude()));
                        if (geo.getType() == GeoType.NEAR) {
                            retFilter = Filters.near(bsonFilterFieldKey, point, geo.getMaxDistance(), geo.getMinDistance());
                        } else {
                            retFilter = Filters.nearSphere(bsonFilterFieldKey, point, geo.getMaxDistance(), geo.getMinDistance());
                        }
                        break;
                    default:
                        throw new RuntimeException("Only near and near sphere are currently supported!");
                }
                break;
            case IS_NULL:
                retFilter = Filters.eq(bsonFilterFieldKey, null);
                break;
            case NON_NULL:
                retFilter = Filters.not(Filters.eq(bsonFilterFieldKey, null));
                break;
            case IS_TRUE:
                retFilter = Filters.eq(bsonFilterFieldKey, true);
                break;
            case IS_FALSE:
                retFilter = Filters.eq(bsonFilterFieldKey, false);
                break;
            case LIST_EMPTY:
                // The thing with "IsListEmpty" check is the following:
                // We need to check multiple conditions in a specific order.
                //   1. Does the field exists?
                //   2. Is the field of type array?
                //   3. Is the value of the field an empty array?
                // There's also a second approach with type equals check,
                // but some tests are not working with that,
                // so lets just create and use the empty list here.
                retFilter = Filters.and(
                    Filters.exists(bsonFilterFieldKey, true),
                    Filters.type(bsonFilterFieldKey, "array"),
                    Filters.ne(bsonFilterFieldKey, new ArrayList<Document>())
                );
                break;
            default: // This filter is not supported. Throw exception.
                throw new MethodUnsupportedFilterException(method, repositoryData.getRepositoryClass());
        }

        // Applying negotiating of the filter, if needed
        if (filter.isNotFilter()) {
            return Filters.nor(retFilter);
        }
        return retFilter;
    }
}