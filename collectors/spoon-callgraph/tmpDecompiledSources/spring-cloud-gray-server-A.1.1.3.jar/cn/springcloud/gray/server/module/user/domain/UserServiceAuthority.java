/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.module.user.domain;

import java.util.Date;

public class UserServiceAuthority {
    private Long id;
    private String userId;
    private String serviceId;
    private String operator;
    private Date operateTime;

    public Long getId() {
        return this.id;
    }

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

    public void setId(Long id) {
        this.id = id;
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
        if (!(o instanceof UserServiceAuthority)) {
            return false;
        }
        UserServiceAuthority other = (UserServiceAuthority)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
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
        return other instanceof UserServiceAuthority;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
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
        return "UserServiceAuthority(id=" + this.getId() + ", userId=" + this.getUserId() + ", serviceId=" + this.getServiceId() + ", operator=" + this.getOperator() + ", operateTime=" + this.getOperateTime() + ")";
    }
}

