/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.BeansException
 *  org.springframework.beans.factory.config.BeanDefinition
 *  org.springframework.beans.factory.config.BeanFactoryPostProcessor
 *  org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 *  org.springframework.core.annotation.AnnotationUtils
 *  org.springframework.util.ClassUtils
 *  org.springframework.util.ReflectionUtils
 */
package cn.springcloud.gray.bean.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

public class ConfigurationBeanFactoryMetadata
implements BeanFactoryPostProcessor {
    public static final String BEAN_NAME = ConfigurationBeanFactoryMetadata.class.getName();
    private ConfigurableListableBeanFactory beanFactory;
    private final Map<String, FactoryMetadata> beansFactoryMetadata = new HashMap<String, FactoryMetadata>();

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        for (String name : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(name);
            String method = definition.getFactoryMethodName();
            String bean = definition.getFactoryBeanName();
            if (method == null || bean == null) continue;
            this.beansFactoryMetadata.put(name, new FactoryMetadata(bean, method));
        }
    }

    public <A extends Annotation> Map<String, Object> getBeansWithFactoryAnnotation(Class<A> type) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        for (String name : this.beansFactoryMetadata.keySet()) {
            if (this.findFactoryAnnotation(name, type) == null) continue;
            result.put(name, this.beanFactory.getBean(name));
        }
        return result;
    }

    public <A extends Annotation> A findFactoryAnnotation(String beanName, Class<A> type) {
        Method method = this.findFactoryMethod(beanName);
        return (A)(method != null ? AnnotationUtils.findAnnotation((Method)method, type) : null);
    }

    public Method findFactoryMethod(String beanName) {
        if (!this.beansFactoryMetadata.containsKey(beanName)) {
            return null;
        }
        AtomicReference<Object> found = new AtomicReference<Object>(null);
        FactoryMetadata metadata = this.beansFactoryMetadata.get(beanName);
        Class factoryType = this.beanFactory.getType(metadata.getBean());
        String factoryMethod = metadata.getMethod();
        if (ClassUtils.isCglibProxyClass((Class)factoryType)) {
            factoryType = factoryType.getSuperclass();
        }
        ReflectionUtils.doWithMethods(factoryType, method -> {
            if (method.getName().equals(factoryMethod)) {
                found.compareAndSet(null, method);
            }
        });
        return found.get();
    }

    private static class FactoryMetadata {
        private final String bean;
        private final String method;

        FactoryMetadata(String bean, String method) {
            this.bean = bean;
            this.method = method;
        }

        public String getBean() {
            return this.bean;
        }

        public String getMethod() {
            return this.method;
        }
    }

}

