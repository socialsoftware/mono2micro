/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.DecisionDefinition
 *  cn.springcloud.gray.utils.ConfigurationUtils
 *  org.springframework.beans.BeansException
 *  org.springframework.beans.factory.BeanFactory
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 *  org.springframework.core.convert.ConversionService
 *  org.springframework.expression.spel.standard.SpelExpressionParser
 *  org.springframework.validation.Validator
 */
package cn.springcloud.gray.decision;

import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.decision.GrayDecisionFactoryKeeper;
import cn.springcloud.gray.decision.factory.GrayDecisionFactory;
import cn.springcloud.gray.model.DecisionDefinition;
import cn.springcloud.gray.utils.ConfigurationUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.Validator;

public class DefaultGrayDecisionFactoryKeeper
implements GrayDecisionFactoryKeeper,
ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(DefaultGrayDecisionFactoryKeeper.class);
    private ApplicationContext cxt;
    private SpelExpressionParser parser = new SpelExpressionParser();
    private ConversionService conversionService;
    private Validator validator;
    private Map<String, GrayDecisionFactory> grayDecisionFactories = new HashMap<String, GrayDecisionFactory>();

    public DefaultGrayDecisionFactoryKeeper(ConversionService conversionService, Validator validator, List<GrayDecisionFactory> decisionFactories) {
        this.conversionService = conversionService;
        this.validator = validator;
        this.initGrayDecisionFactories(decisionFactories);
    }

    @Override
    public GrayDecisionFactory getDecisionFactory(String name) {
        return this.grayDecisionFactories.get(name);
    }

    @Override
    public GrayDecision getGrayDecision(DecisionDefinition decisionDefinition) {
        GrayDecisionFactory factory = this.getDecisionFactory(decisionDefinition.getName());
        if (factory == null) {
            log.warn("\u6ca1\u6709\u627e\u5230\u7070\u5ea6\u51b3\u5b9a\u5de5\u5382:{}", (Object)decisionDefinition.getName());
            throw new NullPointerException("\u6ca1\u6709\u627e\u5230\u7070\u5ea6\u51b3\u5b9a\u5de5\u5382:" + decisionDefinition.getName());
        }
        return factory.apply(configuration -> {
            Map properties = ConfigurationUtils.normalize((Map)decisionDefinition.getInfos(), (SpelExpressionParser)this.parser, (BeanFactory)this.cxt);
            ConfigurationUtils.bind((Object)configuration, (Map)properties, (String)"", (String)"", (Validator)this.validator, (ConversionService)this.conversionService);
        });
    }

    private void initGrayDecisionFactories(List<GrayDecisionFactory> decisionFactories) {
        decisionFactories.stream().forEach(factory -> this.grayDecisionFactories.put(factory.name(), (GrayDecisionFactory)factory));
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.cxt = applicationContext;
    }
}

