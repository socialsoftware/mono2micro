/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceInfo
 *  cn.springcloud.gray.model.InstanceInfo$InstanceInfoBuilder
 *  cn.springcloud.gray.model.InstanceStatus
 *  com.netflix.appinfo.InstanceInfo
 *  com.netflix.appinfo.InstanceInfo$InstanceStatus
 *  com.netflix.discovery.shared.Application
 *  com.netflix.eureka.registry.InstanceRegistry
 *  org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent
 *  org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent
 *  org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent
 *  org.springframework.context.event.EventListener
 */
package cn.springcloud.gray.eureka.server.listener;

import cn.springcloud.gray.eureka.server.EurekaInstatnceTransformer;
import cn.springcloud.gray.eureka.server.communicate.GrayCommunicateClient;
import cn.springcloud.gray.model.InstanceInfo;
import cn.springcloud.gray.model.InstanceStatus;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.registry.InstanceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.context.event.EventListener;

public class EurekaInstanceListener {
    private static final Logger log = LoggerFactory.getLogger(EurekaInstanceListener.class);
    private GrayCommunicateClient communicateClient;

    public EurekaInstanceListener(GrayCommunicateClient communicateClient) {
        this.communicateClient = communicateClient;
    }

    @EventListener
    public void listenDown(EurekaInstanceCanceledEvent event) {
        InstanceRegistry registry = (InstanceRegistry)event.getSource();
        com.netflix.appinfo.InstanceInfo instanceInfo = registry.getApplication(event.getAppName()).getByInstanceId(event.getServerId());
        InstanceStatus instanceStatus = EurekaInstatnceTransformer.toGrayInstanceStatus(instanceInfo.getStatus());
        this.sendNotice(instanceInfo, instanceStatus, "DOWN");
    }

    @EventListener
    public void listenRenew(EurekaInstanceRenewedEvent event) {
        com.netflix.appinfo.InstanceInfo instanceInfo = event.getInstanceInfo();
        InstanceStatus instanceStatus = EurekaInstatnceTransformer.toGrayInstanceStatus(instanceInfo.getStatus());
        this.sendNotice(instanceInfo, instanceStatus, "RENEW");
    }

    @EventListener
    public void listenRegistered(EurekaInstanceRegisteredEvent event) {
        com.netflix.appinfo.InstanceInfo instanceInfo = event.getInstanceInfo();
        InstanceStatus instanceStatus = EurekaInstatnceTransformer.toGrayInstanceStatus(instanceInfo.getStatus());
        this.sendNotice(instanceInfo, instanceStatus, "REGISTERED");
    }

    private void sendNotice(com.netflix.appinfo.InstanceInfo instanceInfo, InstanceStatus instanceStatus, String eventType) {
        log.info(MarkerFactory.getMarker(eventType), "{}  serviceId\uff1a{}, instanceId\uff1a{} ", eventType, instanceInfo.getAppName(), instanceInfo.getInstanceId());
        this.sendNotice(InstanceInfo.builder().instanceId(instanceInfo.getInstanceId()).serviceId(instanceInfo.getVIPAddress()).instanceStatus(instanceStatus).build(), eventType);
    }

    private void sendNotice(InstanceInfo instanceInfo, String eventType) {
        try {
            this.communicateClient.noticeInstanceInfo(instanceInfo);
        }
        catch (Exception e) {
            log.error("\u53d1\u9001\u5b9e\u4f8b{}\u6d88\u606f\u5931\u8d25,serviceId:{}, instanceId:{}", eventType, instanceInfo.getServiceId(), instanceInfo.getInstanceId(), e);
        }
    }
}

