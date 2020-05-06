/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayStatus
 *  cn.springcloud.gray.model.InstanceStatus
 *  io.swagger.annotations.ApiModel
 *  io.swagger.annotations.ApiModelProperty
 */
package cn.springcloud.gray.server.module.gray.domain;

import cn.springcloud.gray.model.GrayStatus;
import cn.springcloud.gray.model.InstanceStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

@ApiModel(value="\u5b9e\u4f8b\u7684\u7070\u5ea6\u4fe1\u606f")
public class GrayInstance {
    public static final int GRAY_LOCKED = 1;
    public static final int GRAY_UNLOCKED = 0;
    @ApiModelProperty(value="\u670d\u52a1\u5b9e\u4f8bid")
    private String instanceId;
    @ApiModelProperty(value="\u670d\u52a1id")
    private String serviceId;
    private String host;
    @ApiModelProperty(value="\u670d\u52a1\u5b9e\u4f8b\u7aef\u53e3")
    private Integer port;
    @ApiModelProperty(value="\u63cf\u8ff0")
    private String des;
    @ApiModelProperty(value="\u6700\u540e\u66f4\u65b0\u65f6\u95f4")
    private Date lastUpdateDate;
    @ApiModelProperty(value="\u64cd\u4f5c\u65f6\u95f4")
    private Date operateTime;
    @ApiModelProperty(value="\u64cd\u4f5c\u4eba")
    private String operator;
    @ApiModelProperty(value="\u670d\u52a1\u5b9e\u4f8b\u72b6\u6001")
    private InstanceStatus instanceStatus;
    @ApiModelProperty(value="\u7070\u5ea6\u72b6\u6001")
    private GrayStatus grayStatus;
    @ApiModelProperty(value="\u7070\u5ea6\u72b6\u6001")
    private Integer grayLock = 0;

    public static GrayInstanceBuilder builder() {
        return new GrayInstanceBuilder();
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setInstanceStatus(InstanceStatus instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    public void setGrayStatus(GrayStatus grayStatus) {
        this.grayStatus = grayStatus;
    }

    public void setGrayLock(Integer grayLock) {
        this.grayLock = grayLock;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public String getHost() {
        return this.host;
    }

    public Integer getPort() {
        return this.port;
    }

    public String getDes() {
        return this.des;
    }

    public Date getLastUpdateDate() {
        return this.lastUpdateDate;
    }

    public Date getOperateTime() {
        return this.operateTime;
    }

    public String getOperator() {
        return this.operator;
    }

    public InstanceStatus getInstanceStatus() {
        return this.instanceStatus;
    }

    public GrayStatus getGrayStatus() {
        return this.grayStatus;
    }

    public Integer getGrayLock() {
        return this.grayLock;
    }

    public GrayInstance(String instanceId, String serviceId, String host, Integer port, String des, Date lastUpdateDate, Date operateTime, String operator, InstanceStatus instanceStatus, GrayStatus grayStatus, Integer grayLock) {
        this.instanceId = instanceId;
        this.serviceId = serviceId;
        this.host = host;
        this.port = port;
        this.des = des;
        this.lastUpdateDate = lastUpdateDate;
        this.operateTime = operateTime;
        this.operator = operator;
        this.instanceStatus = instanceStatus;
        this.grayStatus = grayStatus;
        this.grayLock = grayLock;
    }

    public GrayInstance() {
    }

    public static class GrayInstanceBuilder {
        private String instanceId;
        private String serviceId;
        private String host;
        private Integer port;
        private String des;
        private Date lastUpdateDate;
        private Date operateTime;
        private String operator;
        private InstanceStatus instanceStatus;
        private GrayStatus grayStatus;
        private Integer grayLock;

        GrayInstanceBuilder() {
        }

        public GrayInstanceBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public GrayInstanceBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public GrayInstanceBuilder host(String host) {
            this.host = host;
            return this;
        }

        public GrayInstanceBuilder port(Integer port) {
            this.port = port;
            return this;
        }

        public GrayInstanceBuilder des(String des) {
            this.des = des;
            return this;
        }

        public GrayInstanceBuilder lastUpdateDate(Date lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        public GrayInstanceBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public GrayInstanceBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public GrayInstanceBuilder instanceStatus(InstanceStatus instanceStatus) {
            this.instanceStatus = instanceStatus;
            return this;
        }

        public GrayInstanceBuilder grayStatus(GrayStatus grayStatus) {
            this.grayStatus = grayStatus;
            return this;
        }

        public GrayInstanceBuilder grayLock(Integer grayLock) {
            this.grayLock = grayLock;
            return this;
        }

        public GrayInstance build() {
            return new GrayInstance(this.instanceId, this.serviceId, this.host, this.port, this.des, this.lastUpdateDate, this.operateTime, this.operator, this.instanceStatus, this.grayStatus, this.grayLock);
        }

        public String toString() {
            return "GrayInstance.GrayInstanceBuilder(instanceId=" + this.instanceId + ", serviceId=" + this.serviceId + ", host=" + this.host + ", port=" + this.port + ", des=" + this.des + ", lastUpdateDate=" + this.lastUpdateDate + ", operateTime=" + this.operateTime + ", operator=" + this.operator + ", instanceStatus=" + (Object)this.instanceStatus + ", grayStatus=" + (Object)this.grayStatus + ", grayLock=" + this.grayLock + ")";
        }
    }

}

