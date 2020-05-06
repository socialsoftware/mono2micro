/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.client.config.properties.GrayHoldoutServerProperties
 *  cn.springcloud.gray.model.InstanceStatus
 *  cn.springcloud.gray.servernode.ServerListProcessor
 *  com.netflix.appinfo.InstanceInfo
 *  com.netflix.appinfo.InstanceInfo$InstanceStatus
 *  com.netflix.discovery.EurekaClient
 *  com.netflix.discovery.EurekaEvent
 *  com.netflix.discovery.EurekaEventListener
 *  com.netflix.discovery.shared.Application
 *  com.netflix.loadbalancer.Server
 *  com.netflix.niws.loadbalancer.DiscoveryEnabledServer
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.collections.ListUtils
 *  org.springframework.beans.factory.InitializingBean
 *  org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent
 *  org.springframework.context.event.EventListener
 *  org.springframework.core.annotation.Order
 */
package cn.springcloud.gray.client.netflix.eureka;

import cn.springcloud.gray.client.config.properties.GrayHoldoutServerProperties;
import cn.springcloud.gray.client.netflix.eureka.EurekaInstatnceTransformer;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.servernode.ServerListProcessor;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaEvent;
import com.netflix.discovery.EurekaEventListener;
import com.netflix.discovery.shared.Application;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

public class EurekaServerListProcessor
implements ServerListProcessor<Server>,
EurekaEventListener,
InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(EurekaServerListProcessor.class);
    private EurekaClient eurekaClient;
    private GrayHoldoutServerProperties grayHoldoutServerProperties;
    private Semaphore semaphore = new Semaphore(1);
    private volatile Map<String, List<Server>> unUpServersMap = new HashMap<String, List<Server>>();
    private Map<String, List<Server>> serversMap = new ConcurrentHashMap<String, List<Server>>();

    public EurekaServerListProcessor(GrayHoldoutServerProperties grayHoldoutServerProperties, EurekaClient eurekaClient) {
        this.eurekaClient = eurekaClient;
        this.grayHoldoutServerProperties = grayHoldoutServerProperties;
    }

    public List<Server> process(String serviceId, List<Server> servers) {
        if (!this.grayHoldoutServerProperties.isEnabled() || CollectionUtils.isEmpty((Collection)((Collection)this.grayHoldoutServerProperties.getServices().get(serviceId)))) {
            return servers;
        }
        List serverList = null;
        if (this.grayHoldoutServerProperties.isCacheable() && CollectionUtils.isNotEmpty(serverList = this.serversMap.get(serviceId))) {
            return serverList;
        }
        serverList = servers;
        List<Server> unUpServers = this.getUnUpServers(serviceId);
        if (CollectionUtils.isNotEmpty(unUpServers)) {
            serverList = ListUtils.union(servers, unUpServers);
        }
        if (this.grayHoldoutServerProperties.isCacheable()) {
            this.serversMap.put(serviceId, serverList);
        }
        return serverList;
    }

    public void onEvent(EurekaEvent event) {
        log.debug("\u63a5\u6536\u5230eureka\u4e8b\u4ef6:{}, \u5237\u65b0\u7f13\u5b58\u7684server list", (Object)event);
        this.reload();
    }

    private List<Server> getUnUpServers(String serviceId) {
        return this.unUpServersMap.get(serviceId);
    }

    protected List<Server> getUnUpServerList(String serviceId, List<InstanceStatus> instanceStatuses) {
        Application application = this.eurekaClient.getApplication(serviceId);
        if (Objects.isNull((Object)application)) {
            return null;
        }
        return application.getInstancesAsIsFromEureka().stream().filter(instanceInfo -> instanceStatuses.contains((Object)EurekaInstatnceTransformer.toGrayInstanceStatus(instanceInfo.getStatus()))).map(instanceInfo -> {
            DiscoveryEnabledServer server = new DiscoveryEnabledServer(instanceInfo, false);
            String zone = (String)server.getInstanceInfo().getMetadata().get("zone");
            if (StringUtils.isNotEmpty(zone)) {
                server.setZone(zone);
            }
            return server;
        }).collect(Collectors.toList());
    }

    private void reloadUpServersMap() {
        HashMap<String, List<Server>> unUpServersMap = new HashMap<String, List<Server>>();
        this.grayHoldoutServerProperties.getServices().forEach((serviceId, instanceStatuses) -> {
            if (CollectionUtils.isEmpty((Collection)instanceStatuses)) {
                return;
            }
            List<Server> unUpServers = this.getUnUpServerList((String)serviceId, (List<InstanceStatus>)instanceStatuses);
            if (CollectionUtils.isNotEmpty(unUpServers)) {
                unUpServersMap.put((String)serviceId, unUpServers);
            }
        });
        this.unUpServersMap = unUpServersMap;
    }

    public void afterPropertiesSet() throws Exception {
        this.reloadAndRegister();
    }

    @Order
    @EventListener(value={RefreshScopeRefreshedEvent.class})
    public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
        this.reloadAndRegister();
    }

    public void reload() {
        if (!this.grayHoldoutServerProperties.isEnabled()) {
            this.unUpServersMap.clear();
            this.serversMap.clear();
            return;
        }
        if (!this.semaphore.tryAcquire()) {
            log.info("\u5df2\u6709\u5176\u5b83\u7ebf\u7a0b\u5728\u6267\u884creload");
            return;
        }
        try {
            this.reloadUpServersMap();
            this.serversMap.clear();
        }
        finally {
            this.semaphore.release();
        }
    }

    public void reloadAndRegister() {
        try {
            this.reload();
        }
        finally {
            this.eurekaClient.registerEventListener((EurekaEventListener)this);
        }
    }
}

