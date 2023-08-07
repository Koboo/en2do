package eu.koboo.en2do;

import eu.koboo.en2do.cache.Cache;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class DatabaseManager {

    Map<Class<?>, Repository<?, ?>> repositoryRegistry;

    public DatabaseManager() {
        this.repositoryRegistry = new ConcurrentHashMap<>();
    }

    public abstract <E, ID, R extends Repository<E, ID>> R create(Class<R> repositoryClass);

    public abstract void registerCache(Class<?> repositoryClass, Cache cache);

    public abstract List<Cache> getAllCaches(Class<?> repositoryClass);

    public abstract void connect();

    public abstract void close(boolean shutdownExecutorService);
}