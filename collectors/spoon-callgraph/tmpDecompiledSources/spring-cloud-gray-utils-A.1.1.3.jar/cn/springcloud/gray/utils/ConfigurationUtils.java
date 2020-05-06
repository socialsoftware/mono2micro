/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.aop.TargetSource
 *  org.springframework.aop.framework.Advised
 *  org.springframework.aop.support.AopUtils
 *  org.springframework.beans.factory.BeanFactory
 *  org.springframework.context.expression.BeanFactoryResolver
 *  org.springframework.core.convert.ConversionService
 *  org.springframework.expression.BeanResolver
 *  org.springframework.expression.EvaluationContext
 *  org.springframework.expression.Expression
 *  org.springframework.expression.ParserContext
 *  org.springframework.expression.common.TemplateParserContext
 *  org.springframework.expression.spel.standard.SpelExpressionParser
 *  org.springframework.expression.spel.support.StandardEvaluationContext
 *  org.springframework.validation.BeanPropertyBindingResult
 *  org.springframework.validation.BindException
 *  org.springframework.validation.BindingResult
 *  org.springframework.validation.Errors
 *  org.springframework.validation.Validator
 */
package cn.springcloud.gray.utils;

import cn.springcloud.gray.bean.properties.bind.BindResult;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.bind.Binder;
import cn.springcloud.gray.bean.properties.bind.PlaceholdersResolver;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.MapConfigurationPropertySource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ConfigurationUtils {
    public static Map<String, Object> normalize(Map<String, String> args, SpelExpressionParser parser, BeanFactory beanFactory) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<String, String> entry : args.entrySet()) {
            String key = entry.getKey();
            Object value = ConfigurationUtils.getValue(parser, beanFactory, entry.getValue());
            map.put(key, value);
        }
        return map;
    }

    public static void bind(Object o, Map<String, Object> properties, String configurationPropertyName, String bindingName, Validator validator, ConversionService conversionService) {
        T toBind = ConfigurationUtils.getTargetObject(o);
        new Binder(Collections.singletonList(new MapConfigurationPropertySource(properties)), null, conversionService).bind(configurationPropertyName, Bindable.ofInstance(toBind));
        if (validator != null) {
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(toBind, bindingName);
            validator.validate(toBind, (Errors)errors);
            if (errors.hasErrors()) {
                throw new RuntimeException((Throwable)new BindException((BindingResult)errors));
            }
        }
    }

    private static <T> T getTargetObject(Object candidate) {
        try {
            if (AopUtils.isAopProxy((Object)candidate) && candidate instanceof Advised) {
                return (T)((Advised)candidate).getTargetSource().getTarget();
            }
        }
        catch (Exception ex) {
            throw new IllegalStateException("Failed to unwrap proxied object", ex);
        }
        return (T)candidate;
    }

    private static Object getValue(SpelExpressionParser parser, BeanFactory beanFactory, String entryValue) {
        Object value;
        String rawValue = entryValue;
        if (rawValue != null) {
            rawValue = rawValue.trim();
        }
        if (rawValue != null && rawValue.startsWith("#{") && entryValue.endsWith("}")) {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setBeanResolver((BeanResolver)new BeanFactoryResolver(beanFactory));
            Expression expression = parser.parseExpression(entryValue, (ParserContext)new TemplateParserContext());
            context.lookupVariable("api");
            value = expression.getValue((EvaluationContext)context);
        } else {
            value = entryValue;
        }
        return value;
    }
}

