/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.bean.properties.EnableConfigurationProperties
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.eureka.server.configuration;

import cn.springcloud.gray.bean.properties.EnableConfigurationProperties;
import cn.springcloud.gray.eureka.server.communicate.GrayCommunicateClient;
import cn.springcloud.gray.eureka.server.communicate.HttpCommunicateClient;
import cn.springcloud.gray.eureka.server.communicate.RetryableGrayCommunicateClient;
import cn.springcloud.gray.eureka.server.configuration.properties.GrayServerProperties;
import cn.springcloud.gray.eureka.server.listener.EurekaInstanceListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value={GrayServerProperties.class})
@ConditionalOnProperty(value={"gray.server.url"})
public class EurekaServerGrayAutoConfiguration {
    @Autowired
    private GrayServerProperties grayServerProperties;

    @Bean
    @ConditionalOnMissingBean
    public GrayCommunicateClient grayCommunicateClient() {
        HttpCommunicateClient communicateClient = new HttpCommunicateClient(this.grayServerProperties.getUrl());
        if (this.grayServerProperties.isRetryable()) {
            return new RetryableGrayCommunicateClient(this.grayServerProperties.getRetryNumberOfRetries(), communicateClient);
        }
        return communicateClient;
    }

    @Bean
    public EurekaInstanceListener eurekaInstanceListener(GrayCommunicateClient communicateClient) {
        return new EurekaInstanceListener(communicateClient);
    }
}

