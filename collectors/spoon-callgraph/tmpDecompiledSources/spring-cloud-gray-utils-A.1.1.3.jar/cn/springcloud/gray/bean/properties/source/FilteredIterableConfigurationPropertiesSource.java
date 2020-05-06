/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyState;
import cn.springcloud.gray.bean.properties.source.FilteredConfigurationPropertiesSource;
import cn.springcloud.gray.bean.properties.source.IterableConfigurationPropertySource;
import java.util.function.Predicate;
import java.util.stream.Stream;

class FilteredIterableConfigurationPropertiesSource
extends FilteredConfigurationPropertiesSource
implements IterableConfigurationPropertySource {
    FilteredIterableConfigurationPropertiesSource(IterableConfigurationPropertySource source, Predicate<ConfigurationPropertyName> filter) {
        super(source, filter);
    }

    @Override
    public Stream<ConfigurationPropertyName> stream() {
        return this.getSource().stream().filter(this.getFilter());
    }

    @Override
    protected IterableConfigurationPropertySource getSource() {
        return (IterableConfigurationPropertySource)super.getSource();
    }

    @Override
    public ConfigurationPropertyState containsDescendantOf(ConfigurationPropertyName name) {
        return ConfigurationPropertyState.search(this, name::isAncestorOf);
    }
}

