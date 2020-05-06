/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.model;

import cn.springcloud.gray.model.GrayStatus;
import cn.springcloud.gray.model.PolicyDefinition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@JsonIgnoreProperties(value={"gray"})
public class GrayInstance
implements Serializable {
    private static final long serialVersionUID = 1604426811546120884L;
    private String serviceId;
    private String instanceId;
    private String host;
    private Integer port;
    private List<PolicyDefinition> policyDefinitions = new CopyOnWriteArrayList<PolicyDefinition>();
    private GrayStatus grayStatus;

    public boolean isGray() {
        return this.grayStatus == GrayStatus.OPEN;
    }

    public String toString() {
        return "GrayInstance(serviceId=" + this.getServiceId() + ", instanceId=" + this.getInstanceId() + ", host=" + this.getHost() + ", port=" + this.getPort() + ", policyDefinitions=" + this.getPolicyDefinitions() + ", grayStatus=" + (Object)((Object)this.getGrayStatus()) + ")";
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

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setPolicyDefinitions(List<PolicyDefinition> policyDefinitions) {
        this.policyDefinitions = policyDefinitions;
    }

    public void setGrayStatus(GrayStatus grayStatus) {
        this.grayStatus = grayStatus;
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

    public Integer getPort() {
        return this.port;
    }

    public List<PolicyDefinition> getPolicyDefinitions() {
        return this.policyDefinitions;
    }

    public GrayStatus getGrayStatus() {
        return this.grayStatus;
    }
}

