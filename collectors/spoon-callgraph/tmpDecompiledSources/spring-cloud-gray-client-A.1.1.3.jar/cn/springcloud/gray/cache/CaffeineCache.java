/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.Cache
 *  com.github.benmanes.caffeine.cache.Cache
 */
package cn.springcloud.gray.cache;

import cn.springcloud.gray.Cache;
import java.util.Map;
import java.util.function.Function;

public class CaffeineCache<K, V>
implements Cache<K, V> {
    private com.github.benmanes.caffeine.cache.Cache<K, V> cache;

    public CaffeineCache(com.github.benmanes.caffeine.cache.Cache<K, V> cache) {
        this.cache = cache;
    }

    public V getIfPresent(K key) {
        return (V)this.cache.getIfPresent(key);
    }

    public V get(K key, Function<? super K, ? extends V> mappingFunction) {
        return (V)this.cache.get(key, mappingFunction);
    }

    public Map<K, V> getAllPresent(Iterable<K> keys) {
        return this.cache.getAllPresent(keys);
    }

    public void put(K key, V value) {
        this.cache.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        this.cache.putAll(map);
    }

    public void invalidate(Object key) {
        this.cache.invalidate(key);
    }

    public void invalidateAll(Iterable<?> keys) {
        this.cache.invalidateAll(keys);
    }

    public void invalidateAll() {
        this.cache.invalidateAll();
    }
}

