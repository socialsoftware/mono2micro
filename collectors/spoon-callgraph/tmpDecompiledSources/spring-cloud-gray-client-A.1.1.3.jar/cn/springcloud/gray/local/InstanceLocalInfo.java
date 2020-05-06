/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.local;

public class InstanceLocalInfo {
    private String serviceId;
    private String instanceId;
    private String host;
    private int port;
    private boolean isGray;

    public static InstanceLocalInfoBuilder builder() {
        return new InstanceLocalInfoBuilder();
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

    public boolean isGray() {
        return this.isGray;
    }

    public InstanceLocalInfo(String serviceId, String instanceId, String host, int port, boolean isGray) {
        this.serviceId = serviceId;
        this.instanceId = instanceId;
        this.host = host;
        this.port = port;
        this.isGray = isGray;
    }

    public InstanceLocalInfo() {
    }

    public void setGray(boolean isGray) {
        this.isGray = isGray;
    }

    public static class InstanceLocalInfoBuilder {
        private String serviceId;
        private String instanceId;
        private String host;
        private int port;
        private boolean isGray;

        InstanceLocalInfoBuilder() {
        }

        public InstanceLocalInfoBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public InstanceLocalInfoBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public InstanceLocalInfoBuilder host(String host) {
            this.host = host;
            return this;
        }

        public InstanceLocalInfoBuilder port(int port) {
            this.port = port;
            return this;
        }

        public InstanceLocalInfoBuilder isGray(boolean isGray) {
            this.isGray = isGray;
            return this;
        }

        public InstanceLocalInfo build() {
            return new InstanceLocalInfo(this.serviceId, this.instanceId, this.host, this.port, this.isGray);
        }

        public String toString() {
            return "InstanceLocalInfo.InstanceLocalInfoBuilder(serviceId=" + this.serviceId + ", instanceId=" + this.instanceId + ", host=" + this.host + ", port=" + this.port + ", isGray=" + this.isGray + ")";
        }
    }

}

