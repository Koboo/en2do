package eu.koboo.en2do.factory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.factory.mapping.RepositoryMapping;
import eu.koboo.en2do.factory.mapping.RepositoryMappingFactory;
import eu.koboo.en2do.mongodb.RepositoryData;
import eu.koboo.en2do.mongodb.RepositoryInvocationHandler;
import eu.koboo.en2do.mongodb.exception.RepositoryException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryNameDuplicateException;
import eu.koboo.en2do.mongodb.indexer.MethodIndexer;
import eu.koboo.en2do.mongodb.indexparser.IndexParser;
import eu.koboo.en2do.mongodb.methods.predefined.PredefinedMethodRegistry;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.options.DropEntitiesOnStart;
import eu.koboo.en2do.repository.options.DropIndexesOnStart;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bson.codecs.configuration.CodecRegistry;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class RepositoryFactory {

    MongoManager mongoManager;

    IndexParser indexParser;
    PredefinedMethodRegistry predefinedMethodRegistry;
    Map<Class<?>, RepositoryData<?, ?, ?>> repositoryDataByClassMap;
    Map<Class<?>, Repository<?, ?>> repositoryByClassRegistry;

    public RepositoryFactory(MongoManager mongoManager) {
        this.mongoManager = mongoManager;
        this.indexParser = new IndexParser();
        this.predefinedMethodRegistry = new PredefinedMethodRegistry();
        this.repositoryDataByClassMap = new HashMap<>();
        this.repositoryByClassRegistry = new HashMap<>();
    }

    public void close() {
        indexParser.close();
        predefinedMethodRegistry.close();
        for (RepositoryData<?, ?, ?> data : repositoryDataByClassMap.values()) {
            data.close();
        }
        repositoryDataByClassMap.clear();
        repositoryByClassRegistry.clear();
    }

    @SuppressWarnings("unchecked")
    public <E, ID, R extends Repository<E, ID>> R create(Class<R> repositoryClass) {

        if (!Repository.class.isAssignableFrom(repositoryClass)) {
            throw new RepositoryException("repositoryClass is not of type Repository.", repositoryClass);
        }

        // Check for already created repository to avoid multiply instances of the same repository
        Repository<?, ?> rawRepository = repositoryByClassRegistry.get(repositoryClass);
        if (rawRepository != null) {
            if (!repositoryClass.isInstance(rawRepository)) {
                throw new IllegalStateException(rawRepository.getClass().getSimpleName() +
                    " is not of type " + repositoryClass.getSimpleName());
            }
            return repositoryClass.cast(rawRepository);
        }

        String collectionName = FactoryUtils.parseRepositoryName(repositoryClass, mongoManager.getMongoSettings());
        if (repositoryByClassRegistry.values()
            .stream()
            .anyMatch(repo -> repo.getCollectionName().equals(collectionName))) {
            throw new RepositoryNameDuplicateException(repositoryClass, collectionName);
        }

        RepositoryMapping mapping = RepositoryMappingFactory.create(repositoryClass);

        MongoClient mongoClient = mongoManager.getMongoClient();
        CodecRegistry codecRegistry = mongoClient.getCodecRegistry();
        ValidationUtils.validate(codecRegistry, mapping);

        MongoDatabase mongoDatabase = mongoManager.getMongoDatabase();
        MongoCollection<E> entityCollection = mongoDatabase.getCollection(collectionName, mapping.typedEntityClass());
        RepositoryData<E, ID, R> data = new RepositoryData<>(
            mongoManager,
            mapping,
            entityCollection
        );


        // Iterate through the repository methods
        for (Method method : repositoryClass.getDeclaredMethods()) {
            MethodIndexer<E, ID, R> methodIndexer = new MethodIndexer<>(
                predefinedMethodRegistry,
                mapping,
                method
            );
            methodIndexer.indexMethod(data);
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
        indexParser.parseIndices(mapping, entityCollection);

        ExecutorService executorService = mongoManager.getMongoSettings().executorService();

        // Create dynamic repository proxy object
        ClassLoader repoClassLoader = repositoryClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{repositoryClass};
        Repository<E, ID> repository = (Repository<E, ID>) Proxy.newProxyInstance(
            repoClassLoader,
            interfaces,
            new RepositoryInvocationHandler<>(data, executorService, predefinedMethodRegistry));

        repositoryDataByClassMap.put(repositoryClass, data);
        repositoryByClassRegistry.put(repositoryClass, repository);
        return (R) repository;
    }

    public Set<Repository<?, ?>> getAllRepositories() {
        return Set.copyOf(repositoryByClassRegistry.values());
    }
}
