/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceInfo
 *  cn.springcloud.gray.model.InstanceInfo$InstanceInfoBuilder
 *  cn.springcloud.gray.model.InstanceStatus
 *  cn.springcloud.gray.server.discovery.ServiceDiscovery
 *  cn.springcloud.gray.server.discovery.ServiceInfo
 *  com.alibaba.cloud.nacos.NacosDiscoveryProperties
 *  com.alibaba.nacos.api.exception.NacosException
 *  com.alibaba.nacos.api.naming.NamingService
 *  com.alibaba.nacos.api.naming.pojo.Instance
 *  com.alibaba.nacos.api.naming.pojo.ListView
 *  org.apache.commons.collections.ListUtils
 */
package cn.springcloud.gray.server.plugin.discovery.nacos;

import cn.springcloud.gray.model.InstanceInfo;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.server.discovery.ServiceDiscovery;
import cn.springcloud.gray.server.discovery.ServiceInfo;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NacosServiceDiscovery
implements ServiceDiscovery {
    private static final Logger log = LoggerFactory.getLogger(NacosServiceDiscovery.class);
    private NacosDiscoveryProperties discoveryProperties;

    public NacosServiceDiscovery(NacosDiscoveryProperties discoveryProperties) {
        this.discoveryProperties = discoveryProperties;
    }

    public List<ServiceInfo> listAllSerivceInfos() {
        return this.getServices().stream().map(service -> new ServiceInfo(service)).collect(Collectors.toList());
    }

    public ServiceInfo getServiceInfo(String serviceId) {
        if (!this.getServices().contains(serviceId)) {
            return null;
        }
        return new ServiceInfo(serviceId);
    }

    public List<InstanceInfo> listInstanceInfos(String serviceId) {
        List instances = null;
        try {
            instances = this.discoveryProperties.namingServiceInstance().getAllInstances(serviceId, false);
        }
        catch (NacosException e) {
            log.error(e.getMessage(), e);
            return ListUtils.EMPTY_LIST;
        }
        return instances.stream().map(this::createInstanceInfo).collect(Collectors.toList());
    }

    public InstanceInfo getInstanceInfo(String serviceId, String instanceId) {
        return (InstanceInfo)this.getInstanceInfos(serviceId).get(instanceId);
    }

    private List<String> getServices() {
        try {
            return this.discoveryProperties.namingServiceInstance().getServicesOfServer(1, Integer.MAX_VALUE).getData();
        }
        catch (NacosException e) {
            return Collections.emptyList();
        }
    }

    private InstanceInfo createInstanceInfo(Instance instance) {
        InstanceStatus instanceStatus = InstanceStatus.DOWN;
        if (instance.isEnabled()) {
            instanceStatus = instance.isHealthy() ? InstanceStatus.UP : InstanceStatus.OUT_OF_SERVICE;
        }
        return InstanceInfo.builder().serviceId(instance.getServiceName()).instanceId(instance.getInstanceId()).host(instance.getIp()).port(instance.getPort()).instanceStatus(instanceStatus).build();
    }
}

