/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.env.ConfigurableEnvironment
 *  org.springframework.core.env.Environment
 *  org.springframework.core.env.MutablePropertySources
 *  org.springframework.core.env.PropertySource
 *  org.springframework.core.env.PropertySources
 *  org.springframework.util.Assert
 *  org.springframework.util.PropertyPlaceholderHelper
 *  org.springframework.util.PropertyPlaceholderHelper$PlaceholderResolver
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.properties.bind.PlaceholdersResolver;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.util.Assert;
import org.springframework.util.PropertyPlaceholderHelper;

public class PropertySourcesPlaceholdersResolver
implements PlaceholdersResolver {
    private final Iterable<PropertySource<?>> sources;
    private final PropertyPlaceholderHelper helper;

    public PropertySourcesPlaceholdersResolver(Environment environment) {
        this((Iterable<PropertySource<?>>)PropertySourcesPlaceholdersResolver.getSources(environment), null);
    }

    public PropertySourcesPlaceholdersResolver(Iterable<PropertySource<?>> sources) {
        this(sources, null);
    }

    public PropertySourcesPlaceholdersResolver(Iterable<PropertySource<?>> sources, PropertyPlaceholderHelper helper) {
        this.sources = sources;
        this.helper = helper != null ? helper : new PropertyPlaceholderHelper("${", "}", ":", true);
    }

    @Override
    public Object resolvePlaceholders(Object value) {
        if (value != null && value instanceof String) {
            return this.helper.replacePlaceholders((String)value, this::resolvePlaceholder);
        }
        return value;
    }

    protected String resolvePlaceholder(String placeholder) {
        if (this.sources != null) {
            for (PropertySource<?> source : this.sources) {
                Object value = source.getProperty(placeholder);
                if (value == null) continue;
                return String.valueOf(value);
            }
        }
        return null;
    }

    private static PropertySources getSources(Environment environment) {
        Assert.notNull((Object)environment, (String)"Environment must not be null");
        Assert.isInstanceOf(ConfigurableEnvironment.class, (Object)environment, (String)"Environment must be a ConfigurableEnvironment");
        return ((ConfigurableEnvironment)environment).getPropertySources();
    }
}

