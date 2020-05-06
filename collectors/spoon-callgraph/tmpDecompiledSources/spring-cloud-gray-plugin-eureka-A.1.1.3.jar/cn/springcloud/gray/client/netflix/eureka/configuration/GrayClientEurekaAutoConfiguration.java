/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.client.config.properties.GrayHoldoutServerProperties
 *  cn.springcloud.gray.servernode.InstanceDiscoveryClient
 *  cn.springcloud.gray.servernode.ServerExplainer
 *  cn.springcloud.gray.servernode.ServerListProcessor
 *  com.netflix.discovery.EurekaClient
 *  com.netflix.loadbalancer.Server
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.cloud.netflix.ribbon.SpringClientFactory
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.client.netflix.eureka.configuration;

import cn.springcloud.gray.client.config.properties.GrayHoldoutServerProperties;
import cn.springcloud.gray.client.netflix.eureka.EurekaInstanceDiscoveryClient;
import cn.springcloud.gray.client.netflix.eureka.EurekaInstanceLocalInfoInitiralizer;
import cn.springcloud.gray.client.netflix.eureka.EurekaServerExplainer;
import cn.springcloud.gray.client.netflix.eureka.EurekaServerListProcessor;
import cn.springcloud.gray.client.netflix.eureka.EurekaZoneAffinityServerListProcessor;
import cn.springcloud.gray.servernode.InstanceDiscoveryClient;
import cn.springcloud.gray.servernode.ServerExplainer;
import cn.springcloud.gray.servernode.ServerListProcessor;
import com.netflix.discovery.EurekaClient;
import com.netflix.loadbalancer.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value={"gray.enabled"})
@ConditionalOnClass(value={EurekaClient.class})
public class GrayClientEurekaAutoConfiguration {
    @Autowired
    private SpringClientFactory springClientFactory;

    @Bean
    public EurekaInstanceLocalInfoInitiralizer instanceLocalInfoInitiralizer() {
        return new EurekaInstanceLocalInfoInitiralizer();
    }

    public ServerExplainer<Server> serverExplainer() {
        return new EurekaServerExplainer(this.springClientFactory);
    }

    @Bean
    public InstanceDiscoveryClient instanceDiscoveryClient() {
        return new EurekaInstanceDiscoveryClient();
    }

    @Bean
    @ConditionalOnProperty(value={"gray.holdout-server.enabled"})
    @ConditionalOnMissingBean
    public ServerListProcessor serverListProcessor(GrayHoldoutServerProperties grayHoldoutServerProperties, EurekaClient eurekaClient) {
        if (grayHoldoutServerProperties.isZoneAffinity()) {
            return new EurekaZoneAffinityServerListProcessor(grayHoldoutServerProperties, eurekaClient);
        }
        return new EurekaServerListProcessor(grayHoldoutServerProperties, eurekaClient);
    }
}

