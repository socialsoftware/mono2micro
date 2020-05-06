/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.Assert
 */
package cn.springcloud.gray.bean.properties.source;

import java.util.function.Predicate;
import org.springframework.util.Assert;

public enum ConfigurationPropertyState {
    PRESENT,
    ABSENT,
    UNKNOWN;
    

    static <T> ConfigurationPropertyState search(Iterable<T> source, Predicate<T> predicate) {
        Assert.notNull(source, (String)"Source must not be null");
        Assert.notNull(predicate, (String)"Predicate must not be null");
        for (T item : source) {
            if (!predicate.test(item)) continue;
            return PRESENT;
        }
        return ABSENT;
    }
}

