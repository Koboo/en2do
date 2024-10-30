package eu.koboo.en2do;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.RepositoryInvocationHandler;
import eu.koboo.en2do.mongodb.Validator;
import eu.koboo.en2do.mongodb.codec.InternalPropertyCodecProvider;
import eu.koboo.en2do.mongodb.convention.AnnotationConvention;
import eu.koboo.en2do.mongodb.exception.methods.*;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryIdNotFoundException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryInvalidException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryNameDuplicateException;
import eu.koboo.en2do.mongodb.methods.dynamic.IndexedFilter;
import eu.koboo.en2do.mongodb.methods.dynamic.IndexedMethod;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.mongodb.methods.predefined.impl.*;
import eu.koboo.en2do.operators.Chain;
import eu.koboo.en2do.operators.FilterOperator;
import eu.koboo.en2do.operators.MethodOperator;
import eu.koboo.en2do.parser.RepositoryParser;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.repository.methods.async.Async;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.repository.methods.sort.*;
import eu.koboo.en2do.repository.methods.transform.NestedField;
import eu.koboo.en2do.repository.methods.transform.Transform;
import eu.koboo.en2do.repository.options.DropEntitiesOnStart;
import eu.koboo.en2do.repository.options.DropIndexesOnStart;
import eu.koboo.en2do.utility.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.UuidRepresentation;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class MongoManager {

    @Getter
    SettingsBuilder settingsBuilder;
    RepositoryParser parser;

    Map<Class<?>, RepositoryData<?, ?, ?>> repositoryDataByClassMap;
    Map<Class<?>, Repository<?, ?>> repositoryByClassRegistry;
    Map<String, GlobalPredefinedMethod> predefinedMethodRegistry;
    ExecutorService executorService;

    InternalPropertyCodecProvider internalPropertyCodecProvider;
    CodecRegistry codecRegistry;

    @Getter
    MongoClient mongoClient;

    @Getter
    MongoDatabase mongoDatabase;

    public MongoManager(Credentials credentials, ExecutorService executorService, SettingsBuilder settingsBuilder) {
        if (settingsBuilder == null) {
            settingsBuilder = new SettingsBuilder();
        }
        this.settingsBuilder = settingsBuilder;
        applyLoggerLevel();

        this.parser = new RepositoryParser(settingsBuilder);
        this.repositoryDataByClassMap = new ConcurrentHashMap<>();
        this.repositoryByClassRegistry = new ConcurrentHashMap<>();
        this.predefinedMethodRegistry = new LinkedHashMap<>();
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        }
        this.executorService = executorService;

        // If no credentials given, try loading them from the default sources,
        // like resource files, system properties of environment variables.
        // See Credentials for information about the keys of the properties.
        // They can differ in every source location.
        if (credentials == null) {
            credentials = Credentials.fromFile();
        }
        if (credentials == null) {
            credentials = Credentials.fromResource();
        }
        if (credentials == null) {
            credentials = Credentials.fromSystemProperties();
        }
        if (credentials.getConnectString() == null || credentials.getDatabase() == null) {
            credentials = Credentials.fromSystemEnvVars();
        }

        String connectString = credentials.getConnectString();
        if (connectString == null) {
            throw new NullPointerException("No connectString given! Please make sure to provide a " +
                "accessible connectString.");
        }
        String databaseString = credentials.getDatabase();
        if (databaseString == null) {
            throw new NullPointerException("No databaseString given! Please make sure to provide a " +
                "accessible databaseString.");
        }

        ConnectionString connection = new ConnectionString(connectString);

        internalPropertyCodecProvider = new InternalPropertyCodecProvider(this);

        codecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder()
                .register(internalPropertyCodecProvider)
                .automatic(true)
                .conventions(List.of(
                    new AnnotationConvention(),
                    Conventions.ANNOTATION_CONVENTION,
                    Conventions.SET_PRIVATE_FIELDS_CONVENTION,
                    Conventions.USE_GETTERS_FOR_SETTERS
                ))
                .build())
        );

        MongoClientSettings clientSettings = MongoClientSettings.builder()
            .applicationName("en2do-client")
            .applyConnectionString(connection)
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .codecRegistry(codecRegistry)
            .build();

        mongoClient = MongoClients.create(clientSettings);
        mongoDatabase = mongoClient.getDatabase(databaseString);

        registerPredefinedMethods();
    }

    public MongoManager(Credentials credentials, SettingsBuilder settingsBuilder) {
        this(credentials, null, settingsBuilder);
    }

    public MongoManager(SettingsBuilder settingsBuilder) {
        this(null, null, settingsBuilder);
    }

    public MongoManager(Credentials credentials) {
        this(credentials, null, null);
    }

    public MongoManager() {
        this(null, null, null);
    }

    public void close() {
        close(true);
    }

    public void close(boolean shutdownExecutorService) {
        try {
            if (executorService != null && shutdownExecutorService) {
                executorService.shutdown();
            }
            repositoryByClassRegistry.clear();
            for (RepositoryData<?, ?, ?> meta : repositoryDataByClassMap.values()) {
                meta.destroy();
            }
            repositoryDataByClassMap.clear();
            if (mongoClient != null) {
                mongoClient.close();
            }
            if (parser != null) {
                parser.destroy();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while closing: " + MongoManager.class, e);
        }
    }

    private void registerPredefinedMethod(GlobalPredefinedMethod predefinedMethod) {
        String methodName = predefinedMethod.getMethodName();
        if (predefinedMethodRegistry.containsKey(methodName)) {
            throw new RuntimeException("Already registered method with name \"" + methodName + "\".");
        }
        predefinedMethodRegistry.put(methodName, predefinedMethod);
    }

    private void registerPredefinedMethods() {
        // Register the default predefined methods, which can get executed
        // on every created repository.
        registerPredefinedMethod(new MethodCountAll());
        registerPredefinedMethod(new MethodDelete());
        registerPredefinedMethod(new MethodDeleteAll());
        registerPredefinedMethod(new MethodDeleteById());
        registerPredefinedMethod(new MethodDeleteMany());
        registerPredefinedMethod(new MethodDeleteManyById());
        registerPredefinedMethod(new MethodDrop());
        registerPredefinedMethod(new MethodEquals());
        registerPredefinedMethod(new MethodExists());
        registerPredefinedMethod(new MethodExistsById());
        registerPredefinedMethod(new MethodFindAll());
        registerPredefinedMethod(new MethodFindFirstById());
        registerPredefinedMethod(new MethodGetClass());
        registerPredefinedMethod(new MethodGetCollectionName());
        registerPredefinedMethod(new MethodGetEntityClass());
        registerPredefinedMethod(new MethodGetEntityUniqueIdClass());
        registerPredefinedMethod(new MethodGetNativeCollection());
        registerPredefinedMethod(new MethodGetUniqueId());
        registerPredefinedMethod(new MethodHashCode());
        registerPredefinedMethod(new MethodInsertAll());
        registerPredefinedMethod(new MethodPageAll());
        registerPredefinedMethod(new MethodSave());
        registerPredefinedMethod(new MethodSaveAll());
        registerPredefinedMethod(new MethodSortAll());
        registerPredefinedMethod(new MethodToString());
        registerPredefinedMethod(new MethodUpdateAllFields());
    }

    @SuppressWarnings("unchecked")
    public <E, ID, R extends Repository<E, ID>> R create(Class<R> repositoryClass) {
        try {

            // Check for already created repository to avoid multiply instances of the same repository
            if (repositoryByClassRegistry.containsKey(repositoryClass)) {
                return (R) repositoryByClassRegistry.get(repositoryClass);
            }

            if (!Repository.class.isAssignableFrom(repositoryClass)) {
                throw new RepositoryInvalidException(repositoryClass);
            }

            Tuple<Class<?>, Class<?>> genericTypeTuple = parser.parseGenericTypes(repositoryClass, Repository.class);
            Class<E> entityClass = (Class<E>) genericTypeTuple.getFirst();
            Class<ID> entityIdClass = (Class<ID>) genericTypeTuple.getSecond();

            // Check if repository and async repository use the same entity and id types.
            parser.validateRepositoryTypes(repositoryClass, genericTypeTuple);

            // Check if the collection name is valid
            String entityCollectionName = parser.parseCollectionName(repositoryClass, entityClass);

            // Check if we already got a repository with that name.
            for (RepositoryData<?, ?, ?> meta : repositoryDataByClassMap.values()) {
                if (meta.getCollectionName().equalsIgnoreCase(entityCollectionName)) {
                    throw new RepositoryNameDuplicateException(repositoryClass, Collection.class);
                }
            }

            Validator.validateCompatibility(codecRegistry, repositoryClass, entityClass);

            // Collect all fields recursively to ensure,
            // we'll get the even the fields from the inheritance between object
            Set<Field> entityFieldSet = parser.parseEntityFields(entityClass);

            Field entityUniqueIdField = parser.parseEntityIdField(entityClass);
            if (entityUniqueIdField == null) {
                throw new RepositoryIdNotFoundException(entityClass, Id.class);
            }

            // Creating the native mongodb collection object,
            // and it's respective repository data object.
            MongoCollection<E> entityCollection = mongoDatabase.getCollection(entityCollectionName, entityClass);
            RepositoryData<E, ID, R> repositoryData = new RepositoryData<>(this,
                repositoryClass, entityClass,
                entityFieldSet,
                entityIdClass, entityUniqueIdField,
                entityCollection, entityCollectionName
            );

            Map<String, Field> sortedFieldMap = parser.parseSortedFieldBsonNames(entityClass);

            // Iterate through the repository methods
            for (Method method : repositoryClass.getMethods()) {
                String originalMethodName = method.getName();

                // Apply transform annotation
                String methodName = originalMethodName;
                Transform transform = method.getAnnotation(Transform.class);
                if (transform != null) {
                    methodName = transform.value();
                }

                // Check if we catch a predefined method
                if (predefinedMethodRegistry.containsKey(methodName)) {
                    continue;
                }

                // Method was override and transformed. Nice try my friend ;)
                if (method.isAnnotationPresent(Override.class)
                    && predefinedMethodRegistry.containsKey(originalMethodName)) {
                    continue;
                }

                // Skip the methods, which we can "safely" ignore.
                if (MethodUtils.IGNORED_DEFAULT_METHODS.contains(methodName)) {
                    continue;
                }

                // Get the default return type of the method
                Class<?> returnType = method.getReturnType();

                // Check if the method is async and if so,
                // validate that the return types is a completable future.
                boolean isAsyncMethod = method.isAnnotationPresent(Async.class);
                if (isAsyncMethod) {
                    // Check async method name and if the user tries to
                    // "fake" one of our predefined methods.
                    if (methodName.startsWith("async")) {
                        String predefinedName = repositoryData.stripAsyncName(methodName);
                        if (predefinedMethodRegistry.containsKey(predefinedName)) {
                            continue;
                        }
                        throw new MethodInvalidAsyncNameException(method, repositoryClass);
                    }
                    // Check CompletableFuture return type of async repository methods.
                    if (GenericUtils.isNotTypeOf(returnType, CompletableFuture.class)) {
                        throw new MethodInvalidAsyncReturnException(method, repositoryClass);
                    }
                    returnType = GenericUtils.getGenericTypeOfReturnType(method);
                }


                // Parse the MethodOperator by the methodName
                MethodOperator methodOperator = MethodOperator.parseMethodStartsWith(methodName);
                if (methodOperator == null) {
                    throw new MethodNoMethodOperatorException(method, repositoryClass);
                }

                // Check the returnTypes by using the predefined validator.
                methodOperator.validate(method, returnType, entityClass, repositoryClass);

                // Remove the leading methodOperator to ensure it doesn't trick the validation
                String strippedMethodName = methodOperator.removeOperatorFrom(methodName);

                // Parse the defined entity count, by checking for the keywords
                // "Top" - The first X entities
                // "Many" - All entities
                // "First" - Only the first entity
                Long methodDefinedEntityCount = null;
                if (strippedMethodName.startsWith("Top")) {
                    strippedMethodName = strippedMethodName.replaceFirst("Top", "");
                    if (strippedMethodName.startsWith("0")) {
                        throw new RuntimeException("The number you want to filter can not start with \"0\".");
                    }
                    methodDefinedEntityCount = MethodUtils.getPrefixedNumber(strippedMethodName);
                    if (methodDefinedEntityCount == 0) {
                        throw new RuntimeException("The number 0 is not a valid top number.");
                    }
                    strippedMethodName = strippedMethodName.replaceFirst(String.valueOf(methodDefinedEntityCount), "");
                }
                if (strippedMethodName.startsWith("Many")) {
                    strippedMethodName = strippedMethodName.replaceFirst("Many", "");
                    methodDefinedEntityCount = -1L;
                }
                if (strippedMethodName.startsWith("First")) {
                    strippedMethodName = strippedMethodName.replaceFirst("First", "");
                    methodDefinedEntityCount = 1L;
                }

                // Remove the string "By" from the method name.
                strippedMethodName = strippedMethodName.replaceFirst("By", "");

                // Parse, validate and handle the method name and "compile" it
                // so en2do can use the extracted information for the internal usage.
                Set<NestedField> nestedFieldSet = AnnotationUtils.getNestedKeySet(method);

                // Counts for further validation
                int expectedParameterCount = 0;
                int nextParameterIndex = 0;
                int itemCount = 0;

                // The list of the filters of this method.
                List<IndexedFilter> indexedFilterList = new LinkedList<>();

                // The previous method name, but stripped by the method operator.
                // So only the filters are left and lower cased.
                String loweredStrip = strippedMethodName.toLowerCase(Locale.ROOT);

                // Chain represents either AND or OR for all filters.
                Chain chain = null;

                // We are using a while loop. Just to ensure we are not doing
                // infinite loops, we also track the execution amount using the safeBreakAmount.
                int safeBreakAmount = 200;
                while (!loweredStrip.equalsIgnoreCase("") && safeBreakAmount > 0) {
                    // Add safe break to avoid infinite loops
                    safeBreakAmount--;

                    String bsonName = null;
                    // Check if we can find any nested fields
                    for (NestedField nestedField : nestedFieldSet) {
                        String loweredKey = nestedField.key().toLowerCase(Locale.ROOT);
                        if (!loweredStrip.startsWith(loweredKey)) {
                            continue;
                        }
                        loweredStrip = loweredStrip.replaceFirst(loweredKey, "");
                        bsonName = nestedField.query();
                        break;
                    }

                    // Check if we can find any direct entity fields
                    Field entityField = null;
                    for (Field field : sortedFieldMap.values()) {
                        String bsonFieldName = FieldUtils.parseBsonName(field);
                        String loweredBsonName = bsonFieldName.toLowerCase(Locale.ROOT);
                        if (!loweredStrip.startsWith(loweredBsonName)) {
                            continue;
                        }
                        loweredStrip = loweredStrip.replaceFirst(loweredBsonName, "");
                        entityField = field;
                        bsonName = bsonFieldName;
                        break;
                    }

                    // Check if we found any field.
                    if (bsonName == null) {
                        throw new MethodFieldNotFoundException(strippedMethodName, method, entityClass, repositoryClass);
                    }

                    boolean notFilter = false;
                    if (loweredStrip.startsWith("not")) {
                        notFilter = true;
                        loweredStrip = loweredStrip.replaceFirst("not", "");
                    }

                    FilterOperator filterOperator = FilterOperator.EQUALS;
                    for (FilterOperator value : FilterOperator.VALUES) {
                        if (loweredStrip.startsWith("and") || loweredStrip.startsWith("or")) {
                            break;
                        }
                        if (value == FilterOperator.EQUALS) {
                            continue;
                        }
                        String loweredKeyword = value.getKeyword().toLowerCase(Locale.ROOT);
                        boolean startsWith = loweredStrip.startsWith(loweredKeyword);
                        if (!startsWith) {
                            continue;
                        }
                        loweredStrip = loweredStrip.replaceFirst(loweredKeyword, "");
                        filterOperator = value;
                        break;
                    }

                    if (loweredStrip.startsWith("and")) {
                        loweredStrip = loweredStrip.replaceFirst("and", "");
                        if (chain != null && chain != Chain.AND) {
                            throw new MethodDuplicatedChainException(method, repositoryClass);
                        }
                        chain = Chain.AND;
                    } else if (loweredStrip.startsWith("or")) {
                        loweredStrip = loweredStrip.replaceFirst("or", "");
                        if (chain != null && chain != Chain.OR) {
                            throw new MethodDuplicatedChainException(method, repositoryClass);
                        }
                        chain = Chain.OR;
                    }

                    if (entityField != null) {
                        Validator.validateTypes(repositoryClass, method, entityField, filterOperator, nextParameterIndex);
                    }

                    IndexedFilter indexedFilter = new IndexedFilter(bsonName, notFilter, filterOperator, nextParameterIndex);
                    indexedFilterList.add(indexedFilter);
                    int operatorParameterCount = filterOperator.getExpectedParameterCount();
                    expectedParameterCount += operatorParameterCount;
                    nextParameterIndex = itemCount + operatorParameterCount;
                    itemCount += 1;
                }
                if (chain == null) {
                    chain = Chain.NONE;
                }

                int methodParameterCount = method.getParameterCount();

                // If the method is a pageBy, it needs at least one parameter of type Pagination
                if (methodOperator == MethodOperator.PAGE && methodParameterCount == 0) {
                    throw new MethodPageRequiredException(method, repositoryClass, Pagination.class);
                }

                // Validate the parameterCount of the filters and the method parameters itself.
                if (expectedParameterCount != methodParameterCount) {
                    if (methodParameterCount > 0) {
                        // Subtract 1 from parameterCount. This object could be a Sort or Pagination object.
                        // That means, the expectedParameterCount is less than the actualParameterCount.
                        Class<?> lastMethodParameter = method.getParameterTypes()[methodParameterCount - 1];
                        if (lastMethodParameter.isAssignableFrom(Sort.class)) {
                            if (methodOperator == MethodOperator.PAGE) {
                                throw new MethodSortNotAllowedException(method, repositoryClass);
                            }
                            if ((expectedParameterCount + 1) != methodParameterCount) {
                                throw new MethodParameterCountException(method, repositoryClass, (expectedParameterCount + 1), methodParameterCount);
                            }
                        }
                        if (lastMethodParameter.isAssignableFrom(Pagination.class)) {
                            if (methodOperator != MethodOperator.PAGE) {
                                throw new MethodPageNotAllowedException(method, repositoryClass);
                            }
                            if ((expectedParameterCount + 1) != methodParameterCount) {
                                throw new MethodParameterCountException(method, repositoryClass, (expectedParameterCount + 1), methodParameterCount);
                            }
                        }
                        if (lastMethodParameter.isAssignableFrom(UpdateBatch.class)) {
                            if (methodOperator != MethodOperator.UPDATE_FIELD) {
                                throw new MethodBatchNotAllowedException(method, repositoryClass);
                            }
                            if ((expectedParameterCount + 1) != methodParameterCount) {
                                throw new MethodParameterCountException(method, repositoryClass, (expectedParameterCount + 1), methodParameterCount);
                            }
                        }
                    } else {
                        throw new MethodParameterCountException(method, repositoryClass, expectedParameterCount, methodParameterCount);
                    }
                }

                // Check if the method has the Sort annotation set.
                SortBy sortAnnotation = method.getAnnotation(SortBy.class);
                if (sortAnnotation != null) {
                    if (methodOperator == MethodOperator.PAGE) {
                        throw new MethodSortNotAllowedException(method, repositoryClass);
                    }
                    String sortFieldName = sortAnnotation.field();
                    Field field = FieldUtils.findFieldByName(sortFieldName, entityFieldSet);
                    if (field == null) {
                        throw new MethodSortFieldNotFoundException(sortFieldName, method, entityClass, repositoryClass);
                    }
                }
                if (methodParameterCount > 0) {
                    Class<?> lastMethodParameter = method.getParameterTypes()[methodParameterCount - 1];
                    // Check if both Sort types are used.
                    // This is not allowed, even if it is possible internally.
                    // En2do is capable of handling it, but I don't know the result.
                    // Probably one sort would get an override by the other one.
                    boolean hasAnySortAnnotation = method.isAnnotationPresent(Limit.class)
                        || method.isAnnotationPresent(Skip.class)
                        || method.isAnnotationPresent(SortBy.class)
                        || method.isAnnotationPresent(SortByArray.class);
                    if (hasAnySortAnnotation && lastMethodParameter.isAssignableFrom(Sort.class)) {
                        throw new MethodMixedSortException(method, repositoryClass, Sort.class, SortBy.class);
                    }
                    // We can't check the field, because it's a parameter, we can only check it, on executing
                    // while runtime, so good luck to you my dear ;)
                }

                IndexedMethod<E, ID, R> dynamicMethod = new IndexedMethod<>(
                    method, methodOperator, chain,
                    methodDefinedEntityCount,
                    indexedFilterList, repositoryData);
                repositoryData.registerDynamicMethod(methodName, dynamicMethod);
            }

            // Drop all entities on start if annotation is present.
            if (repositoryClass.isAnnotationPresent(DropEntitiesOnStart.class)) {
                entityCollection.drop();
            }

            // Drop all indexes on start if annotation is present.
            if (repositoryClass.isAnnotationPresent(DropIndexesOnStart.class)) {
                entityCollection.dropIndexes();
            }

            parser.parseIndices(repositoryClass, entityClass, entityCollection);

            ///////////////////////////
            //                       //
            // Validation successful //
            //                       //
            ///////////////////////////

            // Create dynamic repository proxy object
            ClassLoader repoClassLoader = repositoryClass.getClassLoader();
            Class<?>[] interfaces = new Class[]{repositoryClass};
            Repository<E, ID> repository = (Repository<E, ID>) Proxy.newProxyInstance(repoClassLoader, interfaces,
                new RepositoryInvocationHandler<>(repositoryData, executorService, predefinedMethodRegistry));

            repositoryDataByClassMap.put(repositoryClass, repositoryData);

            repositoryByClassRegistry.put(repositoryClass, repository);
            return (R) repository;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> MongoManager registerCodec(Codec<T> typeCodec) {
        internalPropertyCodecProvider.registerCodec(typeCodec.getEncoderClass(), typeCodec);
        return this;
    }

    public MongoManager applySettings(SettingsBuilder newBuilder) {
        settingsBuilder.merge(newBuilder);
        applyLoggerLevel();
        return this;
    }

    private void applyLoggerLevel() {
        Level loggerLevel = settingsBuilder.getMongoLoggerLevel();
        if (loggerLevel == null) {
            return;
        }
        Logger.getLogger("org.mongodb").setLevel(loggerLevel);
        Logger.getLogger("com.mongodb").setLevel(loggerLevel);
    }
}
