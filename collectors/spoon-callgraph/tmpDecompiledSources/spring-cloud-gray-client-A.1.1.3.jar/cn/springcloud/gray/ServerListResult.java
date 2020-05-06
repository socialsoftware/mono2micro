/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray;

import java.util.List;

public class ServerListResult<Server> {
    private String serviceId;
    private List<Server> grayServers;
    private List<Server> normalServers;

    public String getServiceId() {
        return this.serviceId;
    }

    public List<Server> getGrayServers() {
        return this.grayServers;
    }

    public List<Server> getNormalServers() {
        return this.normalServers;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setGrayServers(List<Server> grayServers) {
        this.grayServers = grayServers;
    }

    public void setNormalServers(List<Server> normalServers) {
        this.normalServers = normalServers;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ServerListResult)) {
            return false;
        }
        ServerListResult other = (ServerListResult)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$serviceId = this.getServiceId();
        String other$serviceId = other.getServiceId();
        if (this$serviceId == null ? other$serviceId != null : !this$serviceId.equals(other$serviceId)) {
            return false;
        }
        List<Server> this$grayServers = this.getGrayServers();
        List<Server> other$grayServers = other.getGrayServers();
        if (this$grayServers == null ? other$grayServers != null : !((Object)this$grayServers).equals(other$grayServers)) {
            return false;
        }
        List<Server> this$normalServers = this.getNormalServers();
        List<Server> other$normalServers = other.getNormalServers();
        return !(this$normalServers == null ? other$normalServers != null : !((Object)this$normalServers).equals(other$normalServers));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ServerListResult;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $serviceId = this.getServiceId();
        result = result * 59 + ($serviceId == null ? 43 : $serviceId.hashCode());
        List<Server> $grayServers = this.getGrayServers();
        result = result * 59 + ($grayServers == null ? 43 : ((Object)$grayServers).hashCode());
        List<Server> $normalServers = this.getNormalServers();
        result = result * 59 + ($normalServers == null ? 43 : ((Object)$normalServers).hashCode());
        return result;
    }

    public String toString() {
        return "ServerListResult(serviceId=" + this.getServiceId() + ", grayServers=" + this.getGrayServers() + ", normalServers=" + this.getNormalServers() + ")";
    }

    public ServerListResult() {
    }

    public ServerListResult(String serviceId, List<Server> grayServers, List<Server> normalServers) {
        this.serviceId = serviceId;
        this.grayServers = grayServers;
        this.normalServers = normalServers;
    }
}

