package eu.koboo.en2do;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import eu.koboo.en2do.codec.MapCodecProvider;
import eu.koboo.en2do.exception.*;
import eu.koboo.en2do.index.CompoundIndex;
import eu.koboo.en2do.index.Id;
import eu.koboo.en2do.index.Index;
import eu.koboo.en2do.index.NonIndex;
import eu.koboo.en2do.methods.FilterOperator;
import eu.koboo.en2do.methods.FilterType;
import eu.koboo.en2do.methods.MethodOperator;
import eu.koboo.en2do.repository.DropEntitiesOnStart;
import eu.koboo.en2do.repository.DropIndexesOnStart;
import eu.koboo.en2do.sort.annotation.Limit;
import eu.koboo.en2do.sort.annotation.Skip;
import eu.koboo.en2do.sort.annotation.SortBy;
import eu.koboo.en2do.sort.annotation.SortByArray;
import eu.koboo.en2do.sort.object.Sort;
import eu.koboo.en2do.utility.AnnotationUtils;
import eu.koboo.en2do.utility.FieldUtils;
import eu.koboo.en2do.utility.GenericUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MongoManager {

    // These methods are ignored by our methods processing.
    private static final List<String> IGNORED_REPOSITORY_METHODS = Arrays.asList(
            // Predefined methods by Repository interface
            "getCollectionName", "getUniqueId", "getEntityClass", "getEntityUniqueIdClass",
            "findById", "findAll", "delete", "deleteById", "deleteAll", "drop",
            "save", "saveAll", "exists", "existsById",
            // Predefined methods by Java objects
            "toString", "hashCode", "equals", "notify", "notifyAll", "wait", "finalize", "clone");

    MongoClient client;
    MongoDatabase database;
    Map<Class<?>, Repository<?, ?>> repoRegistry;

    public MongoManager(Credentials credentials) {

        // If no credentials given, try loading them from default file.
        if (credentials == null) {
            credentials = Credentials.fromFile();
        }
        // If no credentials given, try loading them from default resource.
        if (credentials == null) {
            credentials = Credentials.fromResource();
        }
        // If no credentials given, throw exception.
        if (credentials == null) {
            throw new NullPointerException("No credentials given! Please make sure to provide " +
                    "accessible credentials.");
        }

        String connectString = credentials.connectString();
        // If credentials connectString is null, throw exception
        if (connectString == null) {
            throw new NullPointerException("No connectString given! Please make sure to provide a " +
                    "accessible connectString.");
        }
        // If credentials databaseString is null, throw exception
        String databaseString = credentials.database();
        if (databaseString == null) {
            throw new NullPointerException("No databaseString given! Please make sure to provide a " +
                    "accessible databaseString.");
        }

        ConnectionString connection = new ConnectionString(connectString);

        CodecRegistry pojoCodec = CodecRegistries.fromProviders(PojoCodecProvider.builder()
                .register(new MapCodecProvider())
                .automatic(true)
                .build());
        CodecRegistry registry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodec);

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connection)
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(registry)
                .build();

        client = MongoClients.create(clientSettings);
        database = client.getDatabase(databaseString);

        repoRegistry = new ConcurrentHashMap<>();
    }

    public MongoManager() {
        this(null);
    }

    protected MongoDatabase getDatabase() {
        return database;
    }

    public boolean close() {
        try {
            client.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public <E, ID, R extends Repository<E, ID>> R create(Class<R> repoClass) {
        try {

            // Check for already created repository to avoid multiply instances of the same repository
            if (repoRegistry.containsKey(repoClass)) {
                return (R) repoRegistry.get(repoClass);
            }

            // Parse Entity and UniqueId type classes by generic repository arguments
            // (Yea, it's very hacky, but works)
            Type[] repoGenericTypeArray = repoClass.getGenericInterfaces();
            Type repoGenericTypeParams = null;
            for (Type type : repoGenericTypeArray) {
                if (type.getTypeName().split("<")[0].equalsIgnoreCase(Repository.class.getName())) {
                    repoGenericTypeParams = type;
                    break;
                }
            }
            if (repoGenericTypeParams == null) {
                throw new RepositoryNoTypeException(repoClass);
            }

            // Searching for entity class and after that their declared fields
            Class<E> entityClass;
            try {
                // get class name of generic type arguments
                String entityClassName = repoGenericTypeParams.getTypeName().split("<")[1].split(",")[0];
                entityClass = (Class<E>) Class.forName(entityClassName);
            } catch (ClassNotFoundException e) {
                throw new RepositoryEntityNotFoundException(repoClass, e);
            }

            // Collect all fields recursively
            Set<Field> allFields = FieldUtils.collectFields(entityClass);
            if (allFields.size() == 0) {
                throw new RepositoryNoFieldsException(repoClass);
            }

            // Predefine some variables for further validation
            Set<String> entityFieldNameSet = new HashSet<>();
            Class<ID> entityUniqueIdClass = null;
            Field entityUniqueIdField = null;
            for (Field field : allFields) {

                // Check for duplicated lower-case field names
                String lowerFieldName = field.getName().toLowerCase(Locale.ROOT);
                if (entityFieldNameSet.contains(lowerFieldName)) {
                    throw new RepositoryDuplicatedFieldException(field, repoClass);
                }
                entityFieldNameSet.add(lowerFieldName);

                // Check if field has final declaration
                if (Modifier.isFinal(field.getModifiers())) {
                    throw new RepositoryFinalFieldException(field, repoClass);
                }

                // Check for @Id annotation to find unique identifier of entity
                if (!field.isAnnotationPresent(Id.class)) {
                    continue;
                }
                entityUniqueIdClass = (Class<ID>) field.getType();
                entityUniqueIdField = field;
                entityUniqueIdField.setAccessible(true);
            }
            // Check if we found any unique identifier.
            if (entityUniqueIdClass == null) {
                throw new RepositoryIdNotFoundException(entityClass);
            }
            entityFieldNameSet.clear();

            // Iterate through the repository methods
            for (Method method : repoClass.getMethods()) {
                // Skip if the method should be ignored
                if (IGNORED_REPOSITORY_METHODS.contains(method.getName())) {
                    continue;
                }
                // Check for the return-types of the methods, and their defined names to match our pattern.
                checkReturnTypes(method, entityClass, repoClass);
                checkMethodOperation(method, entityClass, repoClass, allFields);
                checkSortOptions(method, entityClass, repoClass, allFields);
            }

            // Parse annotated collection name and create pojo-related mongo collection
            Collection collectionAnnotation = repoClass.getAnnotation(Collection.class);
            if (collectionAnnotation == null) {
                throw new RepositoryNameNotFoundException(repoClass);
            }
            String entityCollectionName = collectionAnnotation.value();
            MongoCollection<E> entityCollection = database.getCollection(entityCollectionName, entityClass);

            // Drop all entities on start if annotation is present.
            if (repoClass.isAnnotationPresent(DropEntitiesOnStart.class)) {
                entityCollection.drop();
            }

            // Drop all indexes on start if annotation is present.
            if (repoClass.isAnnotationPresent(DropIndexesOnStart.class)) {
                entityCollection.dropIndexes();
            }

            // Creating an index on the uniqueIdentifier field of the entity to speed up queries,
            // but only if wanted. Users can disable that with the annotation.
            if (!entityUniqueIdField.isAnnotationPresent(NonIndex.class)) {
                entityCollection.createIndex(Indexes.ascending(entityUniqueIdField.getName()), new IndexOptions().unique(true));
            }
            Set<CompoundIndex> compoundIndexList = AnnotationUtils.collectAnnotations(entityClass, CompoundIndex.class);
            for (CompoundIndex compoundIndex : compoundIndexList) {
                // Checking if the field in the annotation exists in the entity class.
                Index[] fieldIndexes = compoundIndex.value();
                for (Index fieldIndex : fieldIndexes) {
                    if (allFields.stream().map(Field::getName).noneMatch(fieldName -> fieldIndex.value().equalsIgnoreCase(fieldName))) {
                        throw new RepositoryIndexFieldNotFoundException(repoClass, fieldIndex.value());
                    }
                }
                // Validated all fields and creating the indexes on the collection.
                List<Bson> indexBsonList = new ArrayList<>();
                for (Index fieldIndex : fieldIndexes) {
                    String fieldName = fieldIndex.value();
                    Bson bsonIndex;
                    if (fieldIndex.ascending()) {
                        bsonIndex = Indexes.ascending(fieldName);
                    } else {
                        bsonIndex = Indexes.descending(fieldName);
                    }
                    indexBsonList.add(bsonIndex);
                }
                IndexOptions indexOptions = new IndexOptions()
                        .unique(compoundIndex.uniqueIndex());
                entityCollection.createIndex(Indexes.compoundIndex(indexBsonList), indexOptions);
            }

            // Create dynamic repository proxy object
            ClassLoader repoClassLoader = repoClass.getClassLoader();
            Class<?>[] interfaces = new Class[]{repoClass};
            Class<Repository<E, ID>> actualClass = (Class<Repository<E, ID>>) repoClass;
            Repository<E, ID> repository = (Repository<E, ID>) Proxy.newProxyInstance(repoClassLoader, interfaces,
                    new RepositoryInvocationHandler<>(this, entityCollectionName, entityCollection,
                            actualClass, entityClass, allFields, entityUniqueIdClass,
                            entityUniqueIdField));
            repoRegistry.put(repoClass, repository);
            return (R) repository;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <E> void checkReturnTypes(Method method, Class<E> entityClass, Class<?> repoClass) throws Exception {
        Class<?> returnTypeClass = method.getReturnType();
        String methodName = method.getName();
        MethodOperator methodOperator = MethodOperator.parseMethodStartsWith(methodName);
        if (methodOperator == null) {
            throw new MethodNoMethodOperatorException(method, repoClass);
        }
        switch (methodOperator) {
            case FIND -> {
                if (GenericUtils.isTypeOf(List.class, returnTypeClass)) {
                    Class<?> listType = GenericUtils.getGenericTypeOfReturnList(method);
                    if (!listType.isAssignableFrom(entityClass)) {
                        throw new MethodFindListTypeException(method, entityClass, listType);
                    }
                    return;
                }
                if (GenericUtils.isTypeOf(entityClass, returnTypeClass)) {
                    return;
                }
                throw new MethodFindReturnTypeException(method, entityClass, repoClass);
            }
            case DELETE, EXISTS -> {
                if (!GenericUtils.isTypeOf(Boolean.class, returnTypeClass)) {
                    throw new MethodBooleanReturnTypeException(method, repoClass);
                }
            }
            case COUNT -> {
                if (!GenericUtils.isTypeOf(Long.class, returnTypeClass)) {
                    throw new MethodLongReturnTypeException(method, repoClass);
                }
            }
            default -> throw new MethodUnsupportedReturnTypeException(returnTypeClass, methodName, entityClass);
        }
    }

    // ReturnType already checked.
    private <E> void checkMethodOperation(Method method, Class<E> entityClass, Class<?> repoClass,
                                          Set<Field> fieldSet) throws Exception {
        String methodName = method.getName();
        MethodOperator methodOperator = MethodOperator.parseMethodStartsWith(methodName);
        if (methodOperator == null) {
            throw new MethodNoMethodOperatorException(method, repoClass);
        }
        String fieldFilterName = methodOperator.removeOperatorFrom(methodName);
        if (methodName.contains("And") && methodName.contains("Or")) {
            throw new MethodDuplicatedChainException(method, entityClass);
        }
        int expectedParameters = countExpectedParameters(entityClass, repoClass, method, fieldFilterName, fieldSet);
        int parameterCount = method.getParameterCount();
        if (expectedParameters != parameterCount) {
            if (parameterCount > 0) {
                Class<?> lastParam = method.getParameterTypes()[parameterCount - 1];
                if (lastParam.isAssignableFrom(Sort.class) && (expectedParameters + 1) != parameterCount) {
                    throw new MethodParameterCountException(method, repoClass, (expectedParameters + 1), parameterCount);
                }
            } else {
                throw new MethodParameterCountException(method, repoClass, expectedParameters, parameterCount);
            }
        }
        String[] fieldFilterSplitByType = fieldFilterName.contains("And") ?
                fieldFilterName.split("And") : fieldFilterName.split("Or");
        int nextIndex = 0;
        for (int i = 0; i < fieldFilterSplitByType.length; i++) {
            String fieldFilterPart = fieldFilterSplitByType[i];
            FilterType filterType = createFilterType(entityClass, repoClass, method, fieldFilterPart, fieldSet);
            checkParameterType(method, filterType, nextIndex, repoClass);
            nextIndex = i + filterType.operator().getExpectedParameterCount();
        }
    }

    private <E> int countExpectedParameters(Class<E> entityClass, Class<?> repoClass, Method method,
                                            String fieldFilterPart, Set<Field> fieldSet) throws Exception {
        String methodName = method.getName();
        if (!methodName.contains("And") && !methodName.contains("Or")) {
            FilterType filterType = createFilterType(entityClass, repoClass, method, fieldFilterPart, fieldSet);
            return filterType.operator().getExpectedParameterCount();
        }
        String[] fieldFilterPartSplitByType = fieldFilterPart.contains("And") ?
                fieldFilterPart.split("And") : fieldFilterPart.split("Or");
        int expectedCount = 0;
        for (String fieldFilterPartSplit : fieldFilterPartSplitByType) {
            FilterType filterType = createFilterType(entityClass, repoClass, method, fieldFilterPartSplit, fieldSet);
            expectedCount += filterType.operator().getExpectedParameterCount();
        }
        return expectedCount;
    }

    private void checkParameterType(Method method, FilterType filterType, int startIndex, Class<?> repoClass) throws Exception {
        int expectedParamCount = filterType.operator().getExpectedParameterCount();
        for (int i = 0; i < expectedParamCount; i++) {
            int paramIndex = startIndex + i;
            Class<?> paramClass = method.getParameters()[paramIndex].getType();
            if (paramClass == null) {
                throw new MethodParameterNotFoundException(method, repoClass, (startIndex + expectedParamCount),
                        method.getParameterCount());
            }
            // Special check for regex, because it has only String or Pattern parameters
            if (filterType.operator() == FilterOperator.REGEX) {
                if (!GenericUtils.isTypeOf(String.class, paramClass) && !GenericUtils.isTypeOf(Pattern.class, paramClass)) {
                    throw new MethodInvalidRegexParameterException(method, repoClass, paramClass);
                }
                return;
            }
            Class<?> fieldClass = filterType.field().getType();
            if (filterType.operator() == FilterOperator.IN) {
                if (!GenericUtils.isTypeOf(List.class, paramClass)) {
                    throw new MethodMismatchingTypeException(method, repoClass, List.class, paramClass);
                }
                Class<?> listType = GenericUtils.getGenericTypeOfParameterList(method, paramIndex);
                if (!GenericUtils.isTypeOf(fieldClass, listType)) {
                    throw new MethodInvalidListParameterException(method, repoClass, fieldClass, listType);
                }
                return;
            }
            if (!GenericUtils.isTypeOf(fieldClass, paramClass)) {
                throw new MethodMismatchingTypeException(method, repoClass, fieldClass, paramClass);
            }
        }
    }

    protected <E> FilterType createFilterType(Class<E> entityClass, Class<?> repoClass, Method method,
                                              String operatorString, Set<Field> fieldSet) throws Exception {
        FilterOperator filterOperator = FilterOperator.parseFilterEndsWith(operatorString);
        if (filterOperator == null) {
            throw new MethodNoFilterOperatorException(method, repoClass);
        }
        String expectedField = filterOperator.removeOperatorFrom(operatorString);
        expectedField = expectedField.endsWith("Not") ? expectedField.replaceFirst("Not", "") : expectedField;
        Field field = FieldUtils.findFieldByName(expectedField, fieldSet);
        if (field == null) {
            throw new MethodFieldNotFoundException(expectedField, method, entityClass, repoClass);
        }
        return new FilterType(field, filterOperator);
    }

    private <E> void checkSortOptions(Method method, Class<E> entityClass, Class<?> repoClass, Set<Field> fieldSet) throws Exception {
        String fieldName = null;
        if (method.isAnnotationPresent(SortBy.class)) {
            SortBy sortBy = method.getAnnotation(SortBy.class);
            fieldName = sortBy.field();
        }
        int parameterCount = method.getParameterCount();
        if (parameterCount > 0) {
            Class<?> lastParam = method.getParameterTypes()[parameterCount - 1];
            boolean hasAnySortAnnotation = method.isAnnotationPresent(Limit.class)
                    || method.isAnnotationPresent(Skip.class)
                    || method.isAnnotationPresent(SortBy.class)
                    || method.isAnnotationPresent(SortByArray.class);
            if (hasAnySortAnnotation && lastParam.isAssignableFrom(Sort.class)) {
                throw new MethodMixedSortException(method, repoClass);
            }
        }
        if (fieldName == null) {
            return;
        }
        Field field = FieldUtils.findFieldByName(fieldName, fieldSet);
        if (field == null) {
            throw new MethodSortFieldNotFoundException(fieldName, method, entityClass, repoClass);
        }
    }
}
