package eu.koboo.en2do;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.RepositoryInvocationHandler;
import eu.koboo.en2do.mongodb.Validator;
import eu.koboo.en2do.mongodb.codec.InternalPropertyCodecProvider;
import eu.koboo.en2do.mongodb.convention.AnnotationConvention;
import eu.koboo.en2do.mongodb.convention.MethodMappingConvention;
import eu.koboo.en2do.mongodb.exception.methods.*;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryTypeIdNotFoundException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryNameDuplicateException;
import eu.koboo.en2do.mongodb.methods.dynamic.IndexedFilter;
import eu.koboo.en2do.mongodb.methods.dynamic.IndexedMethod;
import eu.koboo.en2do.mongodb.methods.predefined.PredefinedMethodRegistry;
import eu.koboo.en2do.operators.AmountType;
import eu.koboo.en2do.operators.ChainType;
import eu.koboo.en2do.operators.FilterOperator;
import eu.koboo.en2do.operators.MethodOperator;
import eu.koboo.en2do.indexer.MethodIndexer;
import eu.koboo.en2do.parser.repository.RepositoryParser;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.repository.methods.sort.*;
import eu.koboo.en2do.repository.methods.transform.NestedBsonKey;
import eu.koboo.en2do.repository.options.DropEntitiesOnStart;
import eu.koboo.en2do.repository.options.DropIndexesOnStart;
import eu.koboo.en2do.utility.Tuple;
import eu.koboo.en2do.utility.parse.ParseUtils;
import eu.koboo.en2do.utility.reflection.FieldUtils;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
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
    PredefinedMethodRegistry predefinedMethodRegistry;
    ExecutorService executorService;

    InternalPropertyCodecProvider internalPropertyCodecProvider;
    CodecRegistry codecRegistry;

    @Getter
    MongoClient mongoClient;

    @Getter
    MongoDatabase mongoDatabase;

    public MongoManager(String connectString, ExecutorService executorService, SettingsBuilder builder) {
        this.settingsBuilder = ParseUtils.parseSettingsBuilder(builder);
        applyLoggerLevel();

        this.parser = new RepositoryParser(settingsBuilder);
        this.repositoryDataByClassMap = new ConcurrentHashMap<>();
        this.repositoryByClassRegistry = new ConcurrentHashMap<>();
        this.predefinedMethodRegistry = new PredefinedMethodRegistry();
        this.executorService = ParseUtils.parseExecutorService(executorService);

        internalPropertyCodecProvider = new InternalPropertyCodecProvider(this);

        codecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder()
                .register(internalPropertyCodecProvider)
                .automatic(true)
                .conventions(List.of(
                    new AnnotationConvention(),
                    new MethodMappingConvention(this, parser),
                    Conventions.ANNOTATION_CONVENTION,
                    Conventions.SET_PRIVATE_FIELDS_CONVENTION
                ))
                .build())
        );

        ConnectionString connectionString = ParseUtils.parseConnectionString(connectString);
        String database = connectionString.getDatabase();
        if (database == null || database.isEmpty()) {
            throw new NullPointerException("database is null or empty in your connection string!");
        }

        MongoClientSettings.Builder clientSettingsBuilder = MongoClientSettings.builder()
            .applicationName("en2do-client")
            .applyConnectionString(connectionString)
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .codecRegistry(codecRegistry);

        ServerApi serverApi = settingsBuilder.getServerApi();
        if (serverApi != null) {
            clientSettingsBuilder.serverApi(serverApi);
        }

        MongoClientSettings clientSettings = clientSettingsBuilder.build();

        mongoClient = MongoClients.create(clientSettings);
        mongoDatabase = mongoClient.getDatabase(database);
    }

    public MongoManager(String connectString, SettingsBuilder settingsBuilder) {
        this(connectString, null, settingsBuilder);
    }

    public MongoManager(SettingsBuilder settingsBuilder) {
        this(null, null, settingsBuilder);
    }

    public MongoManager(String connectString) {
        this(connectString, null, null);
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

    @SuppressWarnings("unchecked")
    public <E, ID, R extends Repository<E, ID>> R create(Class<R> repositoryClass) {
        try {

            // Check for already created repository to avoid multiply instances of the same repository
            if (repositoryByClassRegistry.containsKey(repositoryClass)) {
                return (R) repositoryByClassRegistry.get(repositoryClass);
            }

            Tuple<Class<?>, Class<?>> typeTuple = ParseUtils.parseGenericTypes(repositoryClass);
            Class<E> entityClass = (Class<E>) typeTuple.getFirst();
            Class<ID> entityIdClass = (Class<ID>) typeTuple.getSecond();

            // Check if the collection name is valid
            String entityCollectionName = ParseUtils.parseCollectionName(
                settingsBuilder,
                repositoryClass,
                entityClass
            );

            // Check if we already got a repository with that name.
            for (RepositoryData<?, ?, ?> meta : repositoryDataByClassMap.values()) {
                if (meta.getCollectionName().equalsIgnoreCase(entityCollectionName)) {
                    throw new RepositoryNameDuplicateException(repositoryClass, entityCollectionName);
                }
            }

            Validator.validateCompatibility(codecRegistry, repositoryClass, entityClass);

            // Collect all fields recursively to ensure we inherited fields too
            Set<Field> entityFieldSet = parser.parseEntityFields(entityClass);

            Field entityUniqueIdField = parser.parseEntityIdField(entityClass);
            if (entityUniqueIdField == null) {
                throw new RepositoryTypeIdNotFoundException(repositoryClass, entityClass);
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

                String methodName = method.getName();
                // Check if we catch a predefined method name
                if (predefinedMethodRegistry.isPredefinedMethod(methodName)) {
                    throw new IllegalStateException("Predefined method '" + methodName + "' has override " +
                        "in repository " + repositoryClass.getName() + "! That's not allowed!");
                }

                MethodIndexer<E, ID, R> methodIndexer = new MethodIndexer<>(repositoryData, method);

                // Parse the MethodOperator by the methodName
                MethodOperator methodOperator = methodIndexer.parseMethodOperator();

                // Parse the defined entity count, by parsing the type using keywords
                AmountType amountType = methodIndexer.parseAmountType();
                long entityAmount = methodIndexer.parseEntityAmount(amountType);

                methodIndexer.removeFilterStartIndicator();

                // Counts for further validation
                int expectedParameterCount = 0;
                int nextParameterIndex = 0;
                int itemCount = 0;

                // The list of all filters from the given method.
                List<IndexedFilter> indexedFilterList = new LinkedList<>();

                // This String represents only the filters and their respective chains.
                // The method operator, "by" keyword and amount types are already stripped.
                methodIndexer.createLoweredParsableMethodName();

                // Chain represents either AND or OR for all filters.
                ChainType chainType = null;

                // We are using a while loop here, until we don't have any filters left.
                // Just to ensure we are not doing infinite loops, we use a specified safeBreakAmount.
                // This also means you can't have more than
                // 200 filters in a single method, which I think is more than enough.
                int safeBreakAmount = 200;
                while (!loweredStrip.equalsIgnoreCase("") && safeBreakAmount > 0) {
                    // Add safe break to avoid infinite loops
                    safeBreakAmount--;

                    String bsonFilterKey = null;
                    // Check if we can find any nested fields
                    for (NestedBsonKey nestedBsonKey : embeddedFieldSet) {
                        String loweredKey = nestedBsonKey.id().toLowerCase(Locale.ROOT);
                        if (!loweredStrip.startsWith(loweredKey)) {
                            continue;
                        }
                        loweredStrip = loweredStrip.replaceFirst(loweredKey, "");
                        bsonFilterKey = nestedBsonKey.bson();
                        break;
                    }

                    // Check if we can find any direct entity fields
                    Field entityField = null;
                    for (Field field : sortedFieldMap.values()) {
                        String fieldName = field.getName();
                        String loweredFieldName = fieldName.toLowerCase(Locale.ROOT);
                        if (!loweredStrip.startsWith(loweredFieldName)) {
                            continue;
                        }
                        loweredStrip = loweredStrip.replaceFirst(loweredFieldName, "");
                        entityField = field;
                        bsonFilterKey = ParseUtils.parseBsonName(field);
                        break;
                    }

                    // Check if we found any key to filter with in bson.
                    if (bsonFilterKey == null) {
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
                        if (chainType != null && chainType != ChainType.AND) {
                            throw new MethodDuplicatedChainException(method, repositoryClass);
                        }
                        chainType = ChainType.AND;
                    } else if (loweredStrip.startsWith("or")) {
                        loweredStrip = loweredStrip.replaceFirst("or", "");
                        if (chainType != null && chainType != ChainType.OR) {
                            throw new MethodDuplicatedChainException(method, repositoryClass);
                        }
                        chainType = ChainType.OR;
                    }

                    if (entityField != null) {
                        Validator.validateParameterTypes(repositoryClass, method, entityField, filterOperator, nextParameterIndex);
                    }

                    IndexedFilter indexedFilter = new IndexedFilter(bsonFilterKey, notFilter, filterOperator, nextParameterIndex);
                    indexedFilterList.add(indexedFilter);
                    int operatorParameterCount = filterOperator.getExpectedParameterCount();
                    expectedParameterCount += operatorParameterCount;
                    nextParameterIndex = itemCount + operatorParameterCount;
                    itemCount += 1;
                }
                if (chainType == null) {
                    chainType = ChainType.NONE;
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
                    method, methodOperator, chainType,
                    -1L,
                    amountType, entityAmount,
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

    public Set<Repository<?, ?>> getAllRepositories() {
        return Set.copyOf(repositoryByClassRegistry.values());
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
