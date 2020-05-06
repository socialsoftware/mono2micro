/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModel
 *  io.swagger.annotations.ApiModelProperty
 */
package cn.springcloud.gray.server.module.gray.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

@ApiModel
public class GrayService {
    @ApiModelProperty(value="\u670d\u52a1id")
    private String serviceId;
    @ApiModelProperty(value="\u670d\u52a1\u540d\u79f0")
    private String serviceName;
    @ApiModelProperty(value="\u670d\u52a1\u7684servlet.context-path")
    private String contextPath;
    @ApiModelProperty(value="\u670d\u52a1\u5b9e\u4f8b\u4e2a\u6570")
    private Integer instanceNumber;
    @ApiModelProperty(value="\u7070\u5ea6\u5b9e\u4f8b\u4e2a\u6570")
    private Integer grayInstanceNumber;
    @ApiModelProperty(value="\u670d\u52a1\u63cf\u8ff0")
    private String describe;
    @ApiModelProperty(value="\u64cd\u4f5c\u4eba")
    private String operator;
    @ApiModelProperty(value="\u64cd\u4f5c\u65f6\u95f4")
    private Date operateTime;

    public static GrayServiceBuilder builder() {
        return new GrayServiceBuilder();
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public void setInstanceNumber(Integer instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public void setGrayInstanceNumber(Integer grayInstanceNumber) {
        this.grayInstanceNumber = grayInstanceNumber;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    public Integer getInstanceNumber() {
        return this.instanceNumber;
    }

    public Integer getGrayInstanceNumber() {
        return this.grayInstanceNumber;
    }

    public String getDescribe() {
        return this.describe;
    }

    public String getOperator() {
        return this.operator;
    }

    public Date getOperateTime() {
        return this.operateTime;
    }

    public GrayService(String serviceId, String serviceName, String contextPath, Integer instanceNumber, Integer grayInstanceNumber, String describe, String operator, Date operateTime) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.contextPath = contextPath;
        this.instanceNumber = instanceNumber;
        this.grayInstanceNumber = grayInstanceNumber;
        this.describe = describe;
        this.operator = operator;
        this.operateTime = operateTime;
    }

    public GrayService() {
    }

    public static class GrayServiceBuilder {
        private String serviceId;
        private String serviceName;
        private String contextPath;
        private Integer instanceNumber;
        private Integer grayInstanceNumber;
        private String describe;
        private String operator;
        private Date operateTime;

        GrayServiceBuilder() {
        }

        public GrayServiceBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public GrayServiceBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public GrayServiceBuilder contextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public GrayServiceBuilder instanceNumber(Integer instanceNumber) {
            this.instanceNumber = instanceNumber;
            return this;
        }

        public GrayServiceBuilder grayInstanceNumber(Integer grayInstanceNumber) {
            this.grayInstanceNumber = grayInstanceNumber;
            return this;
        }

        public GrayServiceBuilder describe(String describe) {
            this.describe = describe;
            return this;
        }

        public GrayServiceBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public GrayServiceBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public GrayService build() {
            return new GrayService(this.serviceId, this.serviceName, this.contextPath, this.instanceNumber, this.grayInstanceNumber, this.describe, this.operator, this.operateTime);
        }

        public String toString() {
            return "GrayService.GrayServiceBuilder(serviceId=" + this.serviceId + ", serviceName=" + this.serviceName + ", contextPath=" + this.contextPath + ", instanceNumber=" + this.instanceNumber + ", grayInstanceNumber=" + this.grayInstanceNumber + ", describe=" + this.describe + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ")";
        }
    }

}

