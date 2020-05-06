/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModel
 *  io.swagger.annotations.ApiModelProperty
 */
package cn.springcloud.gray.server.resources.domain.fo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class ServiceAuthorityFO {
    @ApiModelProperty(value="\u7528\u6237id")
    private String userId;
    @ApiModelProperty(value="\u670d\u52a1id")
    private String serviceId;

    public String getUserId() {
        return this.userId;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ServiceAuthorityFO)) {
            return false;
        }
        ServiceAuthorityFO other = (ServiceAuthorityFO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$userId = this.getUserId();
        String other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) {
            return false;
        }
        String this$serviceId = this.getServiceId();
        String other$serviceId = other.getServiceId();
        return !(this$serviceId == null ? other$serviceId != null : !this$serviceId.equals(other$serviceId));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ServiceAuthorityFO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $userId = this.getUserId();
        result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
        String $serviceId = this.getServiceId();
        result = result * 59 + ($serviceId == null ? 43 : $serviceId.hashCode());
        return result;
    }

    public String toString() {
        return "ServiceAuthorityFO(userId=" + this.getUserId() + ", serviceId=" + this.getServiceId() + ")";
    }
}

