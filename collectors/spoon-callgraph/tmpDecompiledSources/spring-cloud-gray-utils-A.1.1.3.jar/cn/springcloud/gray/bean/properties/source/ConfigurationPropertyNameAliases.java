/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.Assert
 *  org.springframework.util.LinkedMultiValueMap
 *  org.springframework.util.MultiValueMap
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public final class ConfigurationPropertyNameAliases
implements Iterable<ConfigurationPropertyName> {
    private final MultiValueMap<ConfigurationPropertyName, ConfigurationPropertyName> aliases = new LinkedMultiValueMap();

    public ConfigurationPropertyNameAliases() {
    }

    public ConfigurationPropertyNameAliases(String name, String ... aliases) {
        this.addAliases(name, aliases);
    }

    public ConfigurationPropertyNameAliases(ConfigurationPropertyName name, ConfigurationPropertyName ... aliases) {
        this.addAliases(name, aliases);
    }

    public void addAliases(String name, String ... aliases) {
        Assert.notNull((Object)name, (String)"Name must not be null");
        Assert.notNull((Object)aliases, (String)"Aliases must not be null");
        this.addAliases(ConfigurationPropertyName.of(name), (ConfigurationPropertyName[])Arrays.stream(aliases).map(ConfigurationPropertyName::of).toArray(x$0 -> new ConfigurationPropertyName[x$0]));
    }

    public void addAliases(ConfigurationPropertyName name, ConfigurationPropertyName ... aliases) {
        Assert.notNull((Object)name, (String)"Name must not be null");
        Assert.notNull((Object)aliases, (String)"Aliases must not be null");
        Arrays.asList(aliases).forEach(v -> this.aliases.add((Object)name, v));
    }

    public List<ConfigurationPropertyName> getAliases(ConfigurationPropertyName name) {
        return (List)this.aliases.getOrDefault((Object)name, Collections.emptyList());
    }

    public ConfigurationPropertyName getNameForAlias(ConfigurationPropertyName alias) {
        return this.aliases.entrySet().stream().filter(e -> ((List)e.getValue()).contains(alias)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    @Override
    public Iterator<ConfigurationPropertyName> iterator() {
        return this.aliases.keySet().iterator();
    }
}

