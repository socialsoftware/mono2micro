/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.apache.commons.logging.Log
 *  org.apache.commons.logging.LogFactory
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 *  org.springframework.core.env.ConfigurableEnvironment
 *  org.springframework.core.env.Environment
 *  org.springframework.core.env.MutablePropertySources
 *  org.springframework.core.env.PropertySources
 */
package cn.springcloud.gray.bean.properties;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySources;

class PropertySourcesDeducer {
    private static final Log logger = LogFactory.getLog(PropertySourcesDeducer.class);
    private final ApplicationContext applicationContext;

    PropertySourcesDeducer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public PropertySources getPropertySources() {
        PropertySourcesPlaceholderConfigurer configurer = this.getSinglePropertySourcesPlaceholderConfigurer();
        if (configurer != null) {
            return configurer.getAppliedPropertySources();
        }
        MutablePropertySources sources = this.extractEnvironmentPropertySources();
        if (sources != null) {
            return sources;
        }
        throw new IllegalStateException("Unable to obtain PropertySources from PropertySourcesPlaceholderConfigurer or Environment");
    }

    private MutablePropertySources extractEnvironmentPropertySources() {
        Environment environment = this.applicationContext.getEnvironment();
        if (environment instanceof ConfigurableEnvironment) {
            return ((ConfigurableEnvironment)environment).getPropertySources();
        }
        return null;
    }

    private PropertySourcesPlaceholderConfigurer getSinglePropertySourcesPlaceholderConfigurer() {
        Map beans = this.applicationContext.getBeansOfType(PropertySourcesPlaceholderConfigurer.class, false, false);
        if (beans.size() == 1) {
            return (PropertySourcesPlaceholderConfigurer)beans.values().iterator().next();
        }
        if (beans.size() > 1 && logger.isWarnEnabled()) {
            logger.warn((Object)("Multiple PropertySourcesPlaceholderConfigurer beans registered " + beans.keySet() + ", falling back to Environment"));
        }
        return null;
    }
}

