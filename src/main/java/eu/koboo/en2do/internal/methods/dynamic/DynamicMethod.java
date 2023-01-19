package eu.koboo.en2do.internal.methods.dynamic;

import com.mongodb.client.model.Filters;
import eu.koboo.en2do.internal.exception.methods.MethodInvalidRegexParameterException;
import eu.koboo.en2do.internal.exception.methods.MethodUnsupportedFilterException;
import eu.koboo.en2do.internal.methods.operators.MethodOperator;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.internal.RepositoryMeta;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DynamicMethod<E, ID, R extends Repository<E, ID>> {

    Method method;
    RepositoryMeta<E, ID, R> repositoryMeta;
    @Getter
    MethodOperator methodOperator;
    boolean multipleFilter;
    boolean andFilter;
    List<MethodFilterPart> filterPartList;

    public Bson createBsonFilter(Object[] arguments) throws Exception {
        Bson filter;
        List<Bson> filterList = new LinkedList<>();
        for (MethodFilterPart filterPart : filterPartList) {
            FilterType filterType = filterPart.filterType();
            int paramStartIndex = filterPart.nextParameterIndex();
            Bson processedBsonFilter = processBson(filterType, paramStartIndex, arguments);
            filterList.add(processedBsonFilter);
        }
        if (multipleFilter) {
            if (andFilter) {
                filter = Filters.and(filterList);
            } else {
                filter = Filters.or(filterList);
            }
        } else {
            filter = filterList.get(0);
        }
        return filter;
    }

    @SuppressWarnings("unchecked")
    private Bson processBson(FilterType filterType, int paramsIndexAt, Object[] args) throws Exception {
        String fieldName = filterType.field().getName();
        Bson retFilter = null;
        switch (filterType.operator()) {
            case EQUALS -> retFilter = Filters.eq(fieldName, getFilterableValue(args[paramsIndexAt]));
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
            case GREATER_THAN -> retFilter = Filters.gt(fieldName, getFilterableValue(args[paramsIndexAt]));
            case LESS_THAN -> retFilter = Filters.lt(fieldName, getFilterableValue(args[paramsIndexAt]));
            case GREATER_EQUALS -> retFilter = Filters.gte(fieldName, getFilterableValue(args[paramsIndexAt]));
            case LESS_EQUALS -> retFilter = Filters.lte(fieldName, getFilterableValue(args[paramsIndexAt]));
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
                    throw new MethodInvalidRegexParameterException(method, repositoryMeta.getRepositoryClass(), value.getClass());
                }
            }
            case EXISTS -> retFilter = Filters.exists(fieldName);
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
                // MongoDB expects an Array and not a List, but for easier usage
                // the framework wants a list. So just convert the list to an array and pass it to the filter
                List<Object> objectList = (List<Object>) getFilterableValue(args[paramsIndexAt]);
                Object[] objectArray = objectList.toArray(new Object[]{});
                retFilter = Filters.in(fieldName, objectArray);
            }
            default -> // This filter is not supported. Throw exception.
                    throw new MethodUnsupportedFilterException(method, repositoryMeta.getRepositoryClass());
        }
        // Applying negotiating of the filter, if needed
        if (filterType.notFilter()) {
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
