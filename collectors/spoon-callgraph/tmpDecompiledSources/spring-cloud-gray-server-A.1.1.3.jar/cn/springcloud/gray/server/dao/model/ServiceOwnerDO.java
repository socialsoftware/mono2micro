/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.dao.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name="service_owner", indexes={@Index(columnList="userId")})
public class ServiceOwnerDO {
    @Id
    private String serviceId;
    @Column(length=32)
    private String userId;
    @Column(length=32)
    private String operator;
    @Column
    private Date operateTime;

    public static ServiceOwnerDOBuilder builder() {
        return new ServiceOwnerDOBuilder();
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getOperator() {
        return this.operator;
    }

    public Date getOperateTime() {
        return this.operateTime;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
        if (!(o instanceof ServiceOwnerDO)) {
            return false;
        }
        ServiceOwnerDO other = (ServiceOwnerDO)o;
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
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) {
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
        return other instanceof ServiceOwnerDO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $serviceId = this.getServiceId();
        result = result * 59 + ($serviceId == null ? 43 : $serviceId.hashCode());
        String $userId = this.getUserId();
        result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
        String $operator = this.getOperator();
        result = result * 59 + ($operator == null ? 43 : $operator.hashCode());
        Date $operateTime = this.getOperateTime();
        result = result * 59 + ($operateTime == null ? 43 : ((Object)$operateTime).hashCode());
        return result;
    }

    public String toString() {
        return "ServiceOwnerDO(serviceId=" + this.getServiceId() + ", userId=" + this.getUserId() + ", operator=" + this.getOperator() + ", operateTime=" + this.getOperateTime() + ")";
    }

    public ServiceOwnerDO() {
    }

    public ServiceOwnerDO(String serviceId, String userId, String operator, Date operateTime) {
        this.serviceId = serviceId;
        this.userId = userId;
        this.operator = operator;
        this.operateTime = operateTime;
    }

    public static class ServiceOwnerDOBuilder {
        private String serviceId;
        private String userId;
        private String operator;
        private Date operateTime;

        ServiceOwnerDOBuilder() {
        }

        public ServiceOwnerDOBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public ServiceOwnerDOBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public ServiceOwnerDOBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public ServiceOwnerDOBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public ServiceOwnerDO build() {
            return new ServiceOwnerDO(this.serviceId, this.userId, this.operator, this.operateTime);
        }

        public String toString() {
            return "ServiceOwnerDO.ServiceOwnerDOBuilder(serviceId=" + this.serviceId + ", userId=" + this.userId + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ")";
        }
    }

}

