/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.GrayManager
 *  cn.springcloud.gray.client.config.properties.GrayRequestProperties
 *  cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.cloud.client.loadbalancer.LoadBalanced
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.web.client.RestTemplate
 */
package cn.springcloud.gray.client.netflix.resttemplate.configuration;

import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.client.config.properties.GrayRequestProperties;
import cn.springcloud.gray.client.netflix.resttemplate.GrayClientHttpRequestIntercptor;
import cn.springcloud.gray.client.netflix.resttemplate.RestTemplateRequestInterceptor;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnBean(value={GrayManager.class})
@ConditionalOnClass(value={RestTemplate.class, LoadBalanced.class})
public class GrayRestTemplateAutoConfiguration {

    @Configuration
    @ConditionalOnBean(value={RestTemplate.class})
    public static class LoadBalanceRestTemplateConfiguration {
        @Autowired
        private GrayRequestProperties grayRequestProperties;
        @Autowired
        private RoutingConnectionPoint routingConnectionPoint;

        @Bean
        public GrayClientHttpRequestIntercptor grayClientHttpRequestIntercptor(@Autowired(required=false) @LoadBalanced List<RestTemplate> restTemplates) {
            GrayClientHttpRequestIntercptor intercptor = new GrayClientHttpRequestIntercptor(this.grayRequestProperties, this.routingConnectionPoint);
            if (restTemplates != null) {
                restTemplates.forEach(restTemplate -> restTemplate.getInterceptors().add(intercptor));
            }
            return intercptor;
        }

        @Configuration
        @ConditionalOnProperty(value={"gray.request.track.enabled"}, matchIfMissing=true)
        public static class GrayTrackRestTemplateConfiguration {
            @Bean
            public RestTemplateRequestInterceptor restTemplateRequestInterceptor() {
                return new RestTemplateRequestInterceptor();
            }
        }

    }

}

