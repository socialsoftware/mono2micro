/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {
    public static Logger logger(Class<?> cls) {
        return LoggerFactory.getLogger(cls);
    }
}

