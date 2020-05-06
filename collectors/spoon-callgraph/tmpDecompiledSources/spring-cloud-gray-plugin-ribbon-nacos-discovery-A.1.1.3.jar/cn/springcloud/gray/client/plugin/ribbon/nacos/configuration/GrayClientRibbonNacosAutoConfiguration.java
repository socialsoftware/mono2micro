/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.servernode.ServerExplainer
 *  com.alibaba.cloud.nacos.ribbon.NacosServerList
 *  com.netflix.loadbalancer.Server
 *  com.netflix.ribbon.Ribbon
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.cloud.netflix.ribbon.SpringClientFactory
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.client.plugin.ribbon.nacos.configuration;

import cn.springcloud.gray.client.plugin.ribbon.nacos.NacosServerExplainer;
import cn.springcloud.gray.servernode.ServerExplainer;
import com.alibaba.cloud.nacos.ribbon.NacosServerList;
import com.netflix.loadbalancer.Server;
import com.netflix.ribbon.Ribbon;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value={"gray.enabled"})
@ConditionalOnClass(value={Ribbon.class, NacosServerList.class})
public class GrayClientRibbonNacosAutoConfiguration {
    @Bean
    public ServerExplainer<Server> serverExplainer(SpringClientFactory springClientFactory) {
        return new NacosServerExplainer(springClientFactory);
    }
}

