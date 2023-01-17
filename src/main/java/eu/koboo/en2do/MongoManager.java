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
import eu.koboo.en2do.index.ttl.TTLIndex;
import eu.koboo.en2do.meta.RepositoryInvocationHandler;
import eu.koboo.en2do.meta.RepositoryMeta;
import eu.koboo.en2do.meta.operators.FilterOperator;
import eu.koboo.en2do.meta.operators.MethodOperator;
import eu.koboo.en2do.meta.registry.DynamicMethod;
import eu.koboo.en2do.meta.registry.FilterType;
import eu.koboo.en2do.meta.registry.MethodFilterPart;
import eu.koboo.en2do.meta.options.DropEntitiesOnStart;
import eu.koboo.en2do.meta.options.DropIndexesOnStart;
import eu.koboo.en2do.repository.Transform;
import eu.koboo.en2do.repository.methods.*;
import eu.koboo.en2do.sort.annotation.Limit;
import eu.koboo.en2do.sort.annotation.Skip;
import eu.koboo.en2do.sort.annotation.SortBy;
import eu.koboo.en2do.sort.annotation.SortByArray;
import eu.koboo.en2do.sort.parameter.Sort;
import eu.koboo.en2do.utility.AnnotationUtils;
import eu.koboo.en2do.utility.FieldUtils;
import eu.koboo.en2do.utility.GenericUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.conversions.Bson;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MongoManager {

    // Predefined methods by Java objects
    // These methods are ignored by our method processing proxy / invocation handler.
    private static final List<String> IGNORED_DEFAULT_METHODS = Arrays.asList(
            "toString", "hashCode", "equals", "notify", "notifyAll", "wait", "finalize", "clone"
    );

    MongoClient client;
    MongoDatabase database;
    Map<Class<?>, Repository<?, ?>> repoRegistry;
    Map<Class<?>, RepositoryMeta<?, ?, ?>> repoMetaRegistry;

    public MongoManager(Credentials credentials, PropertyCodecProvider... customCodecs) {

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

        PojoCodecProvider.Builder pojoCodecProvider = PojoCodecProvider.builder()
                .register(new MapCodecProvider())
                .automatic(true);

        // Register custom codecs from users
        if(customCodecs != null) {
            for (PropertyCodecProvider customCodec : customCodecs) {
                pojoCodecProvider.register(customCodec);
            }
        }

        CodecRegistry pojoCodec = CodecRegistries.fromProviders(pojoCodecProvider.build());
        CodecRegistry registry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodec);

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connection)
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(registry)
                .build();

        client = MongoClients.create(clientSettings);
        database = client.getDatabase(databaseString);

        repoRegistry = new ConcurrentHashMap<>();
        repoMetaRegistry = new ConcurrentHashMap<>();
    }

    public MongoManager() {
        this(null);
    }

    public boolean close() {
        try {
            if (repoRegistry != null) {
                repoRegistry.clear();
            }
            if (repoMetaRegistry != null) {
                for (RepositoryMeta<?, ?, ?> meta : repoMetaRegistry.values()) {
                    meta.destroy();
                }
                repoMetaRegistry.clear();
            }
            if (client != null) {
                client.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public <E, ID, R extends Repository<E, ID>> R create(Class<R> repositoryClass) {
        try {

            // Check for already created repository to avoid multiply instances of the same repository
            if (repoRegistry.containsKey(repositoryClass)) {
                return (R) repoRegistry.get(repositoryClass);
            }

            // Parse annotated collection name and create pojo-related mongo collection
            Collection collectionAnnotation = repositoryClass.getAnnotation(Collection.class);
            if (collectionAnnotation == null) {
                throw new RepositoryNameNotFoundException(repositoryClass);
            }
            String entityCollectionName = collectionAnnotation.value();

            // Parse Entity and UniqueId type classes by generic repository arguments
            // (Yea, it's very hacky, but works)
            Type[] repoGenericTypeArray = repositoryClass.getGenericInterfaces();
            Type repoGenericTypeParams = null;
            for (Type type : repoGenericTypeArray) {
                if (type.getTypeName().split("<")[0].equalsIgnoreCase(Repository.class.getName())) {
                    repoGenericTypeParams = type;
                    break;
                }
            }
            if (repoGenericTypeParams == null) {
                throw new RepositoryNoTypeException(repositoryClass);
            }

            // Searching for entity class and after that their declared fields
            Class<E> entityClass;
            try {
                // get class name of generic type arguments
                String entityClassName = repoGenericTypeParams.getTypeName().split("<")[1].split(",")[0];
                entityClass = (Class<E>) Class.forName(entityClassName);

                boolean hasValidConstructor = false;
                Constructor<?>[] entityConstructors = entityClass.getConstructors();
                for (Constructor<?> constructor : entityConstructors) {
                    if(!Modifier.isPublic(constructor.getModifiers())) {
                        continue;
                    }
                    if(constructor.getParameterCount() > 0) {
                        continue;
                    }
                    hasValidConstructor = true;
                }
                if(!hasValidConstructor) {
                    throw new RepositoryEntityConstructorException(entityClass, repositoryClass);
                }
            } catch (ClassNotFoundException e) {
                throw new RepositoryEntityNotFoundException(repositoryClass, e);
            }

            // Collect all fields recursively
            Set<Field> entityFieldSet = FieldUtils.collectFields(entityClass);
            if (entityFieldSet.size() == 0) {
                throw new RepositoryNoFieldsException(repositoryClass);
            }

            // Predefine some variables for further validation
            Set<String> entityFieldNameSet = new HashSet<>();
            Class<ID> tempEntityUniqueIdClass = null;
            Field tempEntityUniqueIdField = null;
            for (Field field : entityFieldSet) {

                // Check for duplicated lower-case field names
                String lowerFieldName = field.getName().toLowerCase(Locale.ROOT);
                if (entityFieldNameSet.contains(lowerFieldName)) {
                    throw new RepositoryDuplicatedFieldException(field, repositoryClass);
                }
                entityFieldNameSet.add(lowerFieldName);

                // Check if field has final declaration
                if (Modifier.isFinal(field.getModifiers())) {
                    throw new RepositoryFinalFieldException(field, repositoryClass);
                }

                // Check for @Id annotation to find unique identifier of entity
                if (!field.isAnnotationPresent(Id.class)) {
                    continue;
                }
                tempEntityUniqueIdClass = (Class<ID>) field.getType();
                tempEntityUniqueIdField = field;
                tempEntityUniqueIdField.setAccessible(true);
            }
            // Check if we found any unique identifier.
            if (tempEntityUniqueIdClass == null) {
                throw new RepositoryIdNotFoundException(entityClass);
            }
            Class<ID> entityUniqueIdClass = tempEntityUniqueIdClass;
            Field entityUniqueIdField = tempEntityUniqueIdField;
            entityFieldNameSet.clear();

            RepositoryMeta<E, ID, R> repositoryMeta = new RepositoryMeta<>(
                    repositoryClass, entityClass,
                    entityFieldSet,
                    entityUniqueIdClass, entityUniqueIdField,
                    entityCollectionName
            );

            MongoCollection<E> entityCollection = database.getCollection(entityCollectionName, entityClass);

            // Define default methods with handler into the meta registry
            repositoryMeta.registerHandler(new MethodCountAll<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodGetCollectionName<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodGetClass<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodGetEntityClass<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodGetEntityUniqueIdClass<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodGetUniqueId<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodFindFirstById<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodFindAll<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodDelete<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodDeleteById<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodDeleteAll<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodDrop<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodSave<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodSaveAll<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodExists<>(repositoryMeta, entityCollection));
            repositoryMeta.registerHandler(new MethodExistsById<>(repositoryMeta, entityCollection));

            // Iterate through the repository methods
            for (Method method : repositoryClass.getMethods()) {
                String methodName = method.getName();

                Transform transform = method.getAnnotation(Transform.class);
                if (transform != null) {
                    methodName = transform.value();
                }

                if (repositoryMeta.isRepositoryMethod(methodName)) {
                    continue;
                }

                // Skip if the method should be ignored
                if (IGNORED_DEFAULT_METHODS.contains(methodName)) {
                    continue;
                }
                // Check for the return-types of the methods, and their defined names to match our pattern.
                Class<?> returnType = method.getReturnType();

                // Parse the MethodOperator by the methodName
                MethodOperator methodOperator = MethodOperator.parseMethodStartsWith(methodName);
                if (methodOperator == null) {
                    throw new MethodNoMethodOperatorException(method, repositoryClass);
                }

                // Check the returnTypes by using the predefined validator.
                methodOperator.validate(method, returnType, entityClass, repositoryClass);

                String methodNameWithoutOperator = methodOperator.removeOperatorFrom(methodName);
                if (methodName.contains("And") && methodName.contains("Or")) {
                    throw new MethodDuplicatedChainException(method, entityClass);
                }

                boolean multipleFilter = methodNameWithoutOperator.contains("And") || methodNameWithoutOperator.contains("Or");
                boolean andFilter = methodNameWithoutOperator.contains("And");
                String[] methodFilterPartArray;
                if (andFilter) {
                    methodFilterPartArray = methodNameWithoutOperator.split("And");
                } else {
                    methodFilterPartArray = methodNameWithoutOperator.split("Or");
                }

                // Count for further validation
                int expectedParameterCount = 0;

                int nextParameterIndex = 0;
                int itemCount = 0;
                List<MethodFilterPart> filterPartList = new LinkedList<>();
                for (String filterOperatorString : methodFilterPartArray) {
                    FilterType filterType = createFilterType(entityClass, repositoryClass, method, filterOperatorString,
                            entityFieldSet);
                    int filterTypeParameterCount = filterType.operator().getExpectedParameterCount();
                    for (int i = 0; i < filterTypeParameterCount; i++) {
                        int paramIndex = nextParameterIndex + i;
                        Class<?> paramClass = method.getParameters()[paramIndex].getType();
                        if (paramClass == null) {
                            throw new MethodParameterNotFoundException(method, repositoryClass, (paramIndex + filterTypeParameterCount),
                                    method.getParameterCount());
                        }
                        // Special checks for some operators
                        Class<?> fieldClass = filterType.field().getType();
                        switch (filterType.operator()) {
                            case REGEX -> {
                                if (GenericUtils.isNotTypeOf(String.class, paramClass) && GenericUtils.isNotTypeOf(Pattern.class, paramClass)) {
                                    throw new MethodInvalidRegexParameterException(method, repositoryClass, paramClass);
                                }
                            }
                            case IN -> {
                                if (GenericUtils.isNotTypeOf(List.class, paramClass)) {
                                    throw new MethodMismatchingTypeException(method, repositoryClass, List.class, paramClass);
                                }
                                Class<?> listType = GenericUtils.getGenericTypeOfParameterList(method, paramIndex);
                                if (GenericUtils.isNotTypeOf(fieldClass, listType)) {
                                    throw new MethodInvalidListParameterException(method, repositoryClass, fieldClass, listType);
                                }
                            }
                            default -> {
                                if (GenericUtils.isNotTypeOf(fieldClass, paramClass)) {
                                    throw new MethodMismatchingTypeException(method, repositoryClass, fieldClass, paramClass);
                                }
                            }
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
                // Validate the parameterCount of the filters and the method parameters itself.
                if (expectedParameterCount != methodParameterCount) {
                    if (methodParameterCount > 0) {
                        // Subtract 1 from parameterCount. This object could be the Sort object.
                        // That means, the expectedParameterCount is less than the acutalParameterCount.
                        Class<?> lastMethodParameter = method.getParameterTypes()[methodParameterCount - 1];
                        if (lastMethodParameter.isAssignableFrom(Sort.class) && (expectedParameterCount + 1) != methodParameterCount) {
                            throw new MethodParameterCountException(method, repositoryClass, (expectedParameterCount + 1), methodParameterCount);
                        }
                    } else {
                        throw new MethodParameterCountException(method, repositoryClass, expectedParameterCount, methodParameterCount);
                    }
                }

                // Check if the field from sort annotation exists.
                SortBy sortAnnotation = method.getAnnotation(SortBy.class);
                if (sortAnnotation != null) {
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
                        throw new MethodMixedSortException(method, repositoryClass);
                    }
                    // We can't check the field, because it's a parameter, we can only check it, on executing
                    // while runtime
                }

                DynamicMethod<E, ID, R> dynamicMethod = new DynamicMethod<>(method, repositoryMeta, methodOperator,
                        multipleFilter, andFilter, filterPartList);
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

            // Creating an index on the uniqueIdentifier field of the entity to speed up queries,
            // but only if wanted. Users can disable that with the annotation.
            if (!entityUniqueIdField.isAnnotationPresent(NonIndex.class)) {
                entityCollection.createIndex(Indexes.ascending(entityUniqueIdField.getName()), new IndexOptions().unique(true));
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
                    new RepositoryInvocationHandler<>(repositoryMeta, entityCollection));
            repoRegistry.put(repositoryClass, repository);
            repoMetaRegistry.put(repositoryClass, repositoryMeta);
            return (R) repository;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <E> FilterType createFilterType(Class<E> entityClass, Class<?> repoClass, Method method,
                                            String filterOperatorString, Set<Field> fieldSet) throws Exception {
        FilterOperator filterOperator = FilterOperator.parseFilterEndsWith(filterOperatorString);
        if (filterOperator == null) {
            throw new MethodNoFilterOperatorException(method, repoClass);
        }
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
}
