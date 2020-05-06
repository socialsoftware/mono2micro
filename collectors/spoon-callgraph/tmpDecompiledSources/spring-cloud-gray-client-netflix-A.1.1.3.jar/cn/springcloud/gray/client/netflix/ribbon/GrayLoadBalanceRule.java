/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.GrayClientHolder
 *  cn.springcloud.gray.GrayManager
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayService
 *  cn.springcloud.gray.request.GrayRequest
 *  cn.springcloud.gray.request.RequestLocalStorage
 *  cn.springcloud.gray.servernode.ServerExplainer
 *  cn.springcloud.gray.servernode.ServerListProcessor
 *  com.google.common.base.Optional
 *  com.netflix.loadbalancer.AbstractServerPredicate
 *  com.netflix.loadbalancer.CompositePredicate
 *  com.netflix.loadbalancer.ILoadBalancer
 *  com.netflix.loadbalancer.Server
 *  com.netflix.loadbalancer.Server$MetaInfo
 *  com.netflix.loadbalancer.ZoneAvoidanceRule
 */
package cn.springcloud.gray.client.netflix.ribbon;

import cn.springcloud.gray.GrayClientHolder;
import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.client.netflix.ribbon.GrayDecisionPredicate;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayService;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.RequestLocalStorage;
import cn.springcloud.gray.servernode.ServerExplainer;
import cn.springcloud.gray.servernode.ServerListProcessor;
import com.google.common.base.Optional;
import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.CompositePredicate;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrayLoadBalanceRule
extends ZoneAvoidanceRule {
    private static final Logger log = LoggerFactory.getLogger(GrayLoadBalanceRule.class);
    protected CompositePredicate grayCompositePredicate;
    protected GrayManager grayManager;
    protected RequestLocalStorage requestLocalStorage;
    protected ServerExplainer<Server> serverExplainer;
    protected ServerListProcessor serverListProcessor;

    public GrayLoadBalanceRule() {
        this(GrayClientHolder.getGrayManager(), GrayClientHolder.getRequestLocalStorage(), (ServerExplainer<Server>)GrayClientHolder.getServerExplainer(), (ServerListProcessor<Server>)GrayClientHolder.getServereListProcessor());
    }

    public GrayLoadBalanceRule(GrayManager grayManager, RequestLocalStorage requestLocalStorage, ServerExplainer<Server> serverExplainer, ServerListProcessor<Server> serverServerListProcessor) {
        this.grayManager = grayManager;
        this.requestLocalStorage = requestLocalStorage;
        this.serverExplainer = serverExplainer;
        this.serverListProcessor = serverServerListProcessor;
        this.init();
    }

    protected void init() {
        GrayDecisionPredicate grayPredicate = new GrayDecisionPredicate(this);
        this.grayCompositePredicate = CompositePredicate.withPredicates((AbstractServerPredicate[])new AbstractServerPredicate[]{super.getPredicate(), grayPredicate}).build();
    }

    public Server choose(Object key) {
        ILoadBalancer lb = this.getLoadBalancer();
        GrayRequest grayRequest = this.requestLocalStorage.getGrayRequest();
        String serviceId = grayRequest.getServiceId();
        if (this.grayManager.hasGray(serviceId)) {
            Optional server;
            GrayService grayService = this.grayManager.getGrayService(serviceId);
            List servers = this.serverListProcessor.process(serviceId, lb.getAllServers());
            ArrayList<Server> grayServers = new ArrayList<Server>(grayService.getGrayInstances().size());
            ArrayList<Server> normalServers = new ArrayList<Server>(Math.min(servers.size(), grayService.getGrayInstances().size()));
            for (Server server2 : servers) {
                if (grayService.getGrayInstance(server2.getMetaInfo().getInstanceId()) != null) {
                    grayServers.add(server2);
                    continue;
                }
                normalServers.add(server2);
            }
            if (GrayClientHolder.getGraySwitcher().isEanbleGrayRouting() && (server = this.grayCompositePredicate.chooseRoundRobinAfterFiltering(grayServers, key)).isPresent()) {
                return this.expect((Server)server.get());
            }
            return this.expect(this.choose(super.getPredicate(), normalServers, key));
        }
        return this.expect(super.choose(key));
    }

    protected Server choose(AbstractServerPredicate serverPredicate, List<Server> servers, Object key) {
        Optional server = serverPredicate.chooseRoundRobinAfterFiltering(servers, key);
        if (server.isPresent()) {
            return (Server)server.get();
        }
        return null;
    }

    public GrayManager getGrayManager() {
        return this.grayManager;
    }

    public RequestLocalStorage getRequestLocalStorage() {
        return this.requestLocalStorage;
    }

    public ServerExplainer<Server> getServerExplainer() {
        return this.serverExplainer;
    }

    private Server expect(Server server) {
        if (server != null) {
            log.debug("\u627e\u5230server:{}", (Object)server.getId());
        }
        return server;
    }
}

