package eu.koboo.en2do;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import eu.koboo.en2do.internal.dynamicmethods.FilterType;
import eu.koboo.en2do.internal.dynamicmethods.MethodFilterPart;
import eu.koboo.en2do.internal.operators.FilterOperator;
import eu.koboo.en2do.internal.operators.MethodOperator;
import eu.koboo.en2do.mongodb.RepositoryInvocationHandler;
import eu.koboo.en2do.mongodb.RepositoryMeta;
import eu.koboo.en2do.mongodb.Validator;
import eu.koboo.en2do.mongodb.codec.InternalPropertyCodecProvider;
import eu.koboo.en2do.mongodb.convention.AnnotationConvention;
import eu.koboo.en2do.mongodb.exception.methods.*;
import eu.koboo.en2do.mongodb.exception.repository.*;
import eu.koboo.en2do.mongodb.methods.dynamic.MongoDynamicMethod;
import eu.koboo.en2do.mongodb.methods.predefined.impl.*;
import eu.koboo.en2do.repository.AsyncRepository;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.repository.entity.compound.CompoundIndex;
import eu.koboo.en2do.repository.entity.compound.Index;
import eu.koboo.en2do.repository.entity.ttl.TTLIndex;
import eu.koboo.en2do.repository.methods.async.Async;
import eu.koboo.en2do.repository.methods.fields.UpdateBatch;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import eu.koboo.en2do.repository.methods.sort.*;
import eu.koboo.en2do.repository.methods.transform.Transform;
import eu.koboo.en2do.repository.options.DropEntitiesOnStart;
import eu.koboo.en2do.repository.options.DropIndexesOnStart;
import eu.koboo.en2do.utility.AnnotationUtils;
import eu.koboo.en2do.utility.FieldUtils;
import eu.koboo.en2do.utility.GenericUtils;
import eu.koboo.en2do.utility.MethodUtils;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class MongoManager extends DatabaseManager {

    Map<Class<?>, RepositoryMeta<?, ?, ?>> repositoryMetaRegistry;
    ExecutorService executorService;

    InternalPropertyCodecProvider internalPropertyCodecProvider;
    @Getter
    CodecRegistry codecRegistry;
    MongoClient client;
    MongoDatabase database;

    public MongoManager(Credentials credentials, ExecutorService executorService) {
        this.repositoryMetaRegistry = new ConcurrentHashMap<>();
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

        internalPropertyCodecProvider = new InternalPropertyCodecProvider();

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
    }

    public MongoManager(Credentials credentials) {
        this(credentials, null);
    }

    public MongoManager() {
        this(null, null);
    }

    @Override
    public void start() {
        if (MongoSettings.hasSetting(MongoSettings.DISABLE_LOGGER)) {
            Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);
        }
    }

    public void close() {
        close(true);
    }

    @Override
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

    @SuppressWarnings("unchecked")
    public <E, ID, R extends Repository<E, ID>> R create(Class<R> repositoryClass) {
        try {

            // Check for already created repository to avoid multiply instances of the same repository
            if (repositoryRegistry.containsKey(repositoryClass)) {
                return (R) repositoryRegistry.get(repositoryClass);
            }

            // Parse annotated collection name and create pojo-related mongo collection
            Collection collectionAnnotation = repositoryClass.getAnnotation(Collection.class);
            if (collectionAnnotation == null) {
                throw new RepositoryNameNotFoundException(repositoryClass, Collection.class);
            }

            // Check if the collection name is valid and for duplication issues
            String entityCollectionName = collectionAnnotation.value();
            if (entityCollectionName.trim().equalsIgnoreCase("")) {
                throw new RepositoryNameNotFoundException(repositoryClass, Collection.class);
            }
            for (RepositoryMeta<?, ?, ?> meta : repositoryMetaRegistry.values()) {
                if (meta.getCollectionName().equalsIgnoreCase(entityCollectionName)) {
                    throw new RepositoryNameDuplicateException(repositoryClass, Collection.class);
                }
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

            Validator.validateCompatibility(repositoryClass, entityClass);

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

            // Creating the collection and the repository metaobjects.
            MongoCollection<E> entityCollection = database.getCollection(entityCollectionName, entityClass);
            RepositoryMeta<E, ID, R> repositoryMeta = new RepositoryMeta<>(
                repositoryClass, entityClass,
                entityFieldSet,
                entityIdClass, entityUniqueIdField,
                entityCollection, entityCollectionName
            );

            // Define default methods with handler into the meta registry
            repositoryMeta.registerPredefinedMethod(new MethodCountAll<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodDelete<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodDeleteAll<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodDeleteById<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodDrop<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodEquals<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodExists<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodExistsById<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodFindAll<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodFindFirstById<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodGetClass<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodGetCollectionName<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodGetEntityClass<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodGetEntityUniqueIdClass<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodGetUniqueId<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodHashCode<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodPageAll<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodSave<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodSaveAll<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodSortAll<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodToString<>(repositoryMeta, entityCollection));
            repositoryMeta.registerPredefinedMethod(new MethodUpdateAllFields<>(repositoryMeta, entityCollection));

            // Iterate through the repository methods
            for (Method method : repositoryClass.getMethods()) {
                String methodName = method.getName();

                // Apply transform annotation
                Transform transform = method.getAnnotation(Transform.class);
                if (transform != null) {
                    methodName = transform.value();
                }

                // Check if we catch a predefined method
                if (repositoryMeta.isRepositoryMethod(methodName)) {
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
                        String predefinedName = repositoryMeta.getPredefinedNameByAsyncName(methodName);
                        if (repositoryMeta.isRepositoryMethod(predefinedName)) {
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

                // Check the returnTypes by using the predefined validator.
                methodOperator.validate(method, returnType, entityClass, repositoryClass);

                // Remove the leading methodOperator to ensure it doesn't trick the validation
                String methodNameWithoutOperator = methodOperator.removeOperatorFrom(methodName);
                if (methodName.contains("And") && methodName.contains("Or")) {
                    throw new MethodDuplicatedChainException(method, repositoryClass);
                }

                // Split the "parts" of the methods by "And" or "Or" keywords, because both in one query are not allowed.
                // That's currently the only way to get every filter part.
                boolean multipleFilter = methodNameWithoutOperator.contains("And") || methodNameWithoutOperator.contains("Or");
                boolean andFilter = methodNameWithoutOperator.contains("And");
                String[] methodFilterPartArray;
                if (andFilter) {
                    methodFilterPartArray = methodNameWithoutOperator.split("And");
                } else {
                    methodFilterPartArray = methodNameWithoutOperator.split("Or");
                }

                // Parse, validate and handle the method name and "compile" it to en2do internal usage objects.

                // Counts for further validation
                int expectedParameterCount = 0;
                int nextParameterIndex = 0;
                int itemCount = 0;
                List<MethodFilterPart> filterPartList = new LinkedList<>();
                for (String filterOperatorString : methodFilterPartArray) {

                    // Create the FilterType using the following paring method
                    FilterType filterType = createFilterType(entityClass, repositoryClass, method, filterOperatorString,
                        entityFieldSet);
                    int filterTypeParameterCount = filterType.getOperator().getExpectedParameterCount();

                    // Validate the parameter count and types of the respective filter type
                    for (int i = 0; i < filterTypeParameterCount; i++) {
                        int paramIndex = nextParameterIndex + i;
                        Class<?> paramClass = method.getParameters()[paramIndex].getType();
                        if (paramClass == null) {
                            throw new MethodParameterNotFoundException(method, repositoryClass, (paramIndex + filterTypeParameterCount),
                                method.getParameterCount());
                        }

                        // Special checks for some operators
                        Field field = filterType.getField();
                        Class<?> fieldClass = field.getType();
                        switch (filterType.getOperator()) {
                            case REGEX:
                                // Regex filter allows two types as parameters.
                                if (GenericUtils.isNotTypeOf(String.class, paramClass) && GenericUtils.isNotTypeOf(Pattern.class, paramClass)) {
                                    throw new MethodInvalidRegexParameterException(method, repositoryClass, paramClass);
                                }
                                break;
                            case IN:
                                if (paramClass.isArray()) {
                                    Class<?> arrayType = paramClass.getComponentType();
                                    if (GenericUtils.isNotTypeOf(fieldClass, arrayType)) {
                                        throw new MethodInvalidListParameterException(method, repositoryClass, fieldClass, arrayType);
                                    }
                                    break;
                                }
                                // In filter only allows list. Maybe arrays in future releases.
                                if (!GenericUtils.isNotTypeOf(java.util.Collection.class, paramClass)) {
                                    Class<?> listType = GenericUtils.getGenericTypeOfParameter(method, paramIndex);
                                    if (GenericUtils.isNotTypeOf(fieldClass, listType)) {
                                        throw new MethodInvalidListParameterException(method, repositoryClass, fieldClass, listType);
                                    }
                                    break;
                                }
                                throw new MethodMismatchingTypeException(method, repositoryClass, java.util.Collection.class, paramClass);
                            case HAS_KEY:
                                if (GenericUtils.isNotTypeOf(Map.class, fieldClass)) {
                                    throw new MethodMismatchingTypeException(method, repositoryClass, fieldClass, paramClass);
                                }
                                Class<?> keyClass = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                                if (GenericUtils.isNotTypeOf(keyClass, paramClass)) {
                                    throw new MethodMismatchingTypeException(method, repositoryClass, fieldClass, paramClass);
                                }
                                break;
                            case HAS:
                                if (!GenericUtils.isNotTypeOf(java.util.Collection.class, fieldClass)) {
                                    Class<?> listType = GenericUtils.getGenericTypeOfField(field, 0);
                                    if (GenericUtils.isNotTypeOf(paramClass, listType)) {
                                        throw new MethodMismatchingTypeException(method, repositoryClass, fieldClass, paramClass);
                                    }
                                }
                                break;
                            default:
                                if (GenericUtils.isNotTypeOf(fieldClass, paramClass)) {
                                    throw new MethodMismatchingTypeException(method, repositoryClass, fieldClass, paramClass);
                                }
                                break;
                        }
                    }
                    MethodFilterPart filterPart = new MethodFilterPart(filterType, nextParameterIndex);
                    filterPartList.add(filterPart);
                    // Further validation
                    expectedParameterCount += filterTypeParameterCount;
                    nextParameterIndex = itemCount + filterTypeParameterCount;
                    itemCount += 1;
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
                    // while runtime
                }

                MongoDynamicMethod<E, ID, R> dynamicMethod = new MongoDynamicMethod<>(method, methodOperator,
                    multipleFilter, andFilter, filterPartList, repositoryMeta);
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

            ///////////////////////////
            //                       //
            // Validation successful //
            //                       //
            ///////////////////////////

            // Create dynamic repository proxy object
            ClassLoader repoClassLoader = repositoryClass.getClassLoader();
            Class<?>[] interfaces = new Class[]{repositoryClass};
            Repository<E, ID> repository = (Repository<E, ID>) Proxy.newProxyInstance(repoClassLoader, interfaces,
                new RepositoryInvocationHandler<>(repositoryMeta, executorService));
            repositoryRegistry.put(repositoryClass, repository);
            repositoryMetaRegistry.put(repositoryClass, repositoryMeta);
            return (R) repository;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <E> FilterType createFilterType(Class<E> entityClass, Class<?> repoClass,
                                            Method method, String filterOperatorString,
                                            Set<Field> fieldSet) throws Exception {
        FilterOperator filterOperator = FilterOperator.parseFilterEndsWith(filterOperatorString);
        String expectedFieldName = filterOperator.removeOperatorFrom(filterOperatorString);
        boolean notFilter = false;
        if (expectedFieldName.endsWith("Not")) {
            expectedFieldName = expectedFieldName.replaceFirst("Not", "");
            notFilter = true;
        }
        Field field = FieldUtils.findFieldByName(expectedFieldName, fieldSet);
        if (field == null) {
            throw new MethodFieldNotFoundException(expectedFieldName, method, entityClass, repoClass);
        }
        return new FilterType(field, notFilter, filterOperator);
    }

    public <T> MongoManager registerCodec(Class<T> typeClass, Codec<T> typeCodec) {
        internalPropertyCodecProvider.registerCodec(typeClass, typeCodec);
        return this;
    }
}
