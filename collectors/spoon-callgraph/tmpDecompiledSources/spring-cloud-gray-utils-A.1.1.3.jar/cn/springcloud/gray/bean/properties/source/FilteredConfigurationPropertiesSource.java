/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.Assert
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyState;
import java.util.function.Predicate;
import org.springframework.util.Assert;

class FilteredConfigurationPropertiesSource
implements ConfigurationPropertySource {
    private final ConfigurationPropertySource source;
    private final Predicate<ConfigurationPropertyName> filter;

    FilteredConfigurationPropertiesSource(ConfigurationPropertySource source, Predicate<ConfigurationPropertyName> filter) {
        Assert.notNull((Object)source, (String)"Source must not be null");
        Assert.notNull(filter, (String)"Filter must not be null");
        this.source = source;
        this.filter = filter;
    }

    @Override
    public ConfigurationProperty getConfigurationProperty(ConfigurationPropertyName name) {
        boolean filtered = this.getFilter().test(name);
        return filtered ? this.getSource().getConfigurationProperty(name) : null;
    }

    @Override
    public ConfigurationPropertyState containsDescendantOf(ConfigurationPropertyName name) {
        ConfigurationPropertyState result = this.source.containsDescendantOf(name);
        if (result == ConfigurationPropertyState.PRESENT) {
            return ConfigurationPropertyState.UNKNOWN;
        }
        return result;
    }

    @Override
    public Object getUnderlyingSource() {
        return this.source.getUnderlyingSource();
    }

    protected ConfigurationPropertySource getSource() {
        return this.source;
    }

    protected Predicate<ConfigurationPropertyName> getFilter() {
        return this.filter;
    }

    public String toString() {
        return this.source.toString() + " (filtered)";
    }
}

