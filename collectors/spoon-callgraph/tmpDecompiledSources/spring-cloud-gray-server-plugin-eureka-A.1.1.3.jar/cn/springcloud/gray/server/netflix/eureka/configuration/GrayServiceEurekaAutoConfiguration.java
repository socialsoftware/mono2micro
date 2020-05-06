/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.server.discovery.ServiceDiscovery
 *  com.netflix.discovery.EurekaClient
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.server.netflix.eureka.configuration;

import cn.springcloud.gray.server.discovery.ServiceDiscovery;
import cn.springcloud.gray.server.netflix.eureka.EurekaServiceDiscovery;
import com.netflix.discovery.EurekaClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(value={EurekaClient.class})
public class GrayServiceEurekaAutoConfiguration {
    @Bean
    public ServiceDiscovery serviceDiscover(EurekaClient eurekaClient) {
        return new EurekaServiceDiscovery(eurekaClient);
    }
}

