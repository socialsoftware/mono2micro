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
@Table(name="gray_decision", indexes={@Index(columnList="policyId"), @Index(columnList="instanceId")})
public class GrayDecisionDO {
    @Id
    @Column(length=20)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(length=20)
    private Long policyId;
    @Column(length=64)
    private String instanceId;
    @Column(length=64)
    private String name;
    @Column(length=256)
    private String infos;
    @Column(length=32)
    private String operator;
    @Column
    private Date operateTime;

    public static GrayDecisionDOBuilder builder() {
        return new GrayDecisionDOBuilder();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInfos(String infos) {
        this.infos = infos;
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

    public Long getPolicyId() {
        return this.policyId;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public String getName() {
        return this.name;
    }

    public String getInfos() {
        return this.infos;
    }

    public String getOperator() {
        return this.operator;
    }

    public Date getOperateTime() {
        return this.operateTime;
    }

    public GrayDecisionDO() {
    }

    public GrayDecisionDO(Long id, Long policyId, String instanceId, String name, String infos, String operator, Date operateTime) {
        this.id = id;
        this.policyId = policyId;
        this.instanceId = instanceId;
        this.name = name;
        this.infos = infos;
        this.operator = operator;
        this.operateTime = operateTime;
    }

    public static class GrayDecisionDOBuilder {
        private Long id;
        private Long policyId;
        private String instanceId;
        private String name;
        private String infos;
        private String operator;
        private Date operateTime;

        GrayDecisionDOBuilder() {
        }

        public GrayDecisionDOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public GrayDecisionDOBuilder policyId(Long policyId) {
            this.policyId = policyId;
            return this;
        }

        public GrayDecisionDOBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public GrayDecisionDOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GrayDecisionDOBuilder infos(String infos) {
            this.infos = infos;
            return this;
        }

        public GrayDecisionDOBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public GrayDecisionDOBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public GrayDecisionDO build() {
            return new GrayDecisionDO(this.id, this.policyId, this.instanceId, this.name, this.infos, this.operator, this.operateTime);
        }

        public String toString() {
            return "GrayDecisionDO.GrayDecisionDOBuilder(id=" + this.id + ", policyId=" + this.policyId + ", instanceId=" + this.instanceId + ", name=" + this.name + ", infos=" + this.infos + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ")";
        }
    }

}

