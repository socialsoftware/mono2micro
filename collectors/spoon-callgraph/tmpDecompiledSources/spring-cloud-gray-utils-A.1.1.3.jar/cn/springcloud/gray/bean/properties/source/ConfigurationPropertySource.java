/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.AliasedConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyNameAliases;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyState;
import cn.springcloud.gray.bean.properties.source.FilteredConfigurationPropertiesSource;
import java.util.function.Predicate;

@FunctionalInterface
public interface ConfigurationPropertySource {
    public ConfigurationProperty getConfigurationProperty(ConfigurationPropertyName var1);

    default public ConfigurationPropertyState containsDescendantOf(ConfigurationPropertyName name) {
        return ConfigurationPropertyState.UNKNOWN;
    }

    default public ConfigurationPropertySource filter(Predicate<ConfigurationPropertyName> filter) {
        return new FilteredConfigurationPropertiesSource(this, filter);
    }

    default public ConfigurationPropertySource withAliases(ConfigurationPropertyNameAliases aliases) {
        return new AliasedConfigurationPropertySource(this, aliases);
    }

    default public Object getUnderlyingSource() {
        return null;
    }
}

