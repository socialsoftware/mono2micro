/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.style.ToStringCreator
 *  org.springframework.util.Assert
 *  org.springframework.util.ObjectUtils
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.origin.Origin;
import cn.springcloud.gray.bean.origin.OriginProvider;
import cn.springcloud.gray.bean.origin.OriginTrackedValue;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public final class ConfigurationProperty
implements OriginProvider,
Comparable<ConfigurationProperty> {
    private final ConfigurationPropertyName name;
    private final Object value;
    private final Origin origin;

    public ConfigurationProperty(ConfigurationPropertyName name, Object value, Origin origin) {
        Assert.notNull((Object)name, (String)"Name must not be null");
        Assert.notNull((Object)value, (String)"Value must not be null");
        this.name = name;
        this.value = value;
        this.origin = origin;
    }

    public ConfigurationPropertyName getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public Origin getOrigin() {
        return this.origin;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        ConfigurationProperty other = (ConfigurationProperty)obj;
        boolean result = true;
        result = result && ObjectUtils.nullSafeEquals((Object)this.name, (Object)other.name);
        result = result && ObjectUtils.nullSafeEquals((Object)this.value, (Object)other.value);
        return result;
    }

    public int hashCode() {
        int result = ObjectUtils.nullSafeHashCode((Object)this.name);
        result = 31 * result + ObjectUtils.nullSafeHashCode((Object)this.value);
        return result;
    }

    public String toString() {
        return new ToStringCreator((Object)this).append("name", (Object)this.name).append("value", this.value).append("origin", (Object)this.origin).toString();
    }

    @Override
    public int compareTo(ConfigurationProperty other) {
        return this.name.compareTo(other.name);
    }

    static ConfigurationProperty of(ConfigurationPropertyName name, OriginTrackedValue value) {
        if (value == null) {
            return null;
        }
        return new ConfigurationProperty(name, value.getValue(), value.getOrigin());
    }

    static ConfigurationProperty of(ConfigurationPropertyName name, Object value, Origin origin) {
        if (value == null) {
            return null;
        }
        return new ConfigurationProperty(name, value, origin);
    }
}

