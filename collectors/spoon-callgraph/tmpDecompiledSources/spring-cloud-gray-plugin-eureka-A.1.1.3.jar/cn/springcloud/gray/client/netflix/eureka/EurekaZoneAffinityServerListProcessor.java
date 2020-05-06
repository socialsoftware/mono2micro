/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.client.config.properties.GrayHoldoutServerProperties
 *  cn.springcloud.gray.model.InstanceStatus
 *  com.netflix.config.ConfigurationManager
 *  com.netflix.config.DeploymentContext
 *  com.netflix.config.DeploymentContext$ContextKey
 *  com.netflix.discovery.EurekaClient
 *  com.netflix.loadbalancer.Server
 */
package cn.springcloud.gray.client.netflix.eureka;

import cn.springcloud.gray.client.config.properties.GrayHoldoutServerProperties;
import cn.springcloud.gray.client.netflix.eureka.EurekaServerListProcessor;
import cn.springcloud.gray.model.InstanceStatus;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext;
import com.netflix.discovery.EurekaClient;
import com.netflix.loadbalancer.Server;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public class EurekaZoneAffinityServerListProcessor
extends EurekaServerListProcessor {
    private volatile String zone;

    public EurekaZoneAffinityServerListProcessor(GrayHoldoutServerProperties grayHoldoutServerProperties, EurekaClient eurekaClient) {
        super(grayHoldoutServerProperties, eurekaClient);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.zone = ConfigurationManager.getDeploymentContext().getValue(DeploymentContext.ContextKey.zone);
        super.afterPropertiesSet();
    }

    @Override
    protected List<Server> getUnUpServerList(String serviceId, List<InstanceStatus> instanceStatuses) {
        List<Server> unUpServers = super.getUnUpServerList(serviceId, instanceStatuses);
        return this.getFilteredListOfServers(unUpServers);
    }

    private List<Server> getFilteredListOfServers(List<Server> servers) {
        return servers.stream().filter(this::matchZoneAffinityServer).collect(Collectors.toList());
    }

    private boolean matchZoneAffinityServer(Server server) {
        String zone = this.getZone();
        if (StringUtils.isEmpty(zone)) {
            return true;
        }
        return StringUtils.equals(zone, server.getZone());
    }

    private String getZone() {
        if (StringUtils.isEmpty(this.zone)) {
            this.zone = ConfigurationManager.getDeploymentContext().getValue(DeploymentContext.ContextKey.zone);
        }
        return this.zone;
    }
}

