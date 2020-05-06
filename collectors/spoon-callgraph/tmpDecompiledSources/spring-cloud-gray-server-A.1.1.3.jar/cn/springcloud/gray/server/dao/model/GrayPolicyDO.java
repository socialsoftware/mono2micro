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
@Table(name="gray_policy", indexes={@Index(columnList="instanceId")})
public class GrayPolicyDO {
    @Id
    @Column(length=20)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(length=64)
    private String instanceId;
    @Column(length=256, name="alias_name")
    private String alias;
    @Column(length=32)
    private String operator;
    @Column
    private Date operateTime;

    public static GrayPolicyDOBuilder builder() {
        return new GrayPolicyDOBuilder();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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

    public String getInstanceId() {
        return this.instanceId;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getOperator() {
        return this.operator;
    }

    public Date getOperateTime() {
        return this.operateTime;
    }

    public GrayPolicyDO() {
    }

    public GrayPolicyDO(Long id, String instanceId, String alias, String operator, Date operateTime) {
        this.id = id;
        this.instanceId = instanceId;
        this.alias = alias;
        this.operator = operator;
        this.operateTime = operateTime;
    }

    public static class GrayPolicyDOBuilder {
        private Long id;
        private String instanceId;
        private String alias;
        private String operator;
        private Date operateTime;

        GrayPolicyDOBuilder() {
        }

        public GrayPolicyDOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public GrayPolicyDOBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public GrayPolicyDOBuilder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public GrayPolicyDOBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public GrayPolicyDOBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public GrayPolicyDO build() {
            return new GrayPolicyDO(this.id, this.instanceId, this.alias, this.operator, this.operateTime);
        }

        public String toString() {
            return "GrayPolicyDO.GrayPolicyDOBuilder(id=" + this.id + ", instanceId=" + this.instanceId + ", alias=" + this.alias + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ")";
        }
    }

}

