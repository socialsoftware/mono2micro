/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.config.BeanDefinition
 *  org.springframework.beans.factory.support.BeanDefinitionRegistry
 *  org.springframework.beans.factory.support.GenericBeanDefinition
 *  org.springframework.context.annotation.ImportBeanDefinitionRegistrar
 *  org.springframework.core.type.AnnotationMetadata
 */
package cn.springcloud.gray.bean.properties;

import cn.springcloud.gray.bean.properties.ConfigurationBeanFactoryMetadata;
import cn.springcloud.gray.bean.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class ConfigurationPropertiesBindingPostProcessorRegistrar
implements ImportBeanDefinitionRegistrar {
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(ConfigurationPropertiesBindingPostProcessor.BEAN_NAME)) {
            this.registerConfigurationPropertiesBindingPostProcessor(registry);
            this.registerConfigurationBeanFactoryMetadata(registry);
        }
    }

    private void registerConfigurationPropertiesBindingPostProcessor(BeanDefinitionRegistry registry) {
        GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setBeanClass(ConfigurationPropertiesBindingPostProcessor.class);
        definition.setRole(2);
        registry.registerBeanDefinition(ConfigurationPropertiesBindingPostProcessor.BEAN_NAME, (BeanDefinition)definition);
    }

    private void registerConfigurationBeanFactoryMetadata(BeanDefinitionRegistry registry) {
        GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setBeanClass(ConfigurationBeanFactoryMetadata.class);
        definition.setRole(2);
        registry.registerBeanDefinition(ConfigurationBeanFactoryMetadata.BEAN_NAME, (BeanDefinition)definition);
    }
}

