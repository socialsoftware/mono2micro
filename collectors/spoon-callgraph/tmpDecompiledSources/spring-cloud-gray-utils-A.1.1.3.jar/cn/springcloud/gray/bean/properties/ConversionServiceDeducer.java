/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.BeansException
 *  org.springframework.beans.factory.BeanFactory
 *  org.springframework.beans.factory.BeanFactoryUtils
 *  org.springframework.beans.factory.ListableBeanFactory
 *  org.springframework.beans.factory.NoSuchBeanDefinitionException
 *  org.springframework.beans.factory.annotation.Qualifier
 *  org.springframework.beans.factory.config.AutowireCapableBeanFactory
 *  org.springframework.beans.factory.config.BeanDefinition
 *  org.springframework.beans.factory.config.ConfigurableBeanFactory
 *  org.springframework.beans.factory.support.AbstractBeanDefinition
 *  org.springframework.beans.factory.support.AutowireCandidateQualifier
 *  org.springframework.beans.factory.support.RootBeanDefinition
 *  org.springframework.context.ApplicationContext
 *  org.springframework.core.annotation.AnnotationUtils
 *  org.springframework.core.convert.ConversionService
 *  org.springframework.core.convert.converter.Converter
 *  org.springframework.core.convert.converter.GenericConverter
 */
package cn.springcloud.gray.bean.properties;

import cn.springcloud.gray.bean.convert.ApplicationConversionService;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;

class ConversionServiceDeducer {
    private final ApplicationContext applicationContext;

    ConversionServiceDeducer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ConversionService getConversionService() {
        try {
            return (ConversionService)this.applicationContext.getBean("conversionService", ConversionService.class);
        }
        catch (NoSuchBeanDefinitionException ex) {
            return new Factory((BeanFactory)this.applicationContext.getAutowireCapableBeanFactory()).create();
        }
    }

    public static <T> Map<String, T> qualifiedBeansOfType(ListableBeanFactory beanFactory, Class<T> beanType, String qualifier) throws BeansException {
        String[] candidateBeans = BeanFactoryUtils.beanNamesForTypeIncludingAncestors((ListableBeanFactory)beanFactory, beanType);
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>(4);
        for (String beanName : candidateBeans) {
            if (!ConversionServiceDeducer.isQualifierMatch(qualifier::equals, beanName, (BeanFactory)beanFactory)) continue;
            result.put(beanName, beanFactory.getBean(beanName, beanType));
        }
        return result;
    }

    public static boolean isQualifierMatch(Predicate<String> qualifier, String beanName, BeanFactory beanFactory) {
        if (qualifier.test(beanName)) {
            return true;
        }
        if (beanFactory != null) {
            for (String alias : beanFactory.getAliases(beanName)) {
                if (!qualifier.test(alias)) continue;
                return true;
            }
            try {
                Qualifier targetAnnotation;
                Class beanType = beanFactory.getType(beanName);
                if (beanFactory instanceof ConfigurableBeanFactory) {
                    AbstractBeanDefinition abd;
                    Object value;
                    Qualifier targetAnnotation2;
                    AutowireCandidateQualifier candidate;
                    Method factoryMethod;
                    BeanDefinition bd = ((ConfigurableBeanFactory)beanFactory).getMergedBeanDefinition(beanName);
                    if (bd instanceof AbstractBeanDefinition && (candidate = (abd = (AbstractBeanDefinition)bd).getQualifier(Qualifier.class.getName())) != null && (value = candidate.getAttribute("value")) != null && qualifier.test(value.toString())) {
                        return true;
                    }
                    if (bd instanceof RootBeanDefinition && (factoryMethod = ((RootBeanDefinition)bd).getResolvedFactoryMethod()) != null && (targetAnnotation2 = (Qualifier)AnnotationUtils.getAnnotation((Method)factoryMethod, Qualifier.class)) != null) {
                        return qualifier.test(targetAnnotation2.value());
                    }
                }
                if (beanType != null && (targetAnnotation = (Qualifier)AnnotationUtils.getAnnotation((AnnotatedElement)beanType, Qualifier.class)) != null) {
                    return qualifier.test(targetAnnotation.value());
                }
            }
            catch (NoSuchBeanDefinitionException beanType) {
                // empty catch block
            }
        }
        return false;
    }

    private static class Factory {
        private final List<Converter> converters;
        private final List<GenericConverter> genericConverters;

        Factory(BeanFactory beanFactory) {
            this.converters = this.beans(beanFactory, Converter.class, "org.springframework.boot.context.properties.ConfigurationPropertiesBinding");
            this.genericConverters = this.beans(beanFactory, GenericConverter.class, "org.springframework.boot.context.properties.ConfigurationPropertiesBinding");
        }

        private <T> List<T> beans(BeanFactory beanFactory, Class<T> type, String qualifier) {
            if (beanFactory instanceof ListableBeanFactory) {
                return this.beans(type, qualifier, (ListableBeanFactory)beanFactory);
            }
            return Collections.emptyList();
        }

        private <T> List<T> beans(Class<T> type, String qualifier, ListableBeanFactory beanFactory) {
            return new ArrayList<T>(ConversionServiceDeducer.qualifiedBeansOfType(beanFactory, type, qualifier).values());
        }

        public ConversionService create() {
            if (this.converters.isEmpty() && this.genericConverters.isEmpty()) {
                return ApplicationConversionService.getSharedInstance();
            }
            ApplicationConversionService conversionService = new ApplicationConversionService();
            for (Converter converter : this.converters) {
                conversionService.addConverter(converter);
            }
            for (GenericConverter genericConverter : this.genericConverters) {
                conversionService.addConverter(genericConverter);
            }
            return conversionService;
        }
    }

}

