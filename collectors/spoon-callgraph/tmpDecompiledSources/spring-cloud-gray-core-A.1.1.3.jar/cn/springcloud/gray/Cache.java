/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray;

import java.util.Map;
import java.util.function.Function;

public interface Cache<K, V> {
    public V getIfPresent(K var1);

    public V get(K var1, Function<? super K, ? extends V> var2);

    public Map<K, V> getAllPresent(Iterable<K> var1);

    public void put(K var1, V var2);

    public void putAll(Map<? extends K, ? extends V> var1);

    public void invalidate(Object var1);

    public void invalidateAll(Iterable<?> var1);

    public void invalidateAll();
}

