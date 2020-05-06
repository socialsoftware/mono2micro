/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.Assert
 */
package cn.springcloud.gray.bean.properties.source;

import org.springframework.util.Assert;

public class InvalidConfigurationPropertyValueException
extends RuntimeException {
    private final String name;
    private final Object value;
    private final String reason;

    public InvalidConfigurationPropertyValueException(String name, Object value, String reason) {
        super("Property " + name + " with value '" + value + "' is invalid: " + reason);
        Assert.notNull((Object)name, (String)"Name must not be null");
        this.name = name;
        this.value = value;
        this.reason = reason;
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }

    public String getReason() {
        return this.reason;
    }
}

