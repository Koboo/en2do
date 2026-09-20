package eu.koboo.en2do;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.RepositoryInvocationHandler;
import eu.koboo.en2do.mongodb.codec.InternalPropertyCodecProvider;
import eu.koboo.en2do.mongodb.convention.AnnotationConvention;
import eu.koboo.en2do.mongodb.convention.MethodMappingConvention;
import eu.koboo.en2do.mongodb.indexer.MethodIndexer;
import eu.koboo.en2do.mongodb.indexer.RepositoryIndexer;
import eu.koboo.en2do.mongodb.indexparser.IndexParser;
import eu.koboo.en2do.mongodb.methods.predefined.PredefinedMethodRegistry;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.options.DropEntitiesOnStart;
import eu.koboo.en2do.repository.options.DropIndexesOnStart;
import eu.koboo.en2do.utility.parse.ParseUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.UuidRepresentation;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class MongoManager {

    @Getter
    SettingsBuilder settingsBuilder;
    IndexParser parser;

    Map<Class<?>, RepositoryData<?, ?, ?>> repositoryDataByClassMap;
    Map<Class<?>, Repository<?, ?>> repositoryByClassRegistry;
    PredefinedMethodRegistry predefinedMethodRegistry;
    ExecutorService executorService;

    CodecRegistry codecRegistry;

    @Getter
    MongoClient mongoClient;

    @Getter
    MongoDatabase mongoDatabase;

    public MongoManager(SettingsBuilder builder, ExecutorService executorService) {
        if (builder == null) {
            throw new NullPointerException("SettingsBuilder cannot be null.");
        }
        settingsBuilder = builder;

        parser = new IndexParser();
        repositoryDataByClassMap = new ConcurrentHashMap<>();
        repositoryByClassRegistry = new ConcurrentHashMap<>();
        predefinedMethodRegistry = new PredefinedMethodRegistry();
        this.executorService = ParseUtils.parseExecutorService(executorService);

        // Registering user-provided codecs from SettingsBuilder
        InternalPropertyCodecProvider internalPropertyCodecProvider = new InternalPropertyCodecProvider();
        Set<Codec<?>> codecSet = settingsBuilder.getCodecSet();
        if (codecSet != null && !codecSet.isEmpty()) {
            for (Codec<?> codec : codecSet) {
                internalPropertyCodecProvider.registerCodec(codec);
            }
        }

        // Building the native mongodb CodecRegistry
        codecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder()
                .register(internalPropertyCodecProvider)
                .automatic(true)
                .conventions(List.of(
                    Conventions.ANNOTATION_CONVENTION,
                    Conventions.SET_PRIVATE_FIELDS_CONVENTION,
                    Conventions.USE_GETTERS_FOR_SETTERS,
                    new AnnotationConvention(),
                    new MethodMappingConvention(this)
                ))
                .build())
        );

        // Building and validating the given connection string.
        String settingsConnectionString = settingsBuilder.getConnectionString();
        if (settingsConnectionString == null || settingsConnectionString.isEmpty()) {
            throw new NullPointerException("connectionString is null or empty!");
        }
        ConnectionString connectionString = new ConnectionString(settingsConnectionString);
        String database = connectionString.getDatabase();
        if (database == null || database.isEmpty()) {
            throw new NullPointerException("No database provided in connectionString!");
        }

        // Prebuild the native client settings.
        MongoClientSettings.Builder clientSettingsBuilder = MongoClientSettings.builder()
            .applicationName("en2do-client")
            .applyConnectionString(connectionString)
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .codecRegistry(codecRegistry);

        // Apply the user-provided client configurators
        Set<ClientConfigurator> clientConfiguratorSet = settingsBuilder.getClientConfiguratorSet();
        if (clientConfiguratorSet != null && !clientConfiguratorSet.isEmpty()) {
            for (ClientConfigurator clientConfigurator : clientConfiguratorSet) {
                clientConfigurator.configure(clientSettingsBuilder);
            }
        }

        // Build the actual mongodb client and database
        mongoClient = MongoClients.create(clientSettingsBuilder.build());
        mongoDatabase = mongoClient.getDatabase(database);
    }

    public MongoManager(SettingsBuilder settingsBuilder) {
        this(settingsBuilder, null);
    }

    public MongoManager() {
        this(new SettingsBuilder(), null);
    }

    public void close() {
        try {
            if (executorService != null) {
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

            RepositoryIndexer<E, ID, R> repositoryIndexer = new RepositoryIndexer<>(
                this,
                codecRegistry,
                predefinedMethodRegistry,
                repositoryClass
            );
            // Creating the native mongodb collection object,
            // and it's respective repository data object.
            String collectionName = repositoryIndexer.getCollectionName();
            Class<E> entityClass = repositoryIndexer.getEntityClass();
            MongoCollection<E> entityCollection = mongoDatabase.getCollection(collectionName, entityClass);

            RepositoryData<E, ID, R> repositoryData = repositoryIndexer.index(entityCollection);

            // Iterate through the repository methods
            for (Method method : repositoryClass.getDeclaredMethods()) {
                MethodIndexer<E, ID, R> methodIndexer = new MethodIndexer<>(
                    predefinedMethodRegistry,
                    repositoryIndexer,
                    method
                );
                methodIndexer.indexMethod(repositoryData);
            }

            // Drop all entities on start if annotation is present.
            if (repositoryClass.isAnnotationPresent(DropEntitiesOnStart.class)) {
                entityCollection.drop();
            }

            // Drop all indexes on start if annotation is present.
            if (repositoryClass.isAnnotationPresent(DropIndexesOnStart.class)) {
                entityCollection.dropIndexes();
            }

            // Validation successful
            parser.parseIndices(repositoryClass, repositoryData.getEntityMapping(), entityCollection);

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

    /**
     * @return Unmodifiable {@link List} with all
     * registered and created {@link Repository} of this {@link MongoManager}
     */
    public Set<Repository<?, ?>> getAllRepositories() {
        return Set.copyOf(repositoryByClassRegistry.values());
    }

    /**
     * Defines the logger level for the mongodb loggers
     * with the following package prefixes:
     * - "org.mongodb"
     * - "com.mongodb"
     * If you want to customize logging even more, look into the mongodb logging documentation:
     * <a href="https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/logging/">Click here</a>
     */
    public static void updateLoggingLevel(Level level) {
        // Applying loggerLevel to mongo db logger
        Logger.getLogger("org.mongodb").setLevel(level);
        Logger.getLogger("com.mongodb").setLevel(level);
    }
}
