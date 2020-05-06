/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.module.user.domain;

import java.util.Date;

public class ServiceOwner {
    private String userId;
    private String serviceId;
    private String operator;
    private Date operateTime;

    public String getUserId() {
        return this.userId;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public String getOperator() {
        return this.operator;
    }

    public Date getOperateTime() {
        return this.operateTime;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ServiceOwner)) {
            return false;
        }
        ServiceOwner other = (ServiceOwner)o;
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
        if (this$serviceId == null ? other$serviceId != null : !this$serviceId.equals(other$serviceId)) {
            return false;
        }
        String this$operator = this.getOperator();
        String other$operator = other.getOperator();
        if (this$operator == null ? other$operator != null : !this$operator.equals(other$operator)) {
            return false;
        }
        Date this$operateTime = this.getOperateTime();
        Date other$operateTime = other.getOperateTime();
        return !(this$operateTime == null ? other$operateTime != null : !((Object)this$operateTime).equals(other$operateTime));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ServiceOwner;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $userId = this.getUserId();
        result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
        String $serviceId = this.getServiceId();
        result = result * 59 + ($serviceId == null ? 43 : $serviceId.hashCode());
        String $operator = this.getOperator();
        result = result * 59 + ($operator == null ? 43 : $operator.hashCode());
        Date $operateTime = this.getOperateTime();
        result = result * 59 + ($operateTime == null ? 43 : ((Object)$operateTime).hashCode());
        return result;
    }

    public String toString() {
        return "ServiceOwner(userId=" + this.getUserId() + ", serviceId=" + this.getServiceId() + ", operator=" + this.getOperator() + ", operateTime=" + this.getOperateTime() + ")";
    }
}

