/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModelProperty
 */
package cn.springcloud.gray.server.module.gray.domain;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

public class GrayTrack {
    private Long id;
    private String serviceId;
    private String instanceId;
    private String name;
    private String infos;
    @ApiModelProperty(value="\u64cd\u4f5c\u4eba")
    private String operator;
    @ApiModelProperty(value="\u64cd\u4f5c\u65f6\u95f4")
    private Date operateTime;

    public static GrayTrackBuilder builder() {
        return new GrayTrackBuilder();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
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

    public String getServiceId() {
        return this.serviceId;
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

    public GrayTrack(Long id, String serviceId, String instanceId, String name, String infos, String operator, Date operateTime) {
        this.id = id;
        this.serviceId = serviceId;
        this.instanceId = instanceId;
        this.name = name;
        this.infos = infos;
        this.operator = operator;
        this.operateTime = operateTime;
    }

    public GrayTrack() {
    }

    public static class GrayTrackBuilder {
        private Long id;
        private String serviceId;
        private String instanceId;
        private String name;
        private String infos;
        private String operator;
        private Date operateTime;

        GrayTrackBuilder() {
        }

        public GrayTrackBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public GrayTrackBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public GrayTrackBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public GrayTrackBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GrayTrackBuilder infos(String infos) {
            this.infos = infos;
            return this;
        }

        public GrayTrackBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public GrayTrackBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public GrayTrack build() {
            return new GrayTrack(this.id, this.serviceId, this.instanceId, this.name, this.infos, this.operator, this.operateTime);
        }

        public String toString() {
            return "GrayTrack.GrayTrackBuilder(id=" + this.id + ", serviceId=" + this.serviceId + ", instanceId=" + this.instanceId + ", name=" + this.name + ", infos=" + this.infos + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ")";
        }
    }

}

