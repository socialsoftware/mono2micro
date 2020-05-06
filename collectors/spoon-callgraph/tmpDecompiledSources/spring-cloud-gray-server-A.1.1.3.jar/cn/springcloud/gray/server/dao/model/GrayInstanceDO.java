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
@Table(name="gray_instance", indexes={@Index(columnList="serviceId")})
public class GrayInstanceDO {
    @Id
    @Column(length=64)
    private String instanceId;
    @Column(length=32)
    private String serviceId;
    @Column(length=32)
    private String host;
    @Column(length=5)
    private Integer port;
    @Column(length=128)
    private String des;
    @Column
    private Date lastUpdateDate;
    @Column(length=32)
    private String operator;
    @Column
    private Date operateTime;
    @Column(length=16)
    private String instanceStatus;
    @Column(length=16)
    private String grayStatus;
    @Column(length=2)
    private Integer grayLock;

    public static GrayInstanceDOBuilder builder() {
        return new GrayInstanceDOBuilder();
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

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public void setInstanceStatus(String instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    public void setGrayStatus(String grayStatus) {
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

    public String getOperator() {
        return this.operator;
    }

    public Date getOperateTime() {
        return this.operateTime;
    }

    public String getInstanceStatus() {
        return this.instanceStatus;
    }

    public String getGrayStatus() {
        return this.grayStatus;
    }

    public Integer getGrayLock() {
        return this.grayLock;
    }

    public GrayInstanceDO() {
    }

    public GrayInstanceDO(String instanceId, String serviceId, String host, Integer port, String des, Date lastUpdateDate, String operator, Date operateTime, String instanceStatus, String grayStatus, Integer grayLock) {
        this.instanceId = instanceId;
        this.serviceId = serviceId;
        this.host = host;
        this.port = port;
        this.des = des;
        this.lastUpdateDate = lastUpdateDate;
        this.operator = operator;
        this.operateTime = operateTime;
        this.instanceStatus = instanceStatus;
        this.grayStatus = grayStatus;
        this.grayLock = grayLock;
    }

    public static class GrayInstanceDOBuilder {
        private String instanceId;
        private String serviceId;
        private String host;
        private Integer port;
        private String des;
        private Date lastUpdateDate;
        private String operator;
        private Date operateTime;
        private String instanceStatus;
        private String grayStatus;
        private Integer grayLock;

        GrayInstanceDOBuilder() {
        }

        public GrayInstanceDOBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public GrayInstanceDOBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public GrayInstanceDOBuilder host(String host) {
            this.host = host;
            return this;
        }

        public GrayInstanceDOBuilder port(Integer port) {
            this.port = port;
            return this;
        }

        public GrayInstanceDOBuilder des(String des) {
            this.des = des;
            return this;
        }

        public GrayInstanceDOBuilder lastUpdateDate(Date lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        public GrayInstanceDOBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public GrayInstanceDOBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public GrayInstanceDOBuilder instanceStatus(String instanceStatus) {
            this.instanceStatus = instanceStatus;
            return this;
        }

        public GrayInstanceDOBuilder grayStatus(String grayStatus) {
            this.grayStatus = grayStatus;
            return this;
        }

        public GrayInstanceDOBuilder grayLock(Integer grayLock) {
            this.grayLock = grayLock;
            return this;
        }

        public GrayInstanceDO build() {
            return new GrayInstanceDO(this.instanceId, this.serviceId, this.host, this.port, this.des, this.lastUpdateDate, this.operator, this.operateTime, this.instanceStatus, this.grayStatus, this.grayLock);
        }

        public String toString() {
            return "GrayInstanceDO.GrayInstanceDOBuilder(instanceId=" + this.instanceId + ", serviceId=" + this.serviceId + ", host=" + this.host + ", port=" + this.port + ", des=" + this.des + ", lastUpdateDate=" + this.lastUpdateDate + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ", instanceStatus=" + this.instanceStatus + ", grayStatus=" + this.grayStatus + ", grayLock=" + this.grayLock + ")";
        }
    }

}

