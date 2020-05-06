/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.PropertyEditorRegistry
 *  org.springframework.beans.factory.config.ConfigurableBeanFactory
 *  org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ConfigurableApplicationContext
 *  org.springframework.core.convert.ConversionService
 *  org.springframework.core.env.PropertySource
 *  org.springframework.core.env.PropertySources
 *  org.springframework.util.Assert
 *  org.springframework.validation.Validator
 *  org.springframework.validation.annotation.Validated
 */
package cn.springcloud.gray.bean.properties;

import cn.springcloud.gray.bean.properties.ConfigurationProperties;
import cn.springcloud.gray.bean.properties.ConfigurationPropertiesBindHandlerAdvisor;
import cn.springcloud.gray.bean.properties.ConfigurationPropertiesJsr303Validator;
import cn.springcloud.gray.bean.properties.ConversionServiceDeducer;
import cn.springcloud.gray.bean.properties.PropertySourcesDeducer;
import cn.springcloud.gray.bean.properties.bind.BindHandler;
import cn.springcloud.gray.bean.properties.bind.BindResult;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.bind.Binder;
import cn.springcloud.gray.bean.properties.bind.PlaceholdersResolver;
import cn.springcloud.gray.bean.properties.bind.PropertySourcesPlaceholdersResolver;
import cn.springcloud.gray.bean.properties.bind.handler.IgnoreErrorsBindHandler;
import cn.springcloud.gray.bean.properties.bind.handler.IgnoreTopLevelConverterNotFoundBindHandler;
import cn.springcloud.gray.bean.properties.bind.handler.NoUnboundElementsBindHandler;
import cn.springcloud.gray.bean.properties.bind.validation.ValidationBindHandler;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySources;
import cn.springcloud.gray.bean.properties.source.UnboundElementsSourceFilter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.util.Assert;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

class ConfigurationPropertiesBinder {
    private final ApplicationContext applicationContext;
    private final PropertySources propertySources;
    private final Validator configurationPropertiesValidator;
    private final boolean jsr303Present;
    private volatile Validator jsr303Validator;
    private volatile Binder binder;

    ConfigurationPropertiesBinder(ApplicationContext applicationContext, String validatorBeanName) {
        this.applicationContext = applicationContext;
        this.propertySources = new PropertySourcesDeducer(applicationContext).getPropertySources();
        this.configurationPropertiesValidator = this.getConfigurationPropertiesValidator(applicationContext, validatorBeanName);
        this.jsr303Present = ConfigurationPropertiesJsr303Validator.isJsr303Present(applicationContext);
    }

    public void bind(Bindable<?> target) {
        ConfigurationProperties annotation = target.getAnnotation(ConfigurationProperties.class);
        Assert.state((boolean)(annotation != null), (String)("Missing @ConfigurationProperties on " + target));
        List<Validator> validators = this.getValidators(target);
        BindHandler bindHandler = this.getBindHandler(annotation, validators);
        this.getBinder().bind(annotation.prefix(), target, bindHandler);
    }

    private Validator getConfigurationPropertiesValidator(ApplicationContext applicationContext, String validatorBeanName) {
        if (applicationContext.containsBean(validatorBeanName)) {
            return (Validator)applicationContext.getBean(validatorBeanName, Validator.class);
        }
        return null;
    }

    private List<Validator> getValidators(Bindable<?> target) {
        ArrayList<Validator> validators = new ArrayList<Validator>(3);
        if (this.configurationPropertiesValidator != null) {
            validators.add(this.configurationPropertiesValidator);
        }
        if (this.jsr303Present && target.getAnnotation(Validated.class) != null) {
            validators.add(this.getJsr303Validator());
        }
        if (target.getValue() != null && target.getValue().get() instanceof Validator) {
            validators.add((Validator)target.getValue().get());
        }
        return validators;
    }

    private Validator getJsr303Validator() {
        if (this.jsr303Validator == null) {
            this.jsr303Validator = new ConfigurationPropertiesJsr303Validator(this.applicationContext);
        }
        return this.jsr303Validator;
    }

    private BindHandler getBindHandler(ConfigurationProperties annotation, List<Validator> validators) {
        BindHandler handler = new IgnoreTopLevelConverterNotFoundBindHandler();
        if (annotation.ignoreInvalidFields()) {
            handler = new IgnoreErrorsBindHandler(handler);
        }
        if (!annotation.ignoreUnknownFields()) {
            UnboundElementsSourceFilter filter = new UnboundElementsSourceFilter();
            handler = new NoUnboundElementsBindHandler(handler, filter);
        }
        if (!validators.isEmpty()) {
            handler = new ValidationBindHandler(handler, validators.toArray((T[])new Validator[0]));
        }
        for (ConfigurationPropertiesBindHandlerAdvisor advisor : this.getBindHandlerAdvisors()) {
            handler = advisor.apply(handler);
        }
        return handler;
    }

    private List<ConfigurationPropertiesBindHandlerAdvisor> getBindHandlerAdvisors() {
        return this.applicationContext.getBeansOfType(ConfigurationPropertiesBindHandlerAdvisor.class).values().stream().collect(Collectors.toList());
    }

    private Binder getBinder() {
        if (this.binder == null) {
            this.binder = new Binder(this.getConfigurationPropertySources(), this.getPropertySourcesPlaceholdersResolver(), this.getConversionService(), this.getPropertyEditorInitializer());
        }
        return this.binder;
    }

    private Iterable<ConfigurationPropertySource> getConfigurationPropertySources() {
        return ConfigurationPropertySources.from(this.propertySources);
    }

    private PropertySourcesPlaceholdersResolver getPropertySourcesPlaceholdersResolver() {
        return new PropertySourcesPlaceholdersResolver((Iterable<PropertySource<?>>)this.propertySources);
    }

    private ConversionService getConversionService() {
        return new ConversionServiceDeducer(this.applicationContext).getConversionService();
    }

    private Consumer<PropertyEditorRegistry> getPropertyEditorInitializer() {
        if (this.applicationContext instanceof ConfigurableApplicationContext) {
            return ((ConfigurableListableBeanFactory)((ConfigurableApplicationContext)this.applicationContext).getBeanFactory())::copyRegisteredEditorsTo;
        }
        return null;
    }
}

