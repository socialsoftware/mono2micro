/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.utils;

import cn.springcloud.gray.decision.factory.GrayDecisionFactory;

public class NameUtils {
    public static final String GENERATED_NAME_PREFIX = "_genkey_";

    private NameUtils() {
        throw new AssertionError((Object)"Must not instantiate utility class.");
    }

    public static String generateName(int i) {
        return GENERATED_NAME_PREFIX + i;
    }

    public static String normalizeDecisionFactoryName(Class<? extends GrayDecisionFactory> clazz) {
        return NameUtils.removeGarbage(clazz.getSimpleName().replace(GrayDecisionFactory.class.getSimpleName(), ""));
    }

    public static <T> String normalizeName(Class<? extends T> clazz, Class<T> cls) {
        return NameUtils.removeGarbage(clazz.getSimpleName().replace(cls.getSimpleName(), ""));
    }

    private static String removeGarbage(String s) {
        int garbageIdx = s.indexOf("$Mockito");
        if (garbageIdx > 0) {
            return s.substring(0, garbageIdx);
        }
        return s;
    }
}

