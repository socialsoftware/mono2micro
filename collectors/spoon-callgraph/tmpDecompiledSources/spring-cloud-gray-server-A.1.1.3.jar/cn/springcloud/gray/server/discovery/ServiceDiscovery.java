/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceInfo
 */
package cn.springcloud.gray.server.discovery;

import cn.springcloud.gray.model.InstanceInfo;
import cn.springcloud.gray.server.discovery.ServiceInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ServiceDiscovery {
    public List<ServiceInfo> listAllSerivceInfos();

    public ServiceInfo getServiceInfo(String var1);

    public List<InstanceInfo> listInstanceInfos(String var1);

    public InstanceInfo getInstanceInfo(String var1, String var2);

    default public Map<String, InstanceInfo> getInstanceInfos(String serviceId) {
        return this.listInstanceInfos(serviceId).stream().collect(Collectors.toMap(info -> info.getInstanceId(), info -> info));
    }
}

