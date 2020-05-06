/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.cloud.commons.util.SpringFactoryImportSelector
 *  org.springframework.core.annotation.Order
 *  org.springframework.core.env.ConfigurableEnvironment
 *  org.springframework.core.env.Environment
 *  org.springframework.core.env.MutablePropertySources
 *  org.springframework.core.env.PropertiesPropertySource
 *  org.springframework.core.env.PropertySource
 *  org.springframework.core.type.AnnotationMetadata
 */
package cn.springcloud.gray.client.config;

import cn.springcloud.gray.client.EnableGrayClient;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;

@Order(value=2147483547)
public class GrayClientImportSelector
extends SpringFactoryImportSelector<EnableGrayClient> {
    public String[] selectImports(AnnotationMetadata metadata) {
        String[] imports = super.selectImports(metadata);
        Environment env = this.getEnvironment();
        String grayEnabled = env.getProperty("gray.enabled");
        if (StringUtils.isEmpty(grayEnabled) && ConfigurableEnvironment.class.isInstance((Object)env)) {
            ConfigurableEnvironment environment = (ConfigurableEnvironment)env;
            MutablePropertySources m = environment.getPropertySources();
            Properties p = new Properties();
            p.put("gray.enabled", "true");
            m.addLast((PropertySource)new PropertiesPropertySource("defaultProperties", p));
        }
        return imports;
    }

    protected boolean isEnabled() {
        return false;
    }

    protected boolean hasDefaultFactory() {
        return true;
    }
}

