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
@Table(name="user", indexes={@Index(columnList="account"), @Index(columnList="name")})
public class UserDO {
    @Id
    @Column(length=32)
    private String userId;
    @Column(length=128)
    private String password;
    @Column(length=126)
    private String name;
    @Column(length=32)
    private String account;
    @Column(length=64)
    private String roles;
    @Column(length=4)
    private Integer status;
    @Column(length=32)
    private String operator;
    @Column
    private Date operateTime;
    @Column
    private Date createTime;

    public static UserDOBuilder builder() {
        return new UserDOBuilder();
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getPassword() {
        return this.password;
    }

    public String getName() {
        return this.name;
    }

    public String getAccount() {
        return this.account;
    }

    public String getRoles() {
        return this.roles;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getOperator() {
        return this.operator;
    }

    public Date getOperateTime() {
        return this.operateTime;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public UserDO() {
    }

    public UserDO(String userId, String password, String name, String account, String roles, Integer status, String operator, Date operateTime, Date createTime) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.account = account;
        this.roles = roles;
        this.status = status;
        this.operator = operator;
        this.operateTime = operateTime;
        this.createTime = createTime;
    }

    public static class UserDOBuilder {
        private String userId;
        private String password;
        private String name;
        private String account;
        private String roles;
        private Integer status;
        private String operator;
        private Date operateTime;
        private Date createTime;

        UserDOBuilder() {
        }

        public UserDOBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserDOBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserDOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserDOBuilder account(String account) {
            this.account = account;
            return this;
        }

        public UserDOBuilder roles(String roles) {
            this.roles = roles;
            return this;
        }

        public UserDOBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public UserDOBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public UserDOBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public UserDOBuilder createTime(Date createTime) {
            this.createTime = createTime;
            return this;
        }

        public UserDO build() {
            return new UserDO(this.userId, this.password, this.name, this.account, this.roles, this.status, this.operator, this.operateTime, this.createTime);
        }

        public String toString() {
            return "UserDO.UserDOBuilder(userId=" + this.userId + ", password=" + this.password + ", name=" + this.name + ", account=" + this.account + ", roles=" + this.roles + ", status=" + this.status + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ", createTime=" + this.createTime + ")";
        }
    }

}

