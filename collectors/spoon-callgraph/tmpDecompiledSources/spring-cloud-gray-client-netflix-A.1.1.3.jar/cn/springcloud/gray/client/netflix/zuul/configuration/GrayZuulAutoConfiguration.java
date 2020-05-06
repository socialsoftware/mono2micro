/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.client.config.properties.GrayRequestProperties
 *  cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint
 *  com.netflix.zuul.http.ZuulServlet
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.client.netflix.zuul.configuration;

import cn.springcloud.gray.client.config.properties.GrayRequestProperties;
import cn.springcloud.gray.client.netflix.zuul.GrayPostZuulFilter;
import cn.springcloud.gray.client.netflix.zuul.GrayPreZuulFilter;
import cn.springcloud.gray.client.netflix.zuul.ZuulRequestInterceptor;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint;
import com.netflix.zuul.http.ZuulServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(value={ZuulServlet.class})
@ConditionalOnProperty(value={"gray.enabled"})
public class GrayZuulAutoConfiguration {
    @Autowired
    private GrayRequestProperties grayRequestProperties;

    @Bean
    public GrayPreZuulFilter grayPreZuulFilter(RoutingConnectionPoint routingConnectionPoint) {
        return new GrayPreZuulFilter(this.grayRequestProperties, routingConnectionPoint);
    }

    @Bean
    public GrayPostZuulFilter grayPostZuulFilter(RoutingConnectionPoint routingConnectionPoint) {
        return new GrayPostZuulFilter(routingConnectionPoint);
    }

    @Configuration
    @ConditionalOnProperty(value={"gray.request.track.enabled"}, matchIfMissing=true)
    public static class GrayTrackZuulConfiguration {
        @Bean
        public ZuulRequestInterceptor zuulRequestInterceptor() {
            return new ZuulRequestInterceptor();
        }
    }

}

