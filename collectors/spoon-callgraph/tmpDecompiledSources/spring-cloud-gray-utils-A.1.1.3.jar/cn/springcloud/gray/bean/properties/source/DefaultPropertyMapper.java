/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.ObjectUtils
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.PropertyMapper;
import cn.springcloud.gray.bean.properties.source.PropertyMapping;
import org.springframework.util.ObjectUtils;

final class DefaultPropertyMapper
implements PropertyMapper {
    public static final PropertyMapper INSTANCE = new DefaultPropertyMapper();
    private LastMapping<ConfigurationPropertyName> lastMappedConfigurationPropertyName;
    private LastMapping<String> lastMappedPropertyName;

    private DefaultPropertyMapper() {
    }

    @Override
    public PropertyMapping[] map(ConfigurationPropertyName configurationPropertyName) {
        LastMapping<ConfigurationPropertyName> last = this.lastMappedConfigurationPropertyName;
        if (last != null && last.isFrom(configurationPropertyName)) {
            return last.getMapping();
        }
        String convertedName = configurationPropertyName.toString();
        PropertyMapping[] mapping = new PropertyMapping[]{new PropertyMapping(convertedName, configurationPropertyName)};
        this.lastMappedConfigurationPropertyName = new LastMapping<ConfigurationPropertyName>(configurationPropertyName, mapping);
        return mapping;
    }

    @Override
    public PropertyMapping[] map(String propertySourceName) {
        LastMapping<String> last = this.lastMappedPropertyName;
        if (last != null && last.isFrom(propertySourceName)) {
            return last.getMapping();
        }
        PropertyMapping[] mapping = this.tryMap(propertySourceName);
        this.lastMappedPropertyName = new LastMapping<String>(propertySourceName, mapping);
        return mapping;
    }

    private PropertyMapping[] tryMap(String propertySourceName) {
        try {
            ConfigurationPropertyName convertedName = ConfigurationPropertyName.adapt(propertySourceName, '.');
            if (!convertedName.isEmpty()) {
                return new PropertyMapping[]{new PropertyMapping(propertySourceName, convertedName)};
            }
        }
        catch (Exception convertedName) {
            // empty catch block
        }
        return NO_MAPPINGS;
    }

    private static class LastMapping<T> {
        private final T from;
        private final PropertyMapping[] mapping;

        LastMapping(T from, PropertyMapping[] mapping) {
            this.from = from;
            this.mapping = mapping;
        }

        public boolean isFrom(T from) {
            return ObjectUtils.nullSafeEquals(from, this.from);
        }

        public PropertyMapping[] getMapping() {
            return this.mapping;
        }
    }

}

