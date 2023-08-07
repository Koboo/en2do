package eu.koboo.en2do;

import eu.koboo.en2do.cache.Cache;
import eu.koboo.en2do.cache.CacheGet;
import eu.koboo.en2do.cache.CachePut;
import eu.koboo.en2do.cache.CacheRemove;
import org.redisson.api.RMapCache;

public class RedissonCache implements Cache {

    RMapCache<Object, Object> cache;

    public RedissonCache(RMapCache<Object, Object> cache) {
        this.cache = cache;
    }

    @Override
    public void addObject(CachePut cachePut, Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object getObject(CacheGet cacheGet, Object key) {
        return cache.get(key);
    }

    @Override
    public Object removeObject(CacheRemove cacheRemove, Object key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
