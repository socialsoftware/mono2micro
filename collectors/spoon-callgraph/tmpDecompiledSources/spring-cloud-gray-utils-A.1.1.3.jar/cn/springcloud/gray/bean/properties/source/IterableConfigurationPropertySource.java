/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.AliasedIterableConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyNameAliases;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyState;
import cn.springcloud.gray.bean.properties.source.FilteredIterableConfigurationPropertiesSource;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface IterableConfigurationPropertySource
extends ConfigurationPropertySource,
Iterable<ConfigurationPropertyName> {
    @Override
    default public Iterator<ConfigurationPropertyName> iterator() {
        return this.stream().iterator();
    }

    public Stream<ConfigurationPropertyName> stream();

    @Override
    default public ConfigurationPropertyState containsDescendantOf(ConfigurationPropertyName name) {
        return ConfigurationPropertyState.search(this, name::isAncestorOf);
    }

    @Override
    default public IterableConfigurationPropertySource filter(Predicate<ConfigurationPropertyName> filter) {
        return new FilteredIterableConfigurationPropertiesSource(this, filter);
    }

    @Override
    default public IterableConfigurationPropertySource withAliases(ConfigurationPropertyNameAliases aliases) {
        return new AliasedIterableConfigurationPropertySource(this, aliases);
    }
}

