/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.env.ConfigurableEnvironment
 *  org.springframework.core.env.Environment
 *  org.springframework.core.env.MutablePropertySources
 *  org.springframework.core.env.PropertySource
 *  org.springframework.core.env.PropertySource$StubPropertySource
 *  org.springframework.core.env.PropertySources
 *  org.springframework.util.Assert
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySourcesPropertySource;
import cn.springcloud.gray.bean.properties.source.SpringConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.SpringConfigurationPropertySources;
import java.util.Collections;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.util.Assert;

public final class ConfigurationPropertySources {
    private static final String ATTACHED_PROPERTY_SOURCE_NAME = "configurationProperties";

    private ConfigurationPropertySources() {
    }

    public static boolean isAttachedConfigurationPropertySource(PropertySource<?> propertySource) {
        return ATTACHED_PROPERTY_SOURCE_NAME.equals(propertySource.getName());
    }

    public static void attach(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, (Object)environment);
        MutablePropertySources sources = ((ConfigurableEnvironment)environment).getPropertySources();
        PropertySource attached = sources.get(ATTACHED_PROPERTY_SOURCE_NAME);
        if (attached != null && attached.getSource() != sources) {
            sources.remove(ATTACHED_PROPERTY_SOURCE_NAME);
            attached = null;
        }
        if (attached == null) {
            sources.addFirst((PropertySource)new ConfigurationPropertySourcesPropertySource(ATTACHED_PROPERTY_SOURCE_NAME, new SpringConfigurationPropertySources((Iterable<PropertySource<?>>)sources)));
        }
    }

    public static Iterable<ConfigurationPropertySource> get(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, (Object)environment);
        MutablePropertySources sources = ((ConfigurableEnvironment)environment).getPropertySources();
        ConfigurationPropertySourcesPropertySource attached = (ConfigurationPropertySourcesPropertySource)sources.get(ATTACHED_PROPERTY_SOURCE_NAME);
        if (attached == null) {
            return ConfigurationPropertySources.from(sources);
        }
        return (Iterable)attached.getSource();
    }

    public static Iterable<ConfigurationPropertySource> from(PropertySource<?> source) {
        return Collections.singleton(SpringConfigurationPropertySource.from(source));
    }

    public static Iterable<ConfigurationPropertySource> from(Iterable<PropertySource<?>> sources) {
        return new SpringConfigurationPropertySources(sources);
    }

    private static Stream<PropertySource<?>> streamPropertySources(PropertySources sources) {
        return StreamSupport.stream(sources.spliterator(), false).flatMap(ConfigurationPropertySources::flatten).filter(ConfigurationPropertySources::isIncluded);
    }

    private static Stream<PropertySource<?>> flatten(PropertySource<?> source) {
        if (source.getSource() instanceof ConfigurableEnvironment) {
            return ConfigurationPropertySources.streamPropertySources((PropertySources)((ConfigurableEnvironment)source.getSource()).getPropertySources());
        }
        return Stream.of(source);
    }

    private static boolean isIncluded(PropertySource<?> source) {
        return !(source instanceof PropertySource.StubPropertySource) && !(source instanceof ConfigurationPropertySourcesPropertySource);
    }
}

