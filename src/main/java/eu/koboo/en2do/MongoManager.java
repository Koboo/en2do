package eu.koboo.en2do;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import eu.koboo.en2do.internal.codec.En2doPropertyCodecProvider;
import eu.koboo.en2do.repository.entity.Id;
import eu.koboo.en2do.repository.entity.NonIndex;
import eu.koboo.en2do.repository.entity.compound.CompoundIndex;
import eu.koboo.en2do.repository.entity.compound.Index;
import eu.koboo.en2do.repository.entity.ttl.TTLIndex;
import eu.koboo.en2do.internal.exception.*;
import eu.koboo.en2do.internal.methods.predefined.impl.*;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.internal.RepositoryInvocationHandler;
import eu.koboo.en2do.internal.RepositoryMeta;
import eu.koboo.en2do.internal.methods.dynamic.DynamicMethod;
import eu.koboo.en2do.internal.methods.dynamic.FilterType;
import eu.koboo.en2do.internal.methods.dynamic.MethodFilterPart;
import eu.koboo.en2do.internal.methods.operators.FilterOperator;
import eu.koboo.en2do.internal.methods.operators.MethodOperator;
import eu.koboo.en2do.repository.methods.sort.*;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.DropEntitiesOnStart;
import eu.koboo.en2do.repository.DropIndexesOnStart;
import eu.koboo.en2do.repository.methods.paging.Pager;
import eu.koboo.en2do.repository.methods.transform.Transform;
import eu.koboo.en2do.utility.AnnotationUtils;
import eu.koboo.en2do.utility.FieldUtils;
import eu.koboo.en2do.utility.GenericUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MongoManager {

    // Predefined methods by Java objects
    // These methods are ignored by our method processing proxy / invocation handler.
    private static final List<String> IGNORED_DEFAULT_METHODS = Arrays.asList(
            "notify", "notifyAll", "wait", "finalize", "clone"
    );

    @Getter
    CodecRegistry codecRegistry;
    MongoClient client;
    MongoDatabase database;
    Map<Class<?>, Repository<?, ?>> repoRegistry;
    Map<Class<?>, RepositoryMeta<?, ?, ?>> repoMetaRegistry;

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

        codecRegistry = fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(
                        PojoCodecProvider.builder()
                                .register(new En2doPropertyCodecProvider())
                                .automatic(true)
                                .build()
                )
        );

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applicationName("en2do-client")
                .applyConnectionString(connection)
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(codecRegistry)
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
                throw new RepositoryNameNotFoundException(repositoryClass, Collection.class);
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
                    if (!Modifier.isPublic(constructor.getModifiers())) {
                        continue;
                    }
                    if (constructor.getParameterCount() > 0) {
                        continue;
                    }
                    hasValidConstructor = true;
                }
                if (!hasValidConstructor) {
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
                throw new RepositoryIdNotFoundException(entityClass, Id.class);
            }
            Class<ID> entityUniqueIdClass = tempEntityUniqueIdClass;
            Field entityUniqueIdField = tempEntityUniqueIdField;
            entityFieldNameSet.clear();

            MongoCollection<E> entityCollection = database.getCollection(entityCollectionName, entityClass);

            RepositoryMeta<E, ID, R> repositoryMeta = new RepositoryMeta<>(
                    repositoryClass, entityClass,
                    entityFieldSet,
                    entityUniqueIdClass, entityUniqueIdField,
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
                // If the method is a pageBy, it needs at least one parameter of type Pager
                if (methodOperator == MethodOperator.PAGE && methodParameterCount == 0) {
                    throw new MethodPageRequiredException(method, repositoryClass, Pager.class);
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
                        if (lastMethodParameter.isAssignableFrom(Pager.class)) {
                            if (methodOperator != MethodOperator.PAGE) {
                                throw new MethodPageNotAllowedException(method, repositoryClass);
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
                    new RepositoryInvocationHandler<>(repositoryMeta));
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
