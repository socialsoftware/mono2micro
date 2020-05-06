/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceInfo
 *  cn.springcloud.gray.model.InstanceStatus
 *  cn.springcloud.gray.server.discovery.ServiceDiscovery
 *  cn.springcloud.gray.server.discovery.ServiceInfo
 *  com.netflix.appinfo.InstanceInfo
 *  com.netflix.appinfo.InstanceInfo$InstanceStatus
 *  com.netflix.discovery.EurekaClient
 *  com.netflix.discovery.shared.Application
 *  com.netflix.discovery.shared.Applications
 */
package cn.springcloud.gray.server.netflix.eureka;

import cn.springcloud.gray.model.InstanceInfo;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.server.discovery.ServiceDiscovery;
import cn.springcloud.gray.server.discovery.ServiceInfo;
import cn.springcloud.gray.server.netflix.eureka.EurekaInstatnceTransformer;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EurekaServiceDiscovery
implements ServiceDiscovery {
    private EurekaClient eurekaClient;

    public EurekaServiceDiscovery(EurekaClient eurekaClient) {
        this.eurekaClient = eurekaClient;
    }

    public List<ServiceInfo> listAllSerivceInfos() {
        Applications applications = this.eurekaClient.getApplications();
        if (applications == null) {
            return Collections.emptyList();
        }
        List applicationList = applications.getRegisteredApplications();
        return applicationList.stream().map(this::ofApplication).collect(Collectors.toList());
    }

    public ServiceInfo getServiceInfo(String serviceId) {
        Application application = this.eurekaClient.getApplication(serviceId);
        if (application != null) {
            return this.ofApplication(application);
        }
        return null;
    }

    public List<InstanceInfo> listInstanceInfos(String serviceId) {
        Application application = this.eurekaClient.getApplication(serviceId);
        if (application == null) {
            return Collections.emptyList();
        }
        List eurekaInstanceInfos = application.getInstancesAsIsFromEureka();
        return eurekaInstanceInfos.stream().map(this::ofInstance).collect(Collectors.toList());
    }

    public InstanceInfo getInstanceInfo(String serviceId, String instanceId) {
        com.netflix.appinfo.InstanceInfo eurekaInstanceInfo;
        Application application = this.eurekaClient.getApplication(serviceId);
        if (application != null && (eurekaInstanceInfo = application.getByInstanceId(instanceId)) != null) {
            return this.ofInstance(eurekaInstanceInfo);
        }
        return null;
    }

    private ServiceInfo ofApplication(Application application) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceId(application.getName());
        return serviceInfo;
    }

    private InstanceInfo ofInstance(com.netflix.appinfo.InstanceInfo eurekaInstanceInfo) {
        InstanceInfo instanceInfo = new InstanceInfo();
        instanceInfo.setServiceId(eurekaInstanceInfo.getAppName());
        instanceInfo.setInstanceId(eurekaInstanceInfo.getInstanceId());
        instanceInfo.setHost(eurekaInstanceInfo.getIPAddr());
        instanceInfo.setPort(eurekaInstanceInfo.getPort());
        instanceInfo.setInstanceStatus(this.ofEurekaInstanceStatus(eurekaInstanceInfo.getStatus()));
        return instanceInfo;
    }

    private InstanceStatus ofEurekaInstanceStatus(InstanceInfo.InstanceStatus eurekaInstanceStatus) {
        return EurekaInstatnceTransformer.toGrayInstanceStatus(eurekaInstanceStatus);
    }
}

