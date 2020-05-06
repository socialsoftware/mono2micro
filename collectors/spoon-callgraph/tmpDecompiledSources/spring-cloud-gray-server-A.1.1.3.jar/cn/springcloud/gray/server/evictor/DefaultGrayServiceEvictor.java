/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceInfo
 *  cn.springcloud.gray.model.InstanceStatus
 */
package cn.springcloud.gray.server.evictor;

import cn.springcloud.gray.model.InstanceInfo;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.server.configuration.properties.GrayServerProperties;
import cn.springcloud.gray.server.discovery.ServiceDiscovery;
import cn.springcloud.gray.server.discovery.ServiceInfo;
import cn.springcloud.gray.server.evictor.GrayServerEvictor;
import cn.springcloud.gray.server.module.gray.GrayServerModule;
import cn.springcloud.gray.server.module.gray.domain.GrayInstance;
import cn.springcloud.gray.server.module.gray.domain.GrayService;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class DefaultGrayServiceEvictor
implements GrayServerEvictor {
    private ServiceDiscovery serviceDiscovery;
    private GrayServerProperties grayServerProperties;

    public DefaultGrayServiceEvictor(GrayServerProperties grayServerProperties, ServiceDiscovery serviceDiscovery) {
        this.grayServerProperties = grayServerProperties;
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public void evict(GrayServerModule grayServerModule) {
        grayServerModule.allGrayServices().forEach(grayService -> {
            ServiceInfo serviceInfo = this.serviceDiscovery.getServiceInfo(grayService.getServiceId());
            if (serviceInfo == null) {
                this.downAllInstance(grayServerModule, (GrayService)grayService);
            } else {
                List<GrayInstance> grayInstances = grayServerModule.listGrayInstancesByServiceId(grayService.getServiceId());
                grayInstances.forEach(grayInstance -> {
                    InstanceInfo instanceInfo = this.serviceDiscovery.getInstanceInfo(grayInstance.getServiceId(), grayInstance.getInstanceId());
                    this.updateInstanceStatus(grayServerModule, (GrayInstance)grayInstance, instanceInfo);
                });
            }
        });
    }

    private void updateInstanceStatus(GrayServerModule grayServerModule, GrayInstance grayInstance, InstanceInfo instanceInfo) {
        InstanceStatus instanceStatus;
        InstanceStatus instanceStatus2 = instanceStatus = instanceInfo == null ? InstanceStatus.DOWN : instanceInfo.getInstanceStatus();
        if (!Objects.equals((Object)grayInstance.getInstanceStatus(), (Object)instanceStatus)) {
            grayServerModule.updateInstanceStatus(grayInstance.getInstanceId(), instanceStatus);
        }
    }

    private void downAllInstance(GrayServerModule grayServerModule, GrayService grayService) {
        List<GrayInstance> grayInstances = grayServerModule.listGrayInstancesByServiceId(grayService.getServiceId(), this.grayServerProperties.getInstance().getNormalInstanceStatus());
        grayInstances.forEach(i -> grayServerModule.instanceShutdown(i.getInstanceId()));
    }

    private InstanceStatus getInstanceStatus(InstanceInfo instanceInfo) {
        if (instanceInfo == null) {
            return InstanceStatus.DOWN;
        }
        return instanceInfo.getInstanceStatus();
    }
}

