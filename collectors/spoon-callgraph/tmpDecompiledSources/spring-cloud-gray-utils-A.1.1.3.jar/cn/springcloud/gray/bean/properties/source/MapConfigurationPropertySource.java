/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.env.EnumerablePropertySource
 *  org.springframework.core.env.MapPropertySource
 *  org.springframework.util.Assert
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.DefaultPropertyMapper;
import cn.springcloud.gray.bean.properties.source.IterableConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.PropertyMapper;
import cn.springcloud.gray.bean.properties.source.SpringIterableConfigurationPropertySource;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.Assert;

public class MapConfigurationPropertySource
implements IterableConfigurationPropertySource {
    private final Map<String, Object> source = new LinkedHashMap<String, Object>();
    private final IterableConfigurationPropertySource delegate = new SpringIterableConfigurationPropertySource((EnumerablePropertySource<?>)new MapPropertySource("source", this.source), DefaultPropertyMapper.INSTANCE);

    public MapConfigurationPropertySource() {
        this(Collections.emptyMap());
    }

    public MapConfigurationPropertySource(Map<?, ?> map) {
        this.putAll(map);
    }

    public void putAll(Map<?, ?> map) {
        Assert.notNull(map, (String)"Map must not be null");
        this.assertNotReadOnlySystemAttributesMap(map);
        map.forEach((arg_0, arg_1) -> this.put(arg_0, arg_1));
    }

    public void put(Object name, Object value) {
        this.source.put(name != null ? name.toString() : null, value);
    }

    @Override
    public Object getUnderlyingSource() {
        return this.source;
    }

    @Override
    public ConfigurationProperty getConfigurationProperty(ConfigurationPropertyName name) {
        return this.delegate.getConfigurationProperty(name);
    }

    @Override
    public Iterator<ConfigurationPropertyName> iterator() {
        return this.delegate.iterator();
    }

    @Override
    public Stream<ConfigurationPropertyName> stream() {
        return this.delegate.stream();
    }

    private void assertNotReadOnlySystemAttributesMap(Map<?, ?> map) {
        try {
            map.size();
        }
        catch (UnsupportedOperationException ex) {
            throw new IllegalArgumentException("Security restricted maps are not supported", ex);
        }
    }
}

