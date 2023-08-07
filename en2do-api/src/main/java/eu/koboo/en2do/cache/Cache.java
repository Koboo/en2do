package eu.koboo.en2do.cache;

public interface Cache {

    void addObject(CachePut cachePut, Object key, Object value);

    Object getObject(CacheGet cacheGet, Object key);

    Object removeObject(CacheRemove cacheRemove, Object key);

    void clear();
}
