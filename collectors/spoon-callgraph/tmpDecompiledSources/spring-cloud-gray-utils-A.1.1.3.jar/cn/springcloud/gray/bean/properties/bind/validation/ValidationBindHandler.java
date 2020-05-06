/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.ResolvableType
 *  org.springframework.validation.BeanPropertyBindingResult
 *  org.springframework.validation.BindingResult
 *  org.springframework.validation.Errors
 *  org.springframework.validation.ObjectError
 *  org.springframework.validation.Validator
 */
package cn.springcloud.gray.bean.properties.bind.validation;

import cn.springcloud.gray.bean.properties.bind.AbstractBindHandler;
import cn.springcloud.gray.bean.properties.bind.BindContext;
import cn.springcloud.gray.bean.properties.bind.BindHandler;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.bind.validation.BindValidationException;
import cn.springcloud.gray.bean.properties.bind.validation.ValidationErrors;
import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.ResolvableType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

public class ValidationBindHandler
extends AbstractBindHandler {
    private final Validator[] validators;
    private final Set<ConfigurationProperty> boundProperties = new LinkedHashSet<ConfigurationProperty>();
    private final Deque<BindValidationException> exceptions = new LinkedList<BindValidationException>();

    public ValidationBindHandler(Validator ... validators) {
        this.validators = validators;
    }

    public ValidationBindHandler(BindHandler parent, Validator ... validators) {
        super(parent);
        this.validators = validators;
    }

    @Override
    public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        if (context.getConfigurationProperty() != null) {
            this.boundProperties.add(context.getConfigurationProperty());
        }
        return super.onSuccess(name, target, context, result);
    }

    @Override
    public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) throws Exception {
        this.validate(name, target, context, result);
        super.onFinish(name, target, context, result);
    }

    @Override
    public Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) throws Exception {
        Object result = super.onFailure(name, target, context, error);
        this.validate(name, target, context, null);
        return result;
    }

    private void validate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        Object validationTarget = this.getValidationTarget(target, context, result);
        Class validationType = target.getBoxedType().resolve();
        if (validationTarget != null) {
            this.validateAndPush(name, validationTarget, validationType);
        }
        if (context.getDepth() == 0 && !this.exceptions.isEmpty()) {
            throw this.exceptions.pop();
        }
    }

    private Object getValidationTarget(Bindable<?> target, BindContext context, Object result) {
        if (result != null) {
            return result;
        }
        if (context.getDepth() == 0 && target.getValue() != null) {
            return target.getValue().get();
        }
        return null;
    }

    private void validateAndPush(ConfigurationPropertyName name, Object target, Class<?> type) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(target, name.toString());
        Arrays.stream(this.validators).filter(validator -> validator.supports(type)).forEach(arg_0 -> ValidationBindHandler.lambda$validateAndPush$1(target, (BindingResult)errors, arg_0));
        if (errors.hasErrors()) {
            this.exceptions.push(this.getBindValidationException(name, (BindingResult)errors));
        }
    }

    private BindValidationException getBindValidationException(ConfigurationPropertyName name, BindingResult errors) {
        Set boundProperties = this.boundProperties.stream().filter(property -> name.isAncestorOf(property.getName())).collect(Collectors.toCollection(LinkedHashSet::new));
        ValidationErrors validationErrors = new ValidationErrors(name, boundProperties, errors.getAllErrors());
        return new BindValidationException(validationErrors);
    }

    private static /* synthetic */ void lambda$validateAndPush$1(Object target, BindingResult errors, Validator validator) {
        validator.validate(target, (Errors)errors);
    }
}

