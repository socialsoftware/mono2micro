/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.ResolvableType
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import org.springframework.core.ResolvableType;

public class BindException
extends RuntimeException {
    private final Bindable<?> target;
    private final ConfigurationProperty property;
    private final ConfigurationPropertyName name;

    BindException(ConfigurationPropertyName name, Bindable<?> target, ConfigurationProperty property, Throwable cause) {
        super(BindException.buildMessage(name, target), cause);
        this.name = name;
        this.target = target;
        this.property = property;
    }

    public ConfigurationPropertyName getName() {
        return this.name;
    }

    public Bindable<?> getTarget() {
        return this.target;
    }

    public ConfigurationProperty getProperty() {
        return this.property;
    }

    private static String buildMessage(ConfigurationPropertyName name, Bindable<?> target) {
        StringBuilder message = new StringBuilder();
        message.append("Failed to bind properties");
        message.append(name != null ? " under '" + name + "'" : "");
        message.append(" to ").append((Object)target.getType());
        return message.toString();
    }
}

