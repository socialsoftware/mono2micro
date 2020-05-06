/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  com.alibaba.cloud.nacos.NacosDiscoveryProperties
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.server.plugin.discovery.nacos.configuration;

import cn.springcloud.gray.server.plugin.discovery.nacos.NacosServiceDiscovery;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(value={NacosServiceDiscovery.class})
public class GrayServerPluginNacosDiscoveryAutoConfiguration {
    @Bean
    public NacosServiceDiscovery nacosServiceDiscovery(NacosDiscoveryProperties discoveryProperties) {
        return new NacosServiceDiscovery(discoveryProperties);
    }
}

