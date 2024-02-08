package eu.koboo.en2do;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.Geometry;
import eu.koboo.en2do.mongodb.RepositoryInvocationHandler;
import eu.koboo.en2do.mongodb.RepositoryMeta;
import eu.koboo.en2do.mongodb.Validator;
import eu.koboo.en2do.mongodb.codec.InternalPropertyCodecProvider;
import eu.koboo.en2do.mongodb.convention.AnnotationConvention;
import eu.koboo.en2do.mongodb.exception.methods.*;
import eu.koboo.en2do.mongodb.exception.repository.*;
import eu.koboo.en2do.mongodb.methods.dynamic.IndexedFilter;
import eu.koboo.en2do.mongodb.methods.dynamic.IndexedMethod;
import eu.koboo.en2do.mongodb.methods.predefined.GlobalPredefinedMethod;
import eu.koboo.en2do.mongodb.methods.predefined.impl.*;
import eu.koboo.en2do.operators.Chain;
import eu.koboo.en2do.operators.FilterOperator;
import eu.koboo.en2do.operators.MethodOperator;
import eu.koboo.en2do.repository.AsyncRepository;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.repository.entity.compound.CompoundIndex;
import eu.koboo.en2do.repository.entity.compound.GeoIndex;
import eu.koboo.en2do.repository.entity.compound.Index;
import eu.koboo.en2do.repository.entity.ttl.TTLIndex;
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
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
    SettingsBuilder builder;

    Map<Class<?>, RepositoryMeta<?, ?, ?>> repositoryMetaRegistry;
    Map<Class<?>, Repository<?, ?>> repositoryRegistry;
    Map<String, GlobalPredefinedMethod> predefinedMethodRegistry;
    ExecutorService executorService;

    InternalPropertyCodecProvider internalPropertyCodecProvider;
    @Getter
    CodecRegistry codecRegistry;
    MongoClient client;
    MongoDatabase database;

    public MongoManager(Credentials credentials, ExecutorService executorService, SettingsBuilder builder) {
        if (builder == null) {
            builder = new SettingsBuilder();
        }
        Level loggerLevel = builder.getMongoLoggerLevel();
        if (loggerLevel != null) {
            Logger.getLogger("org.mongodb").setLevel(loggerLevel);
            Logger.getLogger("com.mongodb").setLevel(loggerLevel);
        }
        this.builder = builder;
        this.repositoryMetaRegistry = new ConcurrentHashMap<>();
        this.repositoryRegistry = new ConcurrentHashMap<>();
        this.predefinedMethodRegistry = new LinkedHashMap<>();
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        }
        this.executorService = executorService;

        // If no credentials given, try loading them from default file.
        if (credentials == null) {
            credentials = Credentials.fromFile();
        }
        // If no credentials given, try loading them from default resource.
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
        // If credentials connectString is null, throw exception
        if (connectString == null) {
            throw new NullPointerException("No connectString given! Please make sure to provide a " +
                "accessible connectString.");
        }
        // If credentials databaseString is null, throw exception
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
                    new AnnotationConvention(repositoryMetaRegistry),
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

        client = MongoClients.create(clientSettings);
        database = client.getDatabase(databaseString);

        registerPredefinedMethods();
    }

    public MongoManager(Credentials credentials, SettingsBuilder builder) {
        this(credentials, null, builder);
    }

    public MongoManager(SettingsBuilder builder) {
        this(null, null, builder);
    }

    public MongoManager(Credentials credentials) {
        this(credentials, null, null);
    }

    public MongoManager() {
        this(null, null, null);
    }

    public void start() {
    }

    public void close() {
        close(true);
    }

    public void close(boolean shutdownExecutorService) {
        try {
            if (executorService != null && shutdownExecutorService) {
                executorService.shutdown();
            }
            repositoryRegistry.clear();
            for (RepositoryMeta<?, ?, ?> meta : repositoryMetaRegistry.values()) {
                meta.destroy();
            }
            repositoryMetaRegistry.clear();
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        // Define default methods with handler into the meta registry
        registerPredefinedMethod(new MethodCountAll());
        registerPredefinedMethod(new MethodDelete());
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
            if (repositoryRegistry.containsKey(repositoryClass)) {
                return (R) repositoryRegistry.get(repositoryClass);
            }

            if (!Repository.class.isAssignableFrom(repositoryClass)) {
                throw new RepositoryInvalidException(repositoryClass);
            }

            Map<Class<?>, List<Class<?>>> genericTypes = GenericUtils.getGenericTypes(repositoryClass);
            if (genericTypes.isEmpty()) {
                throw new RepositoryNoTypeException(repositoryClass);
            }

            List<Class<?>> classList = genericTypes.get(Repository.class);
            if (classList == null || classList.size() != 2) {
                throw new RepositoryEntityNotFoundException(repositoryClass);
            }
            Class<E> entityClass = (Class<E>) classList.get(0);
            Class<ID> entityIdClass = (Class<ID>) classList.get(1);


            // Parse annotated collection name and create pojo-related mongo collection
            Collection collectionAnnotation = MongoCollectionUtils.parseAnnotation(repositoryClass, entityClass);
            if (collectionAnnotation == null) {
                throw new RepositoryNameNotFoundException(repositoryClass, Collection.class);
            }

            // Check if the collection name is valid and for duplication issues
            String entityCollectionName = collectionAnnotation.value();
            if (entityCollectionName.trim().equalsIgnoreCase("")) {
                throw new RepositoryNameNotFoundException(repositoryClass, Collection.class);
            }
            entityCollectionName = MongoCollectionUtils.createCollectionName(builder, entityCollectionName);

            for (RepositoryMeta<?, ?, ?> meta : repositoryMetaRegistry.values()) {
                if (meta.getCollectionName().equalsIgnoreCase(entityCollectionName)) {
                    throw new RepositoryNameDuplicateException(repositoryClass, Collection.class);
                }
            }

            if (AsyncRepository.class.isAssignableFrom(repositoryClass)) {
                List<Class<?>> asyncClassList = genericTypes.get(AsyncRepository.class);
                if (asyncClassList != null && asyncClassList.size() == 2) {
                    Class<?> asyncEntityClass = asyncClassList.get(0);
                    if (GenericUtils.isNotTypeOf(asyncEntityClass, entityClass)) {
                        throw new RepositoryInvalidTypeException(entityClass, asyncEntityClass, repositoryClass);
                    }
                    Class<?> asyncIdClass = asyncClassList.get(1);
                    if (GenericUtils.isNotTypeOf(asyncIdClass, entityIdClass)) {
                        throw new RepositoryInvalidTypeException(entityClass, asyncEntityClass, repositoryClass);
                    }
                }
            }

            Validator.validateCompatibility(codecRegistry, repositoryClass, entityClass);

            // Collect all fields recursively to ensure, we'll get the inheritance fields
            Set<Field> entityFieldSet = FieldUtils.collectFields(entityClass);

            // Get the field of the uniqueId of the entity.
            Field tempEntityUniqueIdField = null;
            for (Field field : entityFieldSet) {
                // Check for @Id annotation to find unique identifier of entity
                if (!field.isAnnotationPresent(Id.class)) {
                    continue;
                }
                tempEntityUniqueIdField = field;
                tempEntityUniqueIdField.setAccessible(true);
            }
            // Check if we found any unique identifier.
            if (tempEntityUniqueIdField == null) {
                throw new RepositoryIdNotFoundException(entityClass, Id.class);
            }
            Field entityUniqueIdField = tempEntityUniqueIdField;

            // Creating the collection and the repository meta-objects.
            MongoCollection<E> entityCollection = database.getCollection(entityCollectionName, entityClass);
            RepositoryMeta<E, ID, R> repositoryMeta = new RepositoryMeta<>(this,
                repositoryClass, entityClass,
                entityFieldSet,
                entityIdClass, entityUniqueIdField,
                entityCollection, entityCollectionName
            );

            // Iterate through the repository methods
            for (Method method : repositoryClass.getMethods()) {
                String methodName = method.getName();

                // Apply transform annotation
                Transform transform = method.getAnnotation(Transform.class);
                if (transform != null) {
                    methodName = transform.value();
                }

                // Check if we catch a predefined method
                if (predefinedMethodRegistry.containsKey(methodName)) {
                    continue;
                }

                // Skip if the method should be ignored
                if (MethodUtils.IGNORED_DEFAULT_METHODS.contains(methodName)) {
                    continue;
                }

                // Get the default return type of the method
                Class<?> returnType = method.getReturnType();

                // Check if the method is async and if so, check for completable future return type.
                boolean isAsyncMethod = method.isAnnotationPresent(Async.class);
                if (isAsyncMethod) {
                    // Check async method name
                    if (methodName.startsWith("async")) {
                        String predefinedName = repositoryMeta.stripAsyncName(methodName);
                        if (predefinedMethodRegistry.containsKey(predefinedName)) {
                            continue;
                        }
                        throw new MethodInvalidAsyncNameException(method, repositoryClass);
                    }
                    // Check CompletableFuture return type
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

                // TODO: Save fetched count to indexed method
                // TODO: Replace "By" part of the filter.

                // Check the returnTypes by using the predefined validator.
                methodOperator.validate(method, returnType, entityClass, repositoryClass);

                // Remove the leading methodOperator to ensure it doesn't trick the validation
                String strippedMethodName = methodOperator.removeOperatorFrom(methodName);

                Long methodDefinedEntityCount = null;
                if (strippedMethodName.startsWith("Top")) {
                    strippedMethodName = strippedMethodName.replaceFirst("Top", "");
                    if (strippedMethodName.startsWith("0")) {
                        //TODO: better exception
                        throw new RuntimeException("The number shouldnt start with zero.");
                    }
                    methodDefinedEntityCount = MethodUtils.getPrefixedNumber(strippedMethodName);
                    if (methodDefinedEntityCount == 0) {
                        //TODO: better exception
                        throw new RuntimeException("0 isnt a valid top number");
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

                strippedMethodName = strippedMethodName.replaceFirst("By", "");

                // Parse, validate and handle the method name and "compile" it to en2do internal usage objects.
                Set<NestedField> nestedFieldSet = AnnotationUtils.getNestedKeySet(method);

                // Counts for further validation
                int expectedParameterCount = 0;
                int nextParameterIndex = 0;
                int itemCount = 0;
                List<IndexedFilter> indexedFilterList = new LinkedList<>();
                String loweredStrip = strippedMethodName.toLowerCase(Locale.ROOT);
                Chain chain = null;
                while (!loweredStrip.equalsIgnoreCase("")) {

                    String bsonName = null;
                    for (NestedField nestedField : nestedFieldSet) {
                        bsonName = nestedField.query();
                        String loweredKey = nestedField.key().toLowerCase(Locale.ROOT);
                        if (!loweredStrip.startsWith(loweredKey)) {
                            continue;
                        }
                        loweredStrip = loweredStrip.replaceFirst(loweredKey, "");
                        break;
                    }
                    Field entityField = null;
                    for (Field field : entityFieldSet) {
                        bsonName = FieldUtils.parseBsonName(field);
                        String loweredBsonName = bsonName.toLowerCase(Locale.ROOT);
                        if (!loweredStrip.startsWith(loweredBsonName)) {
                            continue;
                        }
                        loweredStrip = loweredStrip.replaceFirst(loweredBsonName, "");
                        entityField = field;
                        break;
                    }
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
                // If the method is a pageBy, it needs at least one parameter of type Pager
                if (methodOperator == MethodOperator.PAGE && methodParameterCount == 0) {
                    throw new MethodPageRequiredException(method, repositoryClass, Pagination.class);
                }
                // Validate the parameterCount of the filters and the method parameters itself.
                if (expectedParameterCount != methodParameterCount) {
                    if (methodParameterCount > 0) {
                        // Subtract 1 from parameterCount. This object could be a Sort or Pager object.
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

                // Check if the field from sort annotation exists.
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
                    indexedFilterList, repositoryMeta);
                repositoryMeta.registerDynamicMethod(methodName, dynamicMethod);
            }

            // Drop all entities on start if annotation is present.
            if (repositoryClass.isAnnotationPresent(DropEntitiesOnStart.class)) {
                entityCollection.drop();
            }

            // Drop all indexes on start if annotation is present.
            if (repositoryClass.isAnnotationPresent(DropIndexesOnStart.class)) {
                entityCollection.dropIndexes();
            }

            // Create compound indexes
            Set<CompoundIndex> compoundIndexSet = AnnotationUtils.collectAnnotations(entityClass, CompoundIndex.class);
            for (CompoundIndex compoundIndex : compoundIndexSet) {
                // Checking if the field in the annotation exists in the entity class.
                Index[] fieldIndexes = compoundIndex.value();
                for (Index fieldIndex : fieldIndexes) {
                    if (entityFieldSet.stream().map(Field::getName).noneMatch(fieldName -> fieldIndex.value().equalsIgnoreCase(fieldName))) {
                        throw new RepositoryIndexFieldNotFoundException(repositoryClass, fieldIndex.value());
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

            // Create ttl indexes
            Set<TTLIndex> ttlIndexSet = AnnotationUtils.collectAnnotations(entityClass, TTLIndex.class);
            for (TTLIndex ttlIndex : ttlIndexSet) {
                // Checking if the field in the annotation exists in the entity class.
                String ttlField = ttlIndex.value();
                boolean foundTTLField = false;
                for (Field entityField : entityFieldSet) {
                    if (!entityField.getName().equalsIgnoreCase(ttlField)) {
                        continue;
                    }
                    if (GenericUtils.isNotTypeOf(entityField.getType(), Date.class)) {
                        continue;
                    }
                    foundTTLField = true;
                    break;
                }
                if (!foundTTLField) {
                    throw new RepositoryTTLFieldNotFoundException(repositoryClass, ttlField);
                }
                IndexOptions indexOptions = new IndexOptions()
                    .expireAfter(ttlIndex.ttl(), ttlIndex.time());
                entityCollection.createIndex(Indexes.ascending(ttlField), indexOptions);
            }

            // Create geo index
            for (Field field : entityFieldSet) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)
                    || Modifier.isFinal(modifiers)
                    || Modifier.isTransient(modifiers)) {
                    continue;
                }

                System.out.println(Geometry.class.isAssignableFrom(field.getType()));
                if (!Geometry.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                GeoIndex geoIndex = field.getAnnotation(GeoIndex.class);
                if (geoIndex == null) {
                    continue;
                }
                String fieldName = FieldUtils.parseBsonName(field);
                Bson indexBson;
                if (geoIndex.sphere()) {
                    indexBson = Indexes.geo2dsphere(fieldName);
                } else {
                    indexBson = Indexes.geo2d(fieldName);
                }
                entityCollection.createIndex(indexBson);
            }

            ///////////////////////////
            //                       //
            // Validation successful //
            //                       //
            ///////////////////////////

            // Create dynamic repository proxy object
            ClassLoader repoClassLoader = repositoryClass.getClassLoader();
            Class<?>[] interfaces = new Class[]{repositoryClass};
            Repository<E, ID> repository = (Repository<E, ID>) Proxy.newProxyInstance(repoClassLoader, interfaces,
                new RepositoryInvocationHandler<>(repositoryMeta, executorService, predefinedMethodRegistry));
            repositoryRegistry.put(repositoryClass, repository);
            repositoryMetaRegistry.put(repositoryClass, repositoryMeta);
            return (R) repository;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> MongoManager registerCodec(Codec<T> typeCodec) {
        internalPropertyCodecProvider.registerCodec(typeCodec.getEncoderClass(), typeCodec);
        return this;
    }
}
