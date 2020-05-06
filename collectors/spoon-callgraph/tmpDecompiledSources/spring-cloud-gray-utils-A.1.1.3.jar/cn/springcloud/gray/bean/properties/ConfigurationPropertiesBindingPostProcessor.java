/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.BeansException
 *  org.springframework.beans.factory.InitializingBean
 *  org.springframework.beans.factory.config.BeanPostProcessor
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 *  org.springframework.core.PriorityOrdered
 *  org.springframework.core.ResolvableType
 *  org.springframework.core.annotation.AnnotationUtils
 *  org.springframework.validation.annotation.Validated
 */
package cn.springcloud.gray.bean.properties;

import cn.springcloud.gray.bean.properties.ConfigurationBeanFactoryMetadata;
import cn.springcloud.gray.bean.properties.ConfigurationProperties;
import cn.springcloud.gray.bean.properties.ConfigurationPropertiesBindException;
import cn.springcloud.gray.bean.properties.ConfigurationPropertiesBinder;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.annotation.Validated;

public class ConfigurationPropertiesBindingPostProcessor
implements BeanPostProcessor,
PriorityOrdered,
ApplicationContextAware,
InitializingBean {
    public static final String BEAN_NAME = ConfigurationPropertiesBindingPostProcessor.class.getName();
    public static final String VALIDATOR_BEAN_NAME = "configurationPropertiesValidator";
    private ConfigurationBeanFactoryMetadata beanFactoryMetadata;
    private ApplicationContext applicationContext;
    private ConfigurationPropertiesBinder configurationPropertiesBinder;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        this.beanFactoryMetadata = (ConfigurationBeanFactoryMetadata)this.applicationContext.getBean(ConfigurationBeanFactoryMetadata.BEAN_NAME, ConfigurationBeanFactoryMetadata.class);
        this.configurationPropertiesBinder = new ConfigurationPropertiesBinder(this.applicationContext, VALIDATOR_BEAN_NAME);
    }

    public int getOrder() {
        return -2147483647;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ConfigurationProperties annotation = this.getAnnotation(bean, beanName, ConfigurationProperties.class);
        if (annotation != null) {
            this.bind(bean, beanName, annotation);
        }
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private void bind(Object bean, String beanName, ConfigurationProperties annotation) {
        Annotation[] arrannotation;
        ResolvableType type = this.getBeanType(bean, beanName);
        Validated validated = this.getAnnotation(bean, beanName, Validated.class);
        if (validated != null) {
            Annotation[] arrannotation2 = new Annotation[2];
            arrannotation2[0] = annotation;
            arrannotation = arrannotation2;
            arrannotation2[1] = validated;
        } else {
            Annotation[] arrannotation3 = new Annotation[1];
            arrannotation = arrannotation3;
            arrannotation3[0] = annotation;
        }
        Annotation[] annotations = arrannotation;
        Bindable<Object> target = Bindable.of(type).withExistingValue(bean).withAnnotations(annotations);
        try {
            this.configurationPropertiesBinder.bind(target);
        }
        catch (Exception ex) {
            throw new ConfigurationPropertiesBindException(beanName, bean, annotation, ex);
        }
    }

    private ResolvableType getBeanType(Object bean, String beanName) {
        Method factoryMethod = this.beanFactoryMetadata.findFactoryMethod(beanName);
        if (factoryMethod != null) {
            return ResolvableType.forMethodReturnType((Method)factoryMethod);
        }
        return ResolvableType.forClass(bean.getClass());
    }

    private <A extends Annotation> A getAnnotation(Object bean, String beanName, Class<A> type) {
        Object annotation = this.beanFactoryMetadata.findFactoryAnnotation(beanName, type);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(bean.getClass(), type);
        }
        return annotation;
    }
}

