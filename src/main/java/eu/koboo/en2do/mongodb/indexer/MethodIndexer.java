package eu.koboo.en2do.mongodb.indexer;

import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.Validator;
import eu.koboo.en2do.mongodb.exception.RepositoryMethodException;
import eu.koboo.en2do.mongodb.exception.methods.*;
import eu.koboo.en2do.mongodb.methods.dynamic.IndexedFilter;
import eu.koboo.en2do.mongodb.methods.dynamic.IndexedMethod;
import eu.koboo.en2do.mongodb.methods.predefined.PredefinedMethodRegistry;
import eu.koboo.en2do.operators.AmountType;
import eu.koboo.en2do.operators.ChainType;
import eu.koboo.en2do.operators.FilterOperator;
import eu.koboo.en2do.operators.MethodOperator;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.repository.methods.sort.*;
import eu.koboo.en2do.repository.methods.transform.NestedBsonKey;
import eu.koboo.en2do.repository.methods.transform.Transform;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MethodIndexer<E, ID, R extends Repository<E, ID>> {

    final RepositoryIndexer<E, ID, R> repositoryIndexer;
    final Class<R> repositoryClass;
    final Class<E> entityClass;
    final Method method;

    final Set<NestedBsonKey> nestedBsonKeySet;

    final MethodOperator methodOperator;

    final AmountType amountType;
    final long entityAmount;

    final List<IndexedFilter> indexedFilters;
    ChainType chainType;

    String parsableMethodName;
    String parsableLoweredName;

    public MethodIndexer(PredefinedMethodRegistry predefinedMethodRegistry,
                         RepositoryIndexer<E, ID, R> repositoryIndexer,
                         Method method) {
        this.repositoryIndexer = repositoryIndexer;
        this.repositoryClass = repositoryIndexer.getRepositoryClass();
        this.entityClass = repositoryIndexer.getEntityClass();
        this.method = method;

        this.nestedBsonKeySet = parseInnerNestedBsonKeys();

        // Resolve the method name, which should be parsed
        parsableMethodName = method.getName();
        Transform transform = method.getAnnotation(Transform.class);
        if (transform != null) {
            parsableMethodName = transform.value();
        }

        // Check if we catch a predefined method name
        if (predefinedMethodRegistry.isPredefinedMethod(parsableMethodName)) {
            throw new RepositoryMethodException("Override on predefined method!", repositoryClass, method);
        }

        this.methodOperator = parseMethodOperator();

        this.amountType = parseAmountType();
        this.entityAmount = parseEntityAmount();

        removeFilterStartIndicator();

        this.indexedFilters = parseFilters();

        validateParameterCount();
        validateParameterTypes();
    }

    private MethodOperator parseMethodOperator() {
        // Parse the MethodOperator by the methodName
        for (MethodOperator operator : MethodOperator.VALUES) {
            String keyword = operator.getKeyword();
            if (!parsableMethodName.startsWith(keyword)) {
                continue;
            }
            // Validate the return types of the given method operator.
            operator.validateReturnType(method, entityClass, repositoryClass);

            // Remove the method operator string from the parsable value.
            parsableMethodName = parsableMethodName.replaceFirst(keyword, "");
            return operator;
        }
        throw new MethodNoMethodOperatorException(repositoryClass, method);
    }

    private AmountType parseAmountType() {
        // Only find makes use of the AmountType.
        // Ignore if we got a different MethodOperator.
        if(methodOperator != MethodOperator.FIND) {
            return null;
        }
        for (AmountType amountType : AmountType.VALUES) {
            String keyword = amountType.getKeyword();
            if (!parsableMethodName.startsWith(keyword)) {
                continue;
            }
            parsableMethodName = parsableMethodName.replaceFirst(keyword, "");
            return amountType;
        }
        return null;
    }

    private long parseEntityAmount() {
        // Only find makes use of the AmountType.
        // Ignore if we got a different MethodOperator.
        if(methodOperator != MethodOperator.FIND) {
            return -1;
        }
        if (amountType == null) {
            return -1;
        }
        switch (amountType) {
            case ONE:
            case FIRST:
                return 1;
            case TOP:
                long entityAmount = AmountType.parseAmountByStringStartsWith(parsableMethodName);
                if (entityAmount == 0) {
                    throw new RuntimeException("The entityAmount 0 is not a valid top number.");
                }
                parsableMethodName = parsableMethodName.replaceFirst(String.valueOf(entityAmount), "");
                return entityAmount;
            case MANY:
            case ALL:
                // Doesn't get used anyway.
                return -1;
            default:
                throw new IllegalArgumentException("Cannot parse entity amount by type " +
                    amountType.name() + " " + parsableMethodName);
        }
    }

    private void removeFilterStartIndicator() {
        // Remove the keyword "By" from the method name.
        parsableMethodName = parsableMethodName.replaceFirst("By", "");
    }

    private Set<NestedBsonKey> parseInnerNestedBsonKeys() {
        NestedBsonKey[] annotationsByType = method.getAnnotationsByType(NestedBsonKey.class);
        return new LinkedHashSet<>(Arrays.asList(annotationsByType));
    }

    private String parseNextBsonKey() {
        String bsonFilterKey = null;
        // Check if we can find any nested fields
        for (NestedBsonKey nestedBsonKey : nestedBsonKeySet) {
            String loweredKey = nestedBsonKey.id().toLowerCase(Locale.ROOT);
            if (!parsableLoweredName.startsWith(loweredKey)) {
                continue;
            }
            parsableLoweredName = parsableLoweredName.replaceFirst(loweredKey, "");
            bsonFilterKey = nestedBsonKey.bson();
            break;
        }

        // Check if we can find any direct entity fields
        Map<String, Field> bsonToFieldMap = repositoryIndexer.getBsonToFieldMap();
        for (String bsonFieldKey : bsonToFieldMap.keySet()) {
            Field field = bsonToFieldMap.get(bsonFieldKey);
            String fieldName = field.getName();
            String loweredFieldName = fieldName.toLowerCase(Locale.ROOT);
            if (!parsableLoweredName.startsWith(loweredFieldName)) {
                continue;
            }
            parsableLoweredName = parsableLoweredName.replaceFirst(loweredFieldName, "");
            bsonFilterKey = bsonFieldKey;
            break;
        }

        // Check if we found any key to filter with in bson.
        if (bsonFilterKey == null) {
            throw new MethodFieldNotFoundException(repositoryClass, method, parsableLoweredName);
        }
        return bsonFilterKey;
    }

    private boolean parseNextNegateFilter() {
        boolean notFilter = false;
        if (parsableLoweredName.startsWith("not")) {
            notFilter = true;
            parsableLoweredName = parsableLoweredName.replaceFirst("not", "");
        }
        return notFilter;
    }

    private ChainType parseNextChainType() {
        if (parsableLoweredName.startsWith("and")) {
            parsableLoweredName = parsableLoweredName.replaceFirst("and", "");
            return ChainType.AND;
        }
        if (parsableLoweredName.startsWith("or")) {
            parsableLoweredName = parsableLoweredName.replaceFirst("or", "");
            return ChainType.OR;
        }
        return null;
    }

    private FilterOperator parseNextFilterOperator() {
        for (FilterOperator value : FilterOperator.VALUES) {
            if (value == FilterOperator.EQUALS) {
                continue;
            }
            String loweredKeyword = value.getKeyword().toLowerCase(Locale.ROOT);
            boolean startsWith = parsableLoweredName.startsWith(loweredKeyword);
            if (!startsWith) {
                continue;
            }
            parsableLoweredName = parsableLoweredName.replaceFirst(loweredKeyword, "");
            return value;
        }
        return FilterOperator.EQUALS;
    }

    private Field parseFieldByBsonKey(String bsonKey) {
        return repositoryIndexer.getBsonToFieldMap().get(bsonKey);
    }

    private List<IndexedFilter> parseFilters() {
        parsableLoweredName = parsableMethodName.toLowerCase(Locale.ROOT);

        int nextParameterIndex = 0;
        int indexedFilterAmount = 0;

        List<IndexedFilter> indexedFilterList = new LinkedList<>();

        int safeBreakAmount = 200;
        while (!parsableLoweredName.equalsIgnoreCase("") && safeBreakAmount > 0) {
            // Add safe break to avoid infinite loops
            safeBreakAmount--;

            String bsonFilterKey = parseNextBsonKey();
            boolean notFilter = parseNextNegateFilter();

            FilterOperator filterOperator = parseNextFilterOperator();

            ChainType nextChainType = parseNextChainType();
            if (chainType == null) {
                chainType = nextChainType;
            }
            if (nextChainType != null && chainType != nextChainType) {
                throw new MethodDuplicatedChainException(repositoryClass, method);
            }

            Field directEntityField = parseFieldByBsonKey(bsonFilterKey);
            if (directEntityField != null) {
                Validator.validateParameterTypes(repositoryClass, method, directEntityField, filterOperator, nextParameterIndex);
            }

            IndexedFilter indexedFilter = new IndexedFilter(bsonFilterKey, notFilter, filterOperator, nextParameterIndex);
            indexedFilterList.add(indexedFilter);
            int operatorParameterCount = filterOperator.getExpectedParameterCount();
            nextParameterIndex = indexedFilterAmount + operatorParameterCount;
            indexedFilterAmount += 1;
        }
        if (chainType == null) {
            chainType = ChainType.NONE;
        }

        return indexedFilterList;
    }

    private int getExpectedParameterCount() {
        int expectedParameterAmount = methodOperator.getAdditionalParameters();
        for (IndexedFilter indexedFilter : indexedFilters) {
            expectedParameterAmount += indexedFilter.getOperator().getExpectedParameterCount();
        }
        return expectedParameterAmount;
    }

    private void validateParameterCount() {
        int expectedParameterAmount = getExpectedParameterCount();
        int actualParameterAmount = method.getParameterCount();
        if (expectedParameterAmount == actualParameterAmount) {
            return;
        }
        if (actualParameterAmount == 0) {
            if (methodOperator == MethodOperator.PAGE) {
                throw new MethodPageRequiredException(repositoryClass, method);
            }
            return;
        }

        if (actualParameterAmount > 0) {
            // Subtract 1 from parameterCount. This object could be a Sort or Pagination object.
            // That means, the expectedParameterCount is less than the actualParameterCount.
            int lastParameterIndex = (expectedParameterAmount + 1);
            Class<?> lastMethodParameter = method.getParameterTypes()[actualParameterAmount - 1];
            if (lastMethodParameter.isAssignableFrom(Sort.class)) {
                if (methodOperator == MethodOperator.PAGE) {
                    throw new MethodSortNotAllowedException(repositoryClass, method);
                }
                if (lastParameterIndex != actualParameterAmount) {
                    throw new MethodParameterCountException(repositoryClass, method, lastParameterIndex);
                }
            }
            if (lastMethodParameter.isAssignableFrom(Pagination.class)) {
                if (methodOperator != MethodOperator.PAGE) {
                    throw new MethodPageNotAllowedException(repositoryClass, method);
                }
                if (lastParameterIndex != actualParameterAmount) {
                    throw new MethodParameterCountException(repositoryClass, method, lastParameterIndex);
                }
            }
            if (lastMethodParameter.isAssignableFrom(UpdateBatch.class)) {
                if (methodOperator != MethodOperator.UPDATE_FIELD) {
                    throw new MethodBatchNotAllowedException(repositoryClass, method);
                }
                if (lastParameterIndex != actualParameterAmount) {
                    throw new MethodParameterCountException(repositoryClass, method, lastParameterIndex);
                }
            }
            return;
        }
        throw new MethodParameterCountException(repositoryClass, method, expectedParameterAmount);
    }

    void validateParameterTypes() {
        // Check if the method has the Sort annotation set.
        SortBy sortAnnotation = method.getAnnotation(SortBy.class);
        if (sortAnnotation != null) {
            if (methodOperator == MethodOperator.PAGE) {
                throw new MethodSortNotAllowedException(repositoryClass, method);
            }
            String sortFieldName = sortAnnotation.field();
            Field field = parseFieldByBsonKey(sortFieldName);
            if (field == null) {
                throw new MethodSortFieldNotFoundException(repositoryClass, method, sortFieldName);
            }
        }
        int actualParameterCount = method.getParameterCount();
        if (actualParameterCount > 0) {
            Class<?> lastMethodParameter = method.getParameterTypes()[actualParameterCount - 1];
            // Check if both Sort types are used.
            // This is not allowed, even if it is possible internally.
            // En2do is capable of handling it, but I don't know the result.
            // Probably one sort would get an override by the other one.
            boolean hasAnySortAnnotation = method.isAnnotationPresent(Limit.class)
                || method.isAnnotationPresent(Skip.class)
                || method.isAnnotationPresent(SortBy.class)
                || method.isAnnotationPresent(SortByArray.class);
            if (hasAnySortAnnotation && lastMethodParameter.isAssignableFrom(Sort.class)) {
                throw new MethodMixedSortException(repositoryClass, method);
            }
            // We can't check the field, because it's a parameter, we can only check it, on executing
            // while runtime, so good luck to you my dear ;)
        }
    }

    public void indexMethod(RepositoryData<E, ID, R> repositoryData) {
        IndexedMethod<E, ID, R> indexedMethod = new IndexedMethod<>(
            method,
            methodOperator,
            chainType,
            -1L,
            amountType,
            entityAmount,
            indexedFilters,
            repositoryData);
        repositoryData.registerDynamicMethod(method.getName(), indexedMethod);
    }
}
