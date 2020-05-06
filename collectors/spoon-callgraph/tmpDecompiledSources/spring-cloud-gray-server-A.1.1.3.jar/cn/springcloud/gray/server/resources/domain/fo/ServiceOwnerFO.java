/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModel
 *  io.swagger.annotations.ApiModelProperty
 *  org.hibernate.validator.constraints.NotEmpty
 */
package cn.springcloud.gray.server.resources.domain.fo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotEmpty;

@ApiModel
public class ServiceOwnerFO {
    @NotEmpty
    @ApiModelProperty(value="\u670d\u52a1id")
    private String serviceId;
    @NotEmpty
    @ApiModelProperty(value="\u7528\u6237id")
    private String userId;

    public String getServiceId() {
        return this.serviceId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ServiceOwnerFO)) {
            return false;
        }
        ServiceOwnerFO other = (ServiceOwnerFO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$serviceId = this.getServiceId();
        String other$serviceId = other.getServiceId();
        if (this$serviceId == null ? other$serviceId != null : !this$serviceId.equals(other$serviceId)) {
            return false;
        }
        String this$userId = this.getUserId();
        String other$userId = other.getUserId();
        return !(this$userId == null ? other$userId != null : !this$userId.equals(other$userId));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ServiceOwnerFO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $serviceId = this.getServiceId();
        result = result * 59 + ($serviceId == null ? 43 : $serviceId.hashCode());
        String $userId = this.getUserId();
        result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
        return result;
    }

    public String toString() {
        return "ServiceOwnerFO(serviceId=" + this.getServiceId() + ", userId=" + this.getUserId() + ")";
    }
}

