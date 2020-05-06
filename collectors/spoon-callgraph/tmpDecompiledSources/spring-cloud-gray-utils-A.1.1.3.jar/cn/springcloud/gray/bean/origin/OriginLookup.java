/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.origin;

import cn.springcloud.gray.bean.origin.Origin;

@FunctionalInterface
public interface OriginLookup<K> {
    public Origin getOrigin(K var1);

    public static <K> Origin getOrigin(Object source, K key) {
        if (!(source instanceof OriginLookup)) {
            return null;
        }
        try {
            return ((OriginLookup)source).getOrigin(key);
        }
        catch (Throwable ex) {
            return null;
        }
    }
}

