/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.BeanCreationException
 *  org.springframework.util.ClassUtils
 */
package cn.springcloud.gray.bean.properties;

import cn.springcloud.gray.bean.properties.ConfigurationProperties;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.ClassUtils;

public class ConfigurationPropertiesBindException
extends BeanCreationException {
    private final Class<?> beanType;
    private final ConfigurationProperties annotation;

    ConfigurationPropertiesBindException(String beanName, Object bean, ConfigurationProperties annotation, Exception cause) {
        super(beanName, ConfigurationPropertiesBindException.getMessage(bean, annotation), (Throwable)cause);
        this.beanType = bean.getClass();
        this.annotation = annotation;
    }

    public Class<?> getBeanType() {
        return this.beanType;
    }

    public ConfigurationProperties getAnnotation() {
        return this.annotation;
    }

    private static String getMessage(Object bean, ConfigurationProperties annotation) {
        StringBuilder message = new StringBuilder();
        message.append("Could not bind properties to '" + ClassUtils.getShortName(bean.getClass()) + "' : ");
        message.append("prefix=").append(annotation.prefix());
        message.append(", ignoreInvalidFields=").append(annotation.ignoreInvalidFields());
        message.append(", ignoreUnknownFields=").append(annotation.ignoreUnknownFields());
        return message.toString();
    }
}

