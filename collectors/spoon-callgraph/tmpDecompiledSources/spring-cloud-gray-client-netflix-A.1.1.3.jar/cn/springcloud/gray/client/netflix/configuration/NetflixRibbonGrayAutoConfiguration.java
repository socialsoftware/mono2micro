/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.GrayClientHolder
 *  cn.springcloud.gray.GrayManager
 *  cn.springcloud.gray.choose.GrayPredicate
 *  cn.springcloud.gray.request.LocalStorageLifeCycle
 *  cn.springcloud.gray.request.RequestLocalStorage
 *  cn.springcloud.gray.routing.connectionpoint.DefaultRoutingConnectionPoint
 *  cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint
 *  cn.springcloud.gray.servernode.ServerExplainer
 *  cn.springcloud.gray.servernode.ServerListProcessor
 *  com.netflix.loadbalancer.Server
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.cloud.netflix.ribbon.RibbonClients
 *  org.springframework.cloud.netflix.ribbon.SpringClientFactory
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.client.netflix.configuration;

import cn.springcloud.gray.GrayClientHolder;
import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.choose.GrayPredicate;
import cn.springcloud.gray.client.netflix.RibbonServerChooser;
import cn.springcloud.gray.client.netflix.ribbon.RibbonServerExplainer;
import cn.springcloud.gray.client.netflix.ribbon.configuration.GrayRibbonClientsConfiguration;
import cn.springcloud.gray.request.LocalStorageLifeCycle;
import cn.springcloud.gray.request.RequestLocalStorage;
import cn.springcloud.gray.routing.connectionpoint.DefaultRoutingConnectionPoint;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint;
import cn.springcloud.gray.servernode.ServerExplainer;
import cn.springcloud.gray.servernode.ServerListProcessor;
import com.netflix.loadbalancer.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(value={GrayManager.class})
@RibbonClients(defaultConfiguration={GrayRibbonClientsConfiguration.class})
public class NetflixRibbonGrayAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RoutingConnectionPoint ribbonConnectionPoint(GrayManager grayManager, RequestLocalStorage requestLocalStorage, LocalStorageLifeCycle localStorageLifeCycle) {
        return new DefaultRoutingConnectionPoint(grayManager, requestLocalStorage, localStorageLifeCycle);
    }

    @Bean
    @ConditionalOnMissingBean
    public RibbonServerChooser ribbonServerChooser(GrayManager grayManager, RequestLocalStorage requestLocalStorage, GrayPredicate grayPredicate, ServerExplainer<Server> serverExplainer, @Autowired(required=false) ServerListProcessor serverListProcess) {
        if (serverListProcess == null) {
            serverListProcess = GrayClientHolder.getServereListProcessor();
        }
        return new RibbonServerChooser(grayManager, requestLocalStorage, grayPredicate, serverExplainer, serverListProcess);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerExplainer<Server> ribbonServerExplainer(SpringClientFactory springClientFactory) {
        return new RibbonServerExplainer(springClientFactory);
    }
}

