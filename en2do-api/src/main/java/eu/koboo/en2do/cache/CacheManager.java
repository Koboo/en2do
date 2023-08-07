package eu.koboo.en2do.cache;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CacheManager {

    Map<Class<?>, List<Cache>> cacheRegistry;

    public CacheManager() {
        this.cacheRegistry = new ConcurrentHashMap<>();
    }

    public void registerCache(Class<?> repositoryClass, Cache cache) {
        List<Cache> cacheList = cacheRegistry.computeIfAbsent(repositoryClass, k -> new LinkedList<>());
        cacheList.add(cache);
    }

    protected List<Cache> getAllCaches(Class<?> repositoryClass) {
        return cacheRegistry.get(repositoryClass);
    }
}
