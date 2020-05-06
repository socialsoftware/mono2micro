/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.model;

import cn.springcloud.gray.model.InstanceStatus;

public class InstanceInfo {
    private String serviceId;
    private String instanceId;
    private String host;
    private int port;
    private InstanceStatus instanceStatus;

    public static InstanceInfoBuilder builder() {
        return new InstanceInfoBuilder();
    }

    public String toString() {
        return "InstanceInfo(serviceId=" + this.getServiceId() + ", instanceId=" + this.getInstanceId() + ", host=" + this.getHost() + ", port=" + this.getPort() + ", instanceStatus=" + (Object)((Object)this.getInstanceStatus()) + ")";
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
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

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public InstanceStatus getInstanceStatus() {
        return this.instanceStatus;
    }

    public InstanceInfo(String serviceId, String instanceId, String host, int port, InstanceStatus instanceStatus) {
        this.serviceId = serviceId;
        this.instanceId = instanceId;
        this.host = host;
        this.port = port;
        this.instanceStatus = instanceStatus;
    }

    public InstanceInfo() {
    }

    public static class InstanceInfoBuilder {
        private String serviceId;
        private String instanceId;
        private String host;
        private int port;
        private InstanceStatus instanceStatus;

        InstanceInfoBuilder() {
        }

        public InstanceInfoBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public InstanceInfoBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public InstanceInfoBuilder host(String host) {
            this.host = host;
            return this;
        }

        public InstanceInfoBuilder port(int port) {
            this.port = port;
            return this;
        }

        public InstanceInfoBuilder instanceStatus(InstanceStatus instanceStatus) {
            this.instanceStatus = instanceStatus;
            return this;
        }

        public InstanceInfo build() {
            return new InstanceInfo(this.serviceId, this.instanceId, this.host, this.port, this.instanceStatus);
        }

        public String toString() {
            return "InstanceInfo.InstanceInfoBuilder(serviceId=" + this.serviceId + ", instanceId=" + this.instanceId + ", host=" + this.host + ", port=" + this.port + ", instanceStatus=" + (Object)((Object)this.instanceStatus) + ")";
        }
    }

}

