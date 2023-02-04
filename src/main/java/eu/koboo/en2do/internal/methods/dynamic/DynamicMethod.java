package eu.koboo.en2do.internal.methods.dynamic;

import com.mongodb.client.model.Filters;
import eu.koboo.en2do.internal.RepositoryMeta;
import eu.koboo.en2do.internal.exception.methods.MethodInvalidRegexParameterException;
import eu.koboo.en2do.internal.exception.methods.MethodUnsupportedFilterException;
import eu.koboo.en2do.internal.methods.operators.MethodOperator;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DynamicMethod<E, ID, R extends Repository<E, ID>> {

    @NotNull
    Method method;

    @NotNull
    RepositoryMeta<E, ID, R> repositoryMeta;

    @Getter
    @NotNull
    MethodOperator methodOperator;

    boolean multipleFilter;
    boolean andFilter;

    @NotNull
    List<MethodFilterPart> filterPartList;

    public @NotNull Bson createBsonFilter(@NotNull Object[] arguments) throws Exception {
        Bson filter;
        List<Bson> filterList = new LinkedList<>();
        for (MethodFilterPart filterPart : filterPartList) {
            FilterType filterType = filterPart.getFilterType();
            int paramStartIndex = filterPart.getNextParameterIndex();
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
    private @NotNull Bson processBson(@NotNull FilterType filterType, int paramsIndexAt,
                                      @NotNull Object[] args) throws Exception {
        String fieldName = filterType.getField().getName();
        Bson retFilter = null;
        switch (filterType.getOperator()) {
            case EQUALS:
                retFilter = Filters.eq(fieldName, getFilterableValue(args[paramsIndexAt]));
                break;
            case EQUALS_IGNORE_CASE:
                String ignCasePatternString = "(?i)^" + getFilterableValue(args[paramsIndexAt]) + "$";
                Pattern ignCasePattern = Pattern.compile(ignCasePatternString, Pattern.CASE_INSENSITIVE);
                retFilter = Filters.regex(fieldName, ignCasePattern);
                break;
            case CONTAINS:
                String containsPatternString = ".*" + getFilterableValue(args[paramsIndexAt]) + ".*";
                Pattern containsPattern = Pattern.compile(containsPatternString, Pattern.CASE_INSENSITIVE);
                retFilter = Filters.regex(fieldName, containsPattern);
                break;
            case GREATER_THAN:
                retFilter = Filters.gt(fieldName, getFilterableValue(args[paramsIndexAt]));
                break;
            case LESS_THAN:
                retFilter = Filters.lt(fieldName, getFilterableValue(args[paramsIndexAt]));
                break;
            case GREATER_EQUALS:
                retFilter = Filters.gte(fieldName, getFilterableValue(args[paramsIndexAt]));
                break;
            case LESS_EQUALS:
                retFilter = Filters.lte(fieldName, getFilterableValue(args[paramsIndexAt]));
                break;
            case REGEX:
                // MongoDB supports multiple types of regex filtering, so check which type is provided.
                Object value = getFilterableValue(args[paramsIndexAt]);
                if (value instanceof String) {
                    String regexPatternString = (String) value;
                    retFilter = Filters.regex(fieldName, regexPatternString);
                }
                if (value instanceof Pattern) {
                    Pattern regexPattern = (Pattern) value;
                    retFilter = Filters.regex(fieldName, regexPattern);
                }
                if (retFilter == null) {
                    throw new MethodInvalidRegexParameterException(method, repositoryMeta.getRepositoryClass(), value.getClass());
                }
                break;
            case EXISTS:
                retFilter = Filters.exists(fieldName);
                break;
            case BETWEEN:
                Object betweenStart = getFilterableValue(args[paramsIndexAt]);
                Object betweenEnd = args[paramsIndexAt + 1];
                retFilter = Filters.and(Filters.gt(fieldName, betweenStart), Filters.lt(fieldName, betweenEnd));
                break;
            case BETWEEN_EQUALS:
                Object betweenEqStart = getFilterableValue(args[paramsIndexAt]);
                Object betweenEqEnd = args[paramsIndexAt + 1];
                retFilter = Filters.and(Filters.gte(fieldName, betweenEqStart), Filters.lte(fieldName, betweenEqEnd));
                break;
            case IN:
                // MongoDB expects an Array and not a List, but for easier usage
                // the framework wants a list. So just convert the list to an array and pass it to the filter
                List<Object> objectList = (List<Object>) getFilterableValue(args[paramsIndexAt]);
                Object[] objectArray = objectList.toArray(new Object[]{});
                retFilter = Filters.in(fieldName, objectArray);
                break;
            default: // This filter is not supported. Throw exception.
                throw new MethodUnsupportedFilterException(method, repositoryMeta.getRepositoryClass());
        }
        // Applying negotiating of the filter, if needed
        if (filterType.isNotFilter()) {
            return Filters.not(retFilter);
        }
        return retFilter;
    }

    private @NotNull Object getFilterableValue(@NotNull Object object) {
        if (object instanceof Enum<?>) {
            return ((Enum<?>) object).name();
        }
        return object;
    }
}
