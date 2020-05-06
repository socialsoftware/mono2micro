/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.dao.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name="user_service_authority", indexes={@Index(columnList="serviceId"), @Index(columnList="userId")})
public class UserServiceAuthorityDO {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(length=32)
    private String userId;
    @Column(length=32)
    private String serviceId;
    @Column(length=32)
    private String operator;
    @Column
    private Date operateTime;

    public static UserServiceAuthorityDOBuilder builder() {
        return new UserServiceAuthorityDOBuilder();
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

    public UserServiceAuthorityDO() {
    }

    public UserServiceAuthorityDO(Long id, String userId, String serviceId, String operator, Date operateTime) {
        this.id = id;
        this.userId = userId;
        this.serviceId = serviceId;
        this.operator = operator;
        this.operateTime = operateTime;
    }

    public static class UserServiceAuthorityDOBuilder {
        private Long id;
        private String userId;
        private String serviceId;
        private String operator;
        private Date operateTime;

        UserServiceAuthorityDOBuilder() {
        }

        public UserServiceAuthorityDOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserServiceAuthorityDOBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserServiceAuthorityDOBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public UserServiceAuthorityDOBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public UserServiceAuthorityDOBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public UserServiceAuthorityDO build() {
            return new UserServiceAuthorityDO(this.id, this.userId, this.serviceId, this.operator, this.operateTime);
        }

        public String toString() {
            return "UserServiceAuthorityDO.UserServiceAuthorityDOBuilder(id=" + this.id + ", userId=" + this.userId + ", serviceId=" + this.serviceId + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ")";
        }
    }

}

