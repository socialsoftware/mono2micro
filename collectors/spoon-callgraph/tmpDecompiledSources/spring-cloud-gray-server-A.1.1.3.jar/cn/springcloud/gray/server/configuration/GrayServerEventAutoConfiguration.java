/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.GrayEventPublisher
 *  cn.springcloud.gray.event.GraySourceEventPublisher
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.server.configuration;

import cn.springcloud.gray.event.GrayEventPublisher;
import cn.springcloud.gray.event.GraySourceEventPublisher;
import cn.springcloud.gray.server.event.DefaultGrayEventPublisher;
import cn.springcloud.gray.server.event.DefaultGraySourceEventPublisher;
import cn.springcloud.gray.server.event.EventSourceConvertService;
import cn.springcloud.gray.server.event.EventSourceConverter;
import cn.springcloud.gray.server.event.GrayDecisionEventSourceConverter;
import cn.springcloud.gray.server.event.GrayInstanceEventSourceConverter;
import cn.springcloud.gray.server.event.GrayPolicyEventSourceConverter;
import cn.springcloud.gray.server.event.GrayTrackEventSourceConverter;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrayServerEventAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public GrayEventPublisher grayEventPublisher() {
        return new DefaultGrayEventPublisher();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventSourceConvertService eventSourceConvertService(List<EventSourceConverter> converters) {
        return new EventSourceConvertService.Default(converters);
    }

    @Bean
    @ConditionalOnMissingBean
    public GraySourceEventPublisher graySourceEventPublisher(GrayEventPublisher publisherDelegater, @Autowired(required=false) ExecutorService grayEventExecutorService, EventSourceConvertService eventSourceConvertService) {
        if (grayEventExecutorService == null) {
            grayEventExecutorService = new ThreadPoolExecutor(5, 20, 1L, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(40));
        }
        return new DefaultGraySourceEventPublisher(publisherDelegater, grayEventExecutorService, eventSourceConvertService);
    }

    @Configuration
    public static class EventSourceConvertConfiguration {
        @Bean
        public GrayDecisionEventSourceConverter grayDecisionEventSourceConverter() {
            return new GrayDecisionEventSourceConverter();
        }

        @Bean
        public GrayInstanceEventSourceConverter grayInstanceEventSourceConverter() {
            return new GrayInstanceEventSourceConverter();
        }

        @Bean
        public GrayPolicyEventSourceConverter grayPolicyEventSourceConverter() {
            return new GrayPolicyEventSourceConverter();
        }

        @Bean
        public GrayTrackEventSourceConverter grayTrackEventSourceConverter() {
            return new GrayTrackEventSourceConverter();
        }
    }

}

