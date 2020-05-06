/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.dao.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="gray_service")
public class GrayServiceDO {
    @Id
    @Column(length=32)
    private String serviceId;
    @Column(length=64)
    private String serviceName;
    @Column(length=64)
    private String contextPath;
    @Column(length=4)
    private Integer instanceNumber;
    @Column(length=4)
    private Integer grayInstanceNumber;
    @Column(length=256, name="des")
    private String describe;
    @Column(length=32)
    private String operator;
    @Column
    private Date operateTime;

    public static GrayServiceDOBuilder builder() {
        return new GrayServiceDOBuilder();
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

    public GrayServiceDO() {
    }

    public GrayServiceDO(String serviceId, String serviceName, String contextPath, Integer instanceNumber, Integer grayInstanceNumber, String describe, String operator, Date operateTime) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.contextPath = contextPath;
        this.instanceNumber = instanceNumber;
        this.grayInstanceNumber = grayInstanceNumber;
        this.describe = describe;
        this.operator = operator;
        this.operateTime = operateTime;
    }

    public static class GrayServiceDOBuilder {
        private String serviceId;
        private String serviceName;
        private String contextPath;
        private Integer instanceNumber;
        private Integer grayInstanceNumber;
        private String describe;
        private String operator;
        private Date operateTime;

        GrayServiceDOBuilder() {
        }

        public GrayServiceDOBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public GrayServiceDOBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public GrayServiceDOBuilder contextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public GrayServiceDOBuilder instanceNumber(Integer instanceNumber) {
            this.instanceNumber = instanceNumber;
            return this;
        }

        public GrayServiceDOBuilder grayInstanceNumber(Integer grayInstanceNumber) {
            this.grayInstanceNumber = grayInstanceNumber;
            return this;
        }

        public GrayServiceDOBuilder describe(String describe) {
            this.describe = describe;
            return this;
        }

        public GrayServiceDOBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public GrayServiceDOBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public GrayServiceDO build() {
            return new GrayServiceDO(this.serviceId, this.serviceName, this.contextPath, this.instanceNumber, this.grayInstanceNumber, this.describe, this.operator, this.operateTime);
        }

        public String toString() {
            return "GrayServiceDO.GrayServiceDOBuilder(serviceId=" + this.serviceId + ", serviceName=" + this.serviceName + ", contextPath=" + this.contextPath + ", instanceNumber=" + this.instanceNumber + ", grayInstanceNumber=" + this.grayInstanceNumber + ", describe=" + this.describe + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ")";
        }
    }

}

