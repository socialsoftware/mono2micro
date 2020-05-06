/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.bind;

@FunctionalInterface
public interface PlaceholdersResolver {
    public static final PlaceholdersResolver NONE = value -> value;

    public Object resolvePlaceholders(Object var1);
}

