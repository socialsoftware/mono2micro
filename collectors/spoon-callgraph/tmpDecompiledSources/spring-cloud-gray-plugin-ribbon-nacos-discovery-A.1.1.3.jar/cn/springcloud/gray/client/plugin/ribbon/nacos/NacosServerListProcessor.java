/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.client.config.properties.GrayHoldoutServerProperties
 *  cn.springcloud.gray.model.InstanceStatus
 *  cn.springcloud.gray.servernode.AbstractServerListProcessor
 *  com.alibaba.cloud.nacos.ribbon.NacosServer
 *  com.alibaba.nacos.api.exception.NacosException
 *  com.alibaba.nacos.api.naming.NamingService
 *  com.alibaba.nacos.api.naming.pojo.Instance
 *  com.netflix.loadbalancer.Server
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.collections.ListUtils
 */
package cn.springcloud.gray.client.plugin.ribbon.nacos;

import cn.springcloud.gray.client.config.properties.GrayHoldoutServerProperties;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.servernode.AbstractServerListProcessor;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.loadbalancer.Server;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NacosServerListProcessor
extends AbstractServerListProcessor<Server> {
    private static final Logger log = LoggerFactory.getLogger(NacosServerListProcessor.class);
    private NamingService namingService;

    public NacosServerListProcessor(GrayHoldoutServerProperties grayHoldoutServerProperties, NamingService namingService) {
        super(grayHoldoutServerProperties);
        this.namingService = namingService;
    }

    protected List<Server> getServers(String serviceId, List<Server> servers) {
        List statusList = this.getHoldoutInstanceStatus(serviceId);
        List holdoutServers = this.getInstances(serviceId).stream().filter(instance -> statusList.contains((Object)this.getInstanceStatus((Instance)instance))).map(NacosServer::new).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(holdoutServers)) {
            return servers;
        }
        return ListUtils.union(servers, holdoutServers);
    }

    private InstanceStatus getInstanceStatus(Instance instance) {
        if (!instance.isEnabled()) {
            return InstanceStatus.DOWN;
        }
        if (!instance.isHealthy()) {
            return InstanceStatus.OUT_OF_SERVICE;
        }
        return InstanceStatus.UP;
    }

    private List<Instance> getInstances(String serviceId) {
        try {
            return this.namingService.getAllInstances(serviceId);
        }
        catch (NacosException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}

