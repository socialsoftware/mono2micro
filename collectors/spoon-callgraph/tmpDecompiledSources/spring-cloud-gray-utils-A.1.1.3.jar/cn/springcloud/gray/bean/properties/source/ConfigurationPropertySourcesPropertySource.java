/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.env.PropertySource
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.origin.Origin;
import cn.springcloud.gray.bean.origin.OriginLookup;
import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import org.springframework.core.env.PropertySource;

class ConfigurationPropertySourcesPropertySource
extends PropertySource<Iterable<ConfigurationPropertySource>>
implements OriginLookup<String> {
    ConfigurationPropertySourcesPropertySource(String name, Iterable<ConfigurationPropertySource> source) {
        super(name, source);
    }

    public Object getProperty(String name) {
        ConfigurationProperty configurationProperty = this.findConfigurationProperty(name);
        return configurationProperty != null ? configurationProperty.getValue() : null;
    }

    @Override
    public Origin getOrigin(String name) {
        return Origin.from(this.findConfigurationProperty(name));
    }

    private ConfigurationProperty findConfigurationProperty(String name) {
        try {
            return this.findConfigurationProperty(ConfigurationPropertyName.of(name, true));
        }
        catch (Exception ex) {
            return null;
        }
    }

    private ConfigurationProperty findConfigurationProperty(ConfigurationPropertyName name) {
        if (name == null) {
            return null;
        }
        for (ConfigurationPropertySource configurationPropertySource : (Iterable)this.getSource()) {
            ConfigurationProperty configurationProperty = configurationPropertySource.getConfigurationProperty(name);
            if (configurationProperty == null) continue;
            return configurationProperty;
        }
        return null;
    }
}

