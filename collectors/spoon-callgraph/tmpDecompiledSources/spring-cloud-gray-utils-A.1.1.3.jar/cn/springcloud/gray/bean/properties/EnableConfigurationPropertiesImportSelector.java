/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.BeanFactory
 *  org.springframework.beans.factory.config.BeanDefinition
 *  org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 *  org.springframework.beans.factory.support.BeanDefinitionRegistry
 *  org.springframework.beans.factory.support.GenericBeanDefinition
 *  org.springframework.context.annotation.ImportBeanDefinitionRegistrar
 *  org.springframework.context.annotation.ImportSelector
 *  org.springframework.core.annotation.AnnotationUtils
 *  org.springframework.core.type.AnnotationMetadata
 *  org.springframework.util.Assert
 *  org.springframework.util.MultiValueMap
 *  org.springframework.util.StringUtils
 */
package cn.springcloud.gray.bean.properties;

import cn.springcloud.gray.bean.properties.ConfigurationProperties;
import cn.springcloud.gray.bean.properties.ConfigurationPropertiesBindingPostProcessorRegistrar;
import cn.springcloud.gray.bean.properties.EnableConfigurationProperties;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

class EnableConfigurationPropertiesImportSelector
implements ImportSelector {
    private static final String[] IMPORTS = new String[]{ConfigurationPropertiesBeanRegistrar.class.getName(), ConfigurationPropertiesBindingPostProcessorRegistrar.class.getName()};

    EnableConfigurationPropertiesImportSelector() {
    }

    public String[] selectImports(AnnotationMetadata metadata) {
        return IMPORTS;
    }

    public static class ConfigurationPropertiesBeanRegistrar
    implements ImportBeanDefinitionRegistrar {
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
            this.getTypes(metadata).forEach(type -> this.register(registry, (ConfigurableListableBeanFactory)registry, (Class<?>)type));
        }

        private List<Class<?>> getTypes(AnnotationMetadata metadata) {
            MultiValueMap attributes = metadata.getAllAnnotationAttributes(EnableConfigurationProperties.class.getName(), false);
            return this.collectClasses(attributes != null ? (List)attributes.get((Object)"value") : Collections.emptyList());
        }

        private List<Class<?>> collectClasses(List<?> values) {
            return values.stream().flatMap(value -> Arrays.stream((Object[])value)).map(o -> (Class)o).filter(type -> Void.TYPE != type).collect(Collectors.toList());
        }

        private void register(BeanDefinitionRegistry registry, ConfigurableListableBeanFactory beanFactory, Class<?> type) {
            String name = this.getName(type);
            if (!this.containsBeanDefinition(beanFactory, name)) {
                this.registerBeanDefinition(registry, name, type);
            }
        }

        private String getName(Class<?> type) {
            ConfigurationProperties annotation = (ConfigurationProperties)AnnotationUtils.findAnnotation(type, ConfigurationProperties.class);
            String prefix = annotation != null ? annotation.prefix() : "";
            return StringUtils.hasText((String)prefix) ? prefix + "-" + type.getName() : type.getName();
        }

        private boolean containsBeanDefinition(ConfigurableListableBeanFactory beanFactory, String name) {
            if (beanFactory.containsBeanDefinition(name)) {
                return true;
            }
            BeanFactory parent = beanFactory.getParentBeanFactory();
            if (parent instanceof ConfigurableListableBeanFactory) {
                return this.containsBeanDefinition((ConfigurableListableBeanFactory)parent, name);
            }
            return false;
        }

        private void registerBeanDefinition(BeanDefinitionRegistry registry, String name, Class<?> type) {
            this.assertHasAnnotation(type);
            GenericBeanDefinition definition = new GenericBeanDefinition();
            definition.setBeanClass(type);
            registry.registerBeanDefinition(name, (BeanDefinition)definition);
        }

        private void assertHasAnnotation(Class<?> type) {
            Assert.notNull((Object)AnnotationUtils.findAnnotation(type, ConfigurationProperties.class), (String)("No " + ConfigurationProperties.class.getSimpleName() + " annotation found on  '" + type.getName() + "'."));
        }
    }

}

