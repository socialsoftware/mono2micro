/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.discovery;

public class ServiceInfo {
    private String serviceId;

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public ServiceInfo(String serviceId) {
        this.serviceId = serviceId;
    }

    public ServiceInfo() {
    }
}

