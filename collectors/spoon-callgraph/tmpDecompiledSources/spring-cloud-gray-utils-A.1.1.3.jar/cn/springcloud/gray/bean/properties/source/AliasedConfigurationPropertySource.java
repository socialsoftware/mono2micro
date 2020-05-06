/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.Assert
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyNameAliases;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyState;
import java.util.List;
import org.springframework.util.Assert;

class AliasedConfigurationPropertySource
implements ConfigurationPropertySource {
    private final ConfigurationPropertySource source;
    private final ConfigurationPropertyNameAliases aliases;

    AliasedConfigurationPropertySource(ConfigurationPropertySource source, ConfigurationPropertyNameAliases aliases) {
        Assert.notNull((Object)source, (String)"Source must not be null");
        Assert.notNull((Object)aliases, (String)"Aliases must not be null");
        this.source = source;
        this.aliases = aliases;
    }

    @Override
    public ConfigurationProperty getConfigurationProperty(ConfigurationPropertyName name) {
        Assert.notNull((Object)name, (String)"Name must not be null");
        ConfigurationProperty result = this.getSource().getConfigurationProperty(name);
        if (result == null) {
            ConfigurationPropertyName aliasedName = this.getAliases().getNameForAlias(name);
            result = this.getSource().getConfigurationProperty(aliasedName);
        }
        return result;
    }

    @Override
    public ConfigurationPropertyState containsDescendantOf(ConfigurationPropertyName name) {
        Assert.notNull((Object)name, (String)"Name must not be null");
        ConfigurationPropertyState result = this.source.containsDescendantOf(name);
        if (result != ConfigurationPropertyState.ABSENT) {
            return result;
        }
        for (ConfigurationPropertyName alias : this.getAliases().getAliases(name)) {
            Object aliasResult = this.source.containsDescendantOf(alias);
            if (aliasResult == ConfigurationPropertyState.ABSENT) continue;
            return aliasResult;
        }
        for (ConfigurationPropertyName from : this.getAliases()) {
            for (ConfigurationPropertyName alias : this.getAliases().getAliases(from)) {
                if (!name.isAncestorOf(alias) || this.source.getConfigurationProperty(from) == null) continue;
                return ConfigurationPropertyState.PRESENT;
            }
        }
        return ConfigurationPropertyState.ABSENT;
    }

    @Override
    public Object getUnderlyingSource() {
        return this.source.getUnderlyingSource();
    }

    protected ConfigurationPropertySource getSource() {
        return this.source;
    }

    protected ConfigurationPropertyNameAliases getAliases() {
        return this.aliases;
    }
}

