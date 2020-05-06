/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModelProperty
 */
package cn.springcloud.gray.server.module.gray.domain;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

public class GrayDecision {
    private Long id;
    @ApiModelProperty(value="\u5b9e\u4f8bid")
    private String instanceId;
    @ApiModelProperty(value="\u7b56\u7565id")
    private Long policyId;
    @ApiModelProperty(value="\u7070\u5ea6\u51b3\u7b56\u540d\u79f0")
    private String name;
    @ApiModelProperty(value="\u51b3\u7b56\u53c2\u6570")
    private String infos;
    @ApiModelProperty(value="\u64cd\u4f5c\u4eba")
    private String operator;
    @ApiModelProperty(value="\u64cd\u4f5c\u65f6\u95f4")
    private Date operateTime;

    public static GrayDecisionBuilder builder() {
        return new GrayDecisionBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public Long getPolicyId() {
        return this.policyId;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
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

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GrayDecision)) {
            return false;
        }
        GrayDecision other = (GrayDecision)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        String this$instanceId = this.getInstanceId();
        String other$instanceId = other.getInstanceId();
        if (this$instanceId == null ? other$instanceId != null : !this$instanceId.equals(other$instanceId)) {
            return false;
        }
        Long this$policyId = this.getPolicyId();
        Long other$policyId = other.getPolicyId();
        if (this$policyId == null ? other$policyId != null : !((Object)this$policyId).equals(other$policyId)) {
            return false;
        }
        String this$name = this.getName();
        String other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        String this$infos = this.getInfos();
        String other$infos = other.getInfos();
        if (this$infos == null ? other$infos != null : !this$infos.equals(other$infos)) {
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
        return other instanceof GrayDecision;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        String $instanceId = this.getInstanceId();
        result = result * 59 + ($instanceId == null ? 43 : $instanceId.hashCode());
        Long $policyId = this.getPolicyId();
        result = result * 59 + ($policyId == null ? 43 : ((Object)$policyId).hashCode());
        String $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        String $infos = this.getInfos();
        result = result * 59 + ($infos == null ? 43 : $infos.hashCode());
        String $operator = this.getOperator();
        result = result * 59 + ($operator == null ? 43 : $operator.hashCode());
        Date $operateTime = this.getOperateTime();
        result = result * 59 + ($operateTime == null ? 43 : ((Object)$operateTime).hashCode());
        return result;
    }

    public String toString() {
        return "GrayDecision(id=" + this.getId() + ", instanceId=" + this.getInstanceId() + ", policyId=" + this.getPolicyId() + ", name=" + this.getName() + ", infos=" + this.getInfos() + ", operator=" + this.getOperator() + ", operateTime=" + this.getOperateTime() + ")";
    }

    public GrayDecision(Long id, String instanceId, Long policyId, String name, String infos, String operator, Date operateTime) {
        this.id = id;
        this.instanceId = instanceId;
        this.policyId = policyId;
        this.name = name;
        this.infos = infos;
        this.operator = operator;
        this.operateTime = operateTime;
    }

    public GrayDecision() {
    }

    public static class GrayDecisionBuilder {
        private Long id;
        private String instanceId;
        private Long policyId;
        private String name;
        private String infos;
        private String operator;
        private Date operateTime;

        GrayDecisionBuilder() {
        }

        public GrayDecisionBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public GrayDecisionBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public GrayDecisionBuilder policyId(Long policyId) {
            this.policyId = policyId;
            return this;
        }

        public GrayDecisionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GrayDecisionBuilder infos(String infos) {
            this.infos = infos;
            return this;
        }

        public GrayDecisionBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public GrayDecisionBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public GrayDecision build() {
            return new GrayDecision(this.id, this.instanceId, this.policyId, this.name, this.infos, this.operator, this.operateTime);
        }

        public String toString() {
            return "GrayDecision.GrayDecisionBuilder(id=" + this.id + ", instanceId=" + this.instanceId + ", policyId=" + this.policyId + ", name=" + this.name + ", infos=" + this.infos + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ")";
        }
    }

}

