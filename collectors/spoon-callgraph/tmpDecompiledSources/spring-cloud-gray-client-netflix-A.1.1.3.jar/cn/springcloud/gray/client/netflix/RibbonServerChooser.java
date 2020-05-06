/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.GrayClientHolder
 *  cn.springcloud.gray.GrayManager
 *  cn.springcloud.gray.ServerChooser
 *  cn.springcloud.gray.ServerListResult
 *  cn.springcloud.gray.choose.GrayPredicate
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayService
 *  cn.springcloud.gray.request.GrayRequest
 *  cn.springcloud.gray.request.RequestLocalStorage
 *  cn.springcloud.gray.servernode.ServerExplainer
 *  cn.springcloud.gray.servernode.ServerListProcessor
 *  cn.springcloud.gray.servernode.ServerSpec
 *  com.netflix.loadbalancer.Server
 *  com.netflix.loadbalancer.Server$MetaInfo
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.collections.ListUtils
 */
package cn.springcloud.gray.client.netflix;

import cn.springcloud.gray.GrayClientHolder;
import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.ServerChooser;
import cn.springcloud.gray.ServerListResult;
import cn.springcloud.gray.choose.GrayPredicate;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayService;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.RequestLocalStorage;
import cn.springcloud.gray.servernode.ServerExplainer;
import cn.springcloud.gray.servernode.ServerListProcessor;
import cn.springcloud.gray.servernode.ServerSpec;
import com.netflix.loadbalancer.Server;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;

public class RibbonServerChooser
implements ServerChooser<Server> {
    private GrayManager grayManager;
    private RequestLocalStorage requestLocalStorage;
    private GrayPredicate grayPredicate;
    private ServerExplainer<Server> serverExplainer;
    protected ServerListProcessor serverListProcessor;

    public RibbonServerChooser(GrayManager grayManager, RequestLocalStorage requestLocalStorage, GrayPredicate grayPredicate, ServerExplainer<Server> serverExplainer, ServerListProcessor serverListProcessor) {
        this.grayManager = grayManager;
        this.requestLocalStorage = requestLocalStorage;
        this.grayPredicate = grayPredicate;
        this.serverExplainer = serverExplainer;
        this.serverListProcessor = serverListProcessor;
    }

    public boolean matchGrayDecisions(ServerSpec serverSpec) {
        return this.grayPredicate.apply(serverSpec);
    }

    public boolean matchGrayDecisions(Server server) {
        return this.matchGrayDecisions(this.serverExplainer.apply((Object)server));
    }

    public ServerListResult<Server> distinguishServerList(List<Server> servers) {
        String serviceId = this.getServiceId(servers);
        if (StringUtils.isEmpty(serviceId)) {
            return null;
        }
        return this.distinguishServerList(serviceId, servers);
    }

    public ServerListResult<Server> distinguishAndMatchGrayServerList(List<Server> servers) {
        ServerListResult<Server> serverListResult = this.distinguishServerList(servers);
        if (serverListResult == null) {
            return null;
        }
        if (GrayClientHolder.getGraySwitcher().isEanbleGrayRouting()) {
            serverListResult.setGrayServers(serverListResult.getGrayServers().stream().filter(this::matchGrayDecisions).collect(Collectors.toList()));
        } else {
            serverListResult.setGrayServers(ListUtils.EMPTY_LIST);
        }
        return serverListResult;
    }

    private String getServiceId(List<Server> servers) {
        Server server;
        GrayRequest grayRequest = this.requestLocalStorage.getGrayRequest();
        if (grayRequest != null && StringUtils.isNotEmpty(grayRequest.getServiceId())) {
            return grayRequest.getServiceId();
        }
        if (CollectionUtils.isNotEmpty(servers) && !Objects.isNull((Object)(server = servers.get(0)))) {
            return server.getMetaInfo().getServiceIdForDiscovery();
        }
        return null;
    }

    private ServerListResult<Server> distinguishServerList(String serviceId, List<Server> servers) {
        if (!this.grayManager.hasGray(serviceId)) {
            return null;
        }
        GrayService grayService = this.grayManager.getGrayService(serviceId);
        List serverList = this.serverListProcessor.process(serviceId, servers);
        ArrayList<Server> grayServers = new ArrayList<Server>(grayService.getGrayInstances().size());
        ArrayList<Server> normalServers = new ArrayList<Server>(Math.min(servers.size(), grayService.getGrayInstances().size()));
        for (Server server : serverList) {
            if (grayService.getGrayInstance(server.getMetaInfo().getInstanceId()) != null) {
                grayServers.add(server);
                continue;
            }
            normalServers.add(server);
        }
        return new ServerListResult(serviceId, grayServers, normalServers);
    }
}

