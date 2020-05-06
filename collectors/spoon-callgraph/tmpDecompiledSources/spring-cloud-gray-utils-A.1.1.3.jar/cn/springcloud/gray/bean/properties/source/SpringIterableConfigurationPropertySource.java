/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.env.EnumerablePropertySource
 *  org.springframework.core.env.MapPropertySource
 *  org.springframework.core.env.PropertySource
 *  org.springframework.util.ObjectUtils
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyState;
import cn.springcloud.gray.bean.properties.source.IterableConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.PropertyMapper;
import cn.springcloud.gray.bean.properties.source.PropertyMapping;
import cn.springcloud.gray.bean.properties.source.SpringConfigurationPropertySource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.ObjectUtils;

class SpringIterableConfigurationPropertySource
extends SpringConfigurationPropertySource
implements IterableConfigurationPropertySource {
    private volatile Object cacheKey;
    private volatile Cache cache;

    SpringIterableConfigurationPropertySource(EnumerablePropertySource<?> propertySource, PropertyMapper mapper) {
        super((PropertySource<?>)propertySource, mapper, null);
        this.assertEnumerablePropertySource();
    }

    private void assertEnumerablePropertySource() {
        if (this.getPropertySource() instanceof MapPropertySource) {
            try {
                ((Map)((MapPropertySource)this.getPropertySource()).getSource()).size();
            }
            catch (UnsupportedOperationException ex) {
                throw new IllegalArgumentException("PropertySource must be fully enumerable");
            }
        }
    }

    @Override
    public ConfigurationProperty getConfigurationProperty(ConfigurationPropertyName name) {
        ConfigurationProperty configurationProperty = super.getConfigurationProperty(name);
        if (configurationProperty == null) {
            configurationProperty = this.find(this.getPropertyMappings(this.getCache()), name);
        }
        return configurationProperty;
    }

    @Override
    public Stream<ConfigurationPropertyName> stream() {
        return this.getConfigurationPropertyNames().stream();
    }

    @Override
    public Iterator<ConfigurationPropertyName> iterator() {
        return this.getConfigurationPropertyNames().iterator();
    }

    @Override
    public ConfigurationPropertyState containsDescendantOf(ConfigurationPropertyName name) {
        return ConfigurationPropertyState.search(this, name::isAncestorOf);
    }

    private List<ConfigurationPropertyName> getConfigurationPropertyNames() {
        List<ConfigurationPropertyName> names;
        Cache cache = this.getCache();
        List<ConfigurationPropertyName> list = names = cache != null ? cache.getNames() : null;
        if (names != null) {
            return names;
        }
        PropertyMapping[] mappings = this.getPropertyMappings(cache);
        names = new ArrayList<ConfigurationPropertyName>(mappings.length);
        for (PropertyMapping mapping : mappings) {
            names.add(mapping.getConfigurationPropertyName());
        }
        names = Collections.unmodifiableList(names);
        if (cache != null) {
            cache.setNames(names);
        }
        return names;
    }

    private PropertyMapping[] getPropertyMappings(Cache cache) {
        PropertyMapping[] result;
        PropertyMapping[] arrpropertyMapping = result = cache != null ? cache.getMappings() : null;
        if (result != null) {
            return result;
        }
        String[] names = this.getPropertySource().getPropertyNames();
        ArrayList<PropertyMapping> mappings = new ArrayList<PropertyMapping>(names.length * 2);
        for (String name : names) {
            for (PropertyMapping mapping : this.getMapper().map(name)) {
                mappings.add(mapping);
            }
        }
        result = mappings.toArray(new PropertyMapping[0]);
        if (cache != null) {
            cache.setMappings(result);
        }
        return result;
    }

    private Cache getCache() {
        CacheKey cacheKey = CacheKey.get(this.getPropertySource());
        if (cacheKey == null) {
            return null;
        }
        if (ObjectUtils.nullSafeEquals((Object)cacheKey, (Object)this.cacheKey)) {
            return this.cache;
        }
        this.cache = new Cache();
        this.cacheKey = cacheKey.copy();
        return this.cache;
    }

    protected EnumerablePropertySource<?> getPropertySource() {
        return (EnumerablePropertySource)super.getPropertySource();
    }

    private static final class CacheKey {
        private final Object key;

        private CacheKey(Object key) {
            this.key = key;
        }

        public CacheKey copy() {
            return new CacheKey(this.copyKey(this.key));
        }

        private Object copyKey(Object key) {
            if (key instanceof Set) {
                return new HashSet((Set)key);
            }
            return ((String[])key).clone();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            return ObjectUtils.nullSafeEquals((Object)this.key, (Object)((CacheKey)obj).key);
        }

        public int hashCode() {
            return this.key.hashCode();
        }

        public static CacheKey get(EnumerablePropertySource<?> source) {
            if (source instanceof MapPropertySource) {
                return new CacheKey(((Map)((MapPropertySource)source).getSource()).keySet());
            }
            return new CacheKey(source.getPropertyNames());
        }
    }

    private static class Cache {
        private List<ConfigurationPropertyName> names;
        private PropertyMapping[] mappings;

        private Cache() {
        }

        public List<ConfigurationPropertyName> getNames() {
            return this.names;
        }

        public void setNames(List<ConfigurationPropertyName> names) {
            this.names = names;
        }

        public PropertyMapping[] getMappings() {
            return this.mappings;
        }

        public void setMappings(PropertyMapping[] mappings) {
            this.mappings = mappings;
        }
    }

}

