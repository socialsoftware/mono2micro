/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.GrayManager
 *  cn.springcloud.gray.client.config.GrayClientAutoConfiguration
 *  org.springframework.boot.autoconfigure.AutoConfigureAfter
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.client.config;

import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.client.config.GrayClientAutoConfiguration;
import cn.springcloud.gray.web.resources.DiscoveryInstanceResource;
import cn.springcloud.gray.web.resources.GrayListResource;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(value={GrayClientAutoConfiguration.class})
@ConditionalOnBean(value={GrayManager.class})
public class GrayClientWebAutoConfiguration {
    @Bean
    public DiscoveryInstanceResource discoveryInstanceResource() {
        return new DiscoveryInstanceResource();
    }

    @Bean
    public GrayListResource grayListResource() {
        return new GrayListResource();
    }
}

