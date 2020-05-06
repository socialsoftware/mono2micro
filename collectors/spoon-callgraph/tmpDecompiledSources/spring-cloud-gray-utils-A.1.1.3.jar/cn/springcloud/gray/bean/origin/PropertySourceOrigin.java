/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.env.PropertySource
 *  org.springframework.util.Assert
 */
package cn.springcloud.gray.bean.origin;

import cn.springcloud.gray.bean.origin.Origin;
import cn.springcloud.gray.bean.origin.OriginLookup;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

public class PropertySourceOrigin
implements Origin {
    private final PropertySource<?> propertySource;
    private final String propertyName;

    public PropertySourceOrigin(PropertySource<?> propertySource, String propertyName) {
        Assert.notNull(propertySource, (String)"PropertySource must not be null");
        Assert.hasLength((String)propertyName, (String)"PropertyName must not be empty");
        this.propertySource = propertySource;
        this.propertyName = propertyName;
    }

    public PropertySource<?> getPropertySource() {
        return this.propertySource;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public String toString() {
        return "\"" + this.propertyName + "\" from property source \"" + this.propertySource.getName() + "\"";
    }

    public static Origin get(PropertySource<?> propertySource, String name) {
        Origin origin = OriginLookup.getOrigin(propertySource, name);
        return origin != null ? origin : new PropertySourceOrigin(propertySource, name);
    }
}

