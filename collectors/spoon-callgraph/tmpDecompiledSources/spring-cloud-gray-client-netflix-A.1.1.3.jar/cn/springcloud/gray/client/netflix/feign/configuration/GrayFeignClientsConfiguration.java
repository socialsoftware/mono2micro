/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.client.config.properties.GrayRequestProperties
 *  cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint
 *  feign.Client
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.client.netflix.feign.configuration;

import cn.springcloud.gray.client.config.properties.GrayRequestProperties;
import cn.springcloud.gray.client.netflix.feign.GrayFeignClient;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint;
import feign.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrayFeignClientsConfiguration {
    @Autowired
    private GrayRequestProperties grayRequestProperties;

    @Bean
    public Client getFeignClient(Client feignClient, RoutingConnectionPoint routingConnectionPoint) {
        return new GrayFeignClient(feignClient, routingConnectionPoint, this.grayRequestProperties);
    }
}

