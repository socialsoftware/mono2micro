/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceStatus
 */
package cn.springcloud.gray.server.resources.domain.fo;

import cn.springcloud.gray.model.InstanceStatus;

public class RemoteInstanceStatusUpdateFO {
    private String serviceId;
    private String instanceId;
    private InstanceStatus instanceStatus;

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setInstanceStatus(InstanceStatus instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public InstanceStatus getInstanceStatus() {
        return this.instanceStatus;
    }
}

