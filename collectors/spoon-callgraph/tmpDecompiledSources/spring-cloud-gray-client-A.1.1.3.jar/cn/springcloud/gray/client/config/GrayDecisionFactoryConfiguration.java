/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.core.convert.ConversionService
 *  org.springframework.core.convert.support.DefaultConversionService
 *  org.springframework.validation.Validator
 */
package cn.springcloud.gray.client.config;

import cn.springcloud.gray.decision.DefaultGrayDecisionFactoryKeeper;
import cn.springcloud.gray.decision.GrayDecisionFactoryKeeper;
import cn.springcloud.gray.decision.factory.FlowRateGrayDecisionFactory;
import cn.springcloud.gray.decision.factory.GrayDecisionFactory;
import cn.springcloud.gray.decision.factory.HttpHeaderGrayDecisionFactory;
import cn.springcloud.gray.decision.factory.HttpMethodGrayDecisionFactory;
import cn.springcloud.gray.decision.factory.HttpParameterGrayDecisionFactory;
import cn.springcloud.gray.decision.factory.HttpTrackHeaderGrayDecisionFactory;
import cn.springcloud.gray.decision.factory.HttpTrackParameterGrayDecisionFactory;
import cn.springcloud.gray.decision.factory.TraceIpGrayDecisionFactory;
import cn.springcloud.gray.decision.factory.TrackAttributeGrayDecisionFactory;
import cn.springcloud.gray.decision.factory.TrackAttributesGrayDecisionFactory;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.validation.Validator;

@Configuration
public class GrayDecisionFactoryConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public GrayDecisionFactoryKeeper grayDecisionFactoryKeeper(Validator validator, List<GrayDecisionFactory> decisionFactories) {
        return new DefaultGrayDecisionFactoryKeeper(DefaultConversionService.getSharedInstance(), validator, decisionFactories);
    }

    @Configuration
    public static class WebGrayDecisionFactoryConfiguration {
        @Bean
        public HttpHeaderGrayDecisionFactory httpHeaderGrayDecisionFactory() {
            return new HttpHeaderGrayDecisionFactory();
        }

        @Bean
        public HttpMethodGrayDecisionFactory httpMethodGrayDecisionFactory() {
            return new HttpMethodGrayDecisionFactory();
        }

        @Bean
        public HttpParameterGrayDecisionFactory httpParameterGrayDecisionFactory() {
            return new HttpParameterGrayDecisionFactory();
        }

        @Bean
        public TraceIpGrayDecisionFactory traceIpGrayDecisionFactory() {
            return new TraceIpGrayDecisionFactory();
        }

        @Bean
        public HttpTrackParameterGrayDecisionFactory httpTrackParameterGrayDecisionFactory() {
            return new HttpTrackParameterGrayDecisionFactory();
        }

        @Bean
        public HttpTrackHeaderGrayDecisionFactory httpTrackHeaderGrayDecisionFactory() {
            return new HttpTrackHeaderGrayDecisionFactory();
        }

        @Bean
        public TrackAttributeGrayDecisionFactory trackAttributeGrayDecisionFactory() {
            return new TrackAttributeGrayDecisionFactory();
        }

        @Bean
        public TrackAttributesGrayDecisionFactory trackAttributesGrayDecisionFactory() {
            return new TrackAttributesGrayDecisionFactory();
        }

        @Bean
        public FlowRateGrayDecisionFactory flowRateGrayDecisionFactory() {
            return new FlowRateGrayDecisionFactory();
        }
    }

}

