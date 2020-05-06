/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.PropertyMapper;
import cn.springcloud.gray.bean.properties.source.PropertyMapping;
import java.util.Locale;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

final class SystemEnvironmentPropertyMapper
implements PropertyMapper {
    public static final PropertyMapper INSTANCE = new SystemEnvironmentPropertyMapper();

    SystemEnvironmentPropertyMapper() {
    }

    @Override
    public PropertyMapping[] map(ConfigurationPropertyName configurationPropertyName) {
        String legacyName;
        String name = this.convertName(configurationPropertyName);
        if (name.equals(legacyName = this.convertLegacyName(configurationPropertyName))) {
            return new PropertyMapping[]{new PropertyMapping(name, configurationPropertyName)};
        }
        return new PropertyMapping[]{new PropertyMapping(name, configurationPropertyName), new PropertyMapping(legacyName, configurationPropertyName)};
    }

    @Override
    public PropertyMapping[] map(String propertySourceName) {
        ConfigurationPropertyName name = this.convertName(propertySourceName);
        if (name == null || name.isEmpty()) {
            return NO_MAPPINGS;
        }
        return new PropertyMapping[]{new PropertyMapping(propertySourceName, name)};
    }

    private ConfigurationPropertyName convertName(String propertySourceName) {
        try {
            return ConfigurationPropertyName.adapt(propertySourceName, '_', this::processElementValue);
        }
        catch (Exception ex) {
            return null;
        }
    }

    private String convertName(ConfigurationPropertyName name) {
        return this.convertName(name, name.getNumberOfElements());
    }

    private String convertName(ConfigurationPropertyName name, int numberOfElements) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < numberOfElements; ++i) {
            if (result.length() > 0) {
                result.append("_");
            }
            result.append(name.getElement(i, ConfigurationPropertyName.Form.UNIFORM).toUpperCase(Locale.ENGLISH));
        }
        return result.toString();
    }

    private String convertLegacyName(ConfigurationPropertyName name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.getNumberOfElements(); ++i) {
            if (result.length() > 0) {
                result.append("_");
            }
            result.append(this.convertLegacyNameElement(name.getElement(i, ConfigurationPropertyName.Form.ORIGINAL)));
        }
        return result.toString();
    }

    private Object convertLegacyNameElement(String element) {
        return element.replace('-', '_').toUpperCase(Locale.ENGLISH);
    }

    private CharSequence processElementValue(CharSequence value) {
        String result = value.toString().toLowerCase(Locale.ENGLISH);
        return SystemEnvironmentPropertyMapper.isNumber(result) ? "[" + result + "]" : result;
    }

    private static boolean isNumber(String string) {
        return string.chars().allMatch(Character::isDigit);
    }
}

