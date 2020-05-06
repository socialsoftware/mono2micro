/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModelProperty
 */
package cn.springcloud.gray.server.module.gray.domain;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

public class GrayPolicy {
    private Long id;
    @ApiModelProperty(value="\u670d\u52a1\u5b9e\u4f8bid")
    private String instanceId;
    @ApiModelProperty(value="\u7b56\u7565\u522b\u540d")
    private String alias;
    @ApiModelProperty(value="\u64cd\u4f5c\u4eba")
    private String operator;
    @ApiModelProperty(value="\u64cd\u4f5c\u65f6\u95f4")
    private Date operateTime;

    public static GrayPolicyBuilder builder() {
        return new GrayPolicyBuilder();
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

    public GrayPolicy(Long id, String instanceId, String alias, String operator, Date operateTime) {
        this.id = id;
        this.instanceId = instanceId;
        this.alias = alias;
        this.operator = operator;
        this.operateTime = operateTime;
    }

    public GrayPolicy() {
    }

    public static class GrayPolicyBuilder {
        private Long id;
        private String instanceId;
        private String alias;
        private String operator;
        private Date operateTime;

        GrayPolicyBuilder() {
        }

        public GrayPolicyBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public GrayPolicyBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public GrayPolicyBuilder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public GrayPolicyBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public GrayPolicyBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public GrayPolicy build() {
            return new GrayPolicy(this.id, this.instanceId, this.alias, this.operator, this.operateTime);
        }

        public String toString() {
            return "GrayPolicy.GrayPolicyBuilder(id=" + this.id + ", instanceId=" + this.instanceId + ", alias=" + this.alias + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ")";
        }
    }

}

