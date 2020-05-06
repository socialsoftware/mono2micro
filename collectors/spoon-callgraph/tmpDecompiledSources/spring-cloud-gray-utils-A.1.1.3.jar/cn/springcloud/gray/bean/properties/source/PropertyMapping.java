/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;

class PropertyMapping {
    private final String propertySourceName;
    private final ConfigurationPropertyName configurationPropertyName;

    PropertyMapping(String propertySourceName, ConfigurationPropertyName configurationPropertyName) {
        this.propertySourceName = propertySourceName;
        this.configurationPropertyName = configurationPropertyName;
    }

    public String getPropertySourceName() {
        return this.propertySourceName;
    }

    public ConfigurationPropertyName getConfigurationPropertyName() {
        return this.configurationPropertyName;
    }

    public boolean isApplicable(ConfigurationPropertyName name) {
        return this.configurationPropertyName.equals(name);
    }
}

