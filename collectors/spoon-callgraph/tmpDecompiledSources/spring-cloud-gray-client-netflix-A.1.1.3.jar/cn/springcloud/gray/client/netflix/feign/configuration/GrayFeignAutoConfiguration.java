/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.GrayManager
 *  cn.springcloud.gray.request.RequestLocalStorage
 *  com.netflix.loadbalancer.ILoadBalancer
 *  feign.Feign
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.cloud.netflix.feign.EnableFeignClients
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.client.netflix.feign.configuration;

import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.client.netflix.feign.GrayTrackFeignRequestInterceptor;
import cn.springcloud.gray.client.netflix.feign.configuration.GrayFeignClientsConfiguration;
import cn.springcloud.gray.request.RequestLocalStorage;
import com.netflix.loadbalancer.ILoadBalancer;
import feign.Feign;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(value={GrayManager.class})
@ConditionalOnClass(value={ILoadBalancer.class, Feign.class})
@EnableFeignClients(defaultConfiguration={GrayFeignClientsConfiguration.class})
public class GrayFeignAutoConfiguration {

    @Configuration
    @ConditionalOnProperty(value={"gray.request.track.enabled"}, matchIfMissing=true)
    public static class GrayTrackFeignConfiguration {
        @Bean
        public GrayTrackFeignRequestInterceptor grayTrackFeignRequestInterceptor(RequestLocalStorage requestLocalStorage) {
            return new GrayTrackFeignRequestInterceptor(requestLocalStorage);
        }
    }

}

