/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.env.EnumerablePropertySource
 *  org.springframework.core.env.PropertySource
 *  org.springframework.core.env.SystemEnvironmentPropertySource
 *  org.springframework.util.Assert
 *  org.springframework.util.ObjectUtils
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.origin.Origin;
import cn.springcloud.gray.bean.origin.PropertySourceOrigin;
import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyState;
import cn.springcloud.gray.bean.properties.source.DefaultPropertyMapper;
import cn.springcloud.gray.bean.properties.source.PropertyMapper;
import cn.springcloud.gray.bean.properties.source.PropertyMapping;
import cn.springcloud.gray.bean.properties.source.SpringIterableConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.SystemEnvironmentPropertyMapper;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

class SpringConfigurationPropertySource
implements ConfigurationPropertySource {
    private static final ConfigurationPropertyName RANDOM = ConfigurationPropertyName.of("random");
    private final PropertySource<?> propertySource;
    private final PropertyMapper mapper;
    private final Function<ConfigurationPropertyName, ConfigurationPropertyState> containsDescendantOf;

    SpringConfigurationPropertySource(PropertySource<?> propertySource, PropertyMapper mapper, Function<ConfigurationPropertyName, ConfigurationPropertyState> containsDescendantOf) {
        Assert.notNull(propertySource, (String)"PropertySource must not be null");
        Assert.notNull((Object)mapper, (String)"Mapper must not be null");
        this.propertySource = propertySource;
        this.mapper = mapper instanceof DelegatingPropertyMapper ? mapper : new DelegatingPropertyMapper(mapper);
        this.containsDescendantOf = containsDescendantOf != null ? containsDescendantOf : n -> ConfigurationPropertyState.UNKNOWN;
    }

    @Override
    public ConfigurationProperty getConfigurationProperty(ConfigurationPropertyName name) {
        PropertyMapping[] mappings = this.getMapper().map(name);
        return this.find(mappings, name);
    }

    @Override
    public ConfigurationPropertyState containsDescendantOf(ConfigurationPropertyName name) {
        return this.containsDescendantOf.apply(name);
    }

    @Override
    public Object getUnderlyingSource() {
        return this.propertySource;
    }

    protected final ConfigurationProperty find(PropertyMapping[] mappings, ConfigurationPropertyName name) {
        for (PropertyMapping candidate : mappings) {
            ConfigurationProperty result;
            if (!candidate.isApplicable(name) || (result = this.find(candidate)) == null) continue;
            return result;
        }
        return null;
    }

    private ConfigurationProperty find(PropertyMapping mapping) {
        String propertySourceName = mapping.getPropertySourceName();
        Object value = this.getPropertySource().getProperty(propertySourceName);
        if (value == null) {
            return null;
        }
        ConfigurationPropertyName configurationPropertyName = mapping.getConfigurationPropertyName();
        Origin origin = PropertySourceOrigin.get(this.propertySource, propertySourceName);
        return ConfigurationProperty.of(configurationPropertyName, value, origin);
    }

    protected PropertySource<?> getPropertySource() {
        return this.propertySource;
    }

    protected final PropertyMapper getMapper() {
        return this.mapper;
    }

    public String toString() {
        return this.propertySource.toString();
    }

    public static SpringConfigurationPropertySource from(PropertySource<?> source) {
        Assert.notNull(source, (String)"Source must not be null");
        PropertyMapper mapper = SpringConfigurationPropertySource.getPropertyMapper(source);
        if (SpringConfigurationPropertySource.isFullEnumerable(source)) {
            return new SpringIterableConfigurationPropertySource((EnumerablePropertySource)source, mapper);
        }
        return new SpringConfigurationPropertySource(source, mapper, SpringConfigurationPropertySource.getContainsDescendantOfForSource(source));
    }

    private static PropertyMapper getPropertyMapper(PropertySource<?> source) {
        if (source instanceof SystemEnvironmentPropertySource && SpringConfigurationPropertySource.hasSystemEnvironmentName(source)) {
            return new DelegatingPropertyMapper(SystemEnvironmentPropertyMapper.INSTANCE, DefaultPropertyMapper.INSTANCE);
        }
        return new DelegatingPropertyMapper(DefaultPropertyMapper.INSTANCE);
    }

    private static boolean hasSystemEnvironmentName(PropertySource<?> source) {
        String name = source.getName();
        return "systemEnvironment".equals(name) || name.endsWith("-systemEnvironment");
    }

    private static boolean isFullEnumerable(PropertySource<?> source) {
        PropertySource<?> rootSource = SpringConfigurationPropertySource.getRootSource(source);
        if (rootSource.getSource() instanceof Map) {
            try {
                ((Map)rootSource.getSource()).size();
            }
            catch (UnsupportedOperationException ex) {
                return false;
            }
        }
        return source instanceof EnumerablePropertySource;
    }

    private static PropertySource<?> getRootSource(PropertySource<?> source) {
        while (source.getSource() != null && source.getSource() instanceof PropertySource) {
            source = (PropertySource)source.getSource();
        }
        return source;
    }

    private static Function<ConfigurationPropertyName, ConfigurationPropertyState> getContainsDescendantOfForSource(PropertySource<?> source) {
        if (source.getSource() instanceof Random) {
            return SpringConfigurationPropertySource::containsDescendantOfForRandom;
        }
        return null;
    }

    private static ConfigurationPropertyState containsDescendantOfForRandom(ConfigurationPropertyName name) {
        if (name.isAncestorOf(RANDOM) || name.equals(RANDOM)) {
            return ConfigurationPropertyState.PRESENT;
        }
        return ConfigurationPropertyState.ABSENT;
    }

    private static class DelegatingPropertyMapper
    implements PropertyMapper {
        private static final PropertyMapping[] NONE = new PropertyMapping[0];
        private final PropertyMapper first;
        private final PropertyMapper second;

        DelegatingPropertyMapper(PropertyMapper first) {
            this(first, null);
        }

        DelegatingPropertyMapper(PropertyMapper first, PropertyMapper second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public PropertyMapping[] map(ConfigurationPropertyName configurationPropertyName) {
            PropertyMapping[] first = this.map(this.first, configurationPropertyName);
            PropertyMapping[] second = this.map(this.second, configurationPropertyName);
            return this.merge(first, second);
        }

        private PropertyMapping[] map(PropertyMapper mapper, ConfigurationPropertyName configurationPropertyName) {
            try {
                return mapper != null ? mapper.map(configurationPropertyName) : NONE;
            }
            catch (Exception ex) {
                return NONE;
            }
        }

        @Override
        public PropertyMapping[] map(String propertySourceName) {
            PropertyMapping[] first = this.map(this.first, propertySourceName);
            PropertyMapping[] second = this.map(this.second, propertySourceName);
            return this.merge(first, second);
        }

        private PropertyMapping[] map(PropertyMapper mapper, String propertySourceName) {
            try {
                return mapper != null ? mapper.map(propertySourceName) : NONE;
            }
            catch (Exception ex) {
                return NONE;
            }
        }

        private PropertyMapping[] merge(PropertyMapping[] first, PropertyMapping[] second) {
            if (ObjectUtils.isEmpty((Object[])second)) {
                return first;
            }
            if (ObjectUtils.isEmpty((Object[])first)) {
                return second;
            }
            PropertyMapping[] merged = new PropertyMapping[first.length + second.length];
            System.arraycopy(first, 0, merged, 0, first.length);
            System.arraycopy(second, 0, merged, first.length, second.length);
            return merged;
        }
    }

}

