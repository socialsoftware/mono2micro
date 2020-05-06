/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.CollectionUtils
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.AliasedConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyNameAliases;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.IterableConfigurationPropertySource;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.util.CollectionUtils;

class AliasedIterableConfigurationPropertySource
extends AliasedConfigurationPropertySource
implements IterableConfigurationPropertySource {
    AliasedIterableConfigurationPropertySource(IterableConfigurationPropertySource source, ConfigurationPropertyNameAliases aliases) {
        super(source, aliases);
    }

    @Override
    public Stream<ConfigurationPropertyName> stream() {
        return this.getSource().stream().flatMap(this::addAliases);
    }

    private Stream<ConfigurationPropertyName> addAliases(ConfigurationPropertyName name) {
        Stream<ConfigurationPropertyName> names = Stream.of(name);
        List<ConfigurationPropertyName> aliases = this.getAliases().getAliases(name);
        if (CollectionUtils.isEmpty(aliases)) {
            return names;
        }
        return Stream.concat(names, aliases.stream());
    }

    @Override
    protected IterableConfigurationPropertySource getSource() {
        return (IterableConfigurationPropertySource)super.getSource();
    }
}

