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
@Table(name="gray_track", indexes={@Index(columnList="serviceId"), @Index(columnList="instanceId")})
public class GrayTrackDO {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(length=32)
    private String serviceId;
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

    public static GrayTrackDOBuilder builder() {
        return new GrayTrackDOBuilder();
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

    public GrayTrackDO() {
    }

    public GrayTrackDO(Long id, String serviceId, String instanceId, String name, String infos, String operator, Date operateTime) {
        this.id = id;
        this.serviceId = serviceId;
        this.instanceId = instanceId;
        this.name = name;
        this.infos = infos;
        this.operator = operator;
        this.operateTime = operateTime;
    }

    public static class GrayTrackDOBuilder {
        private Long id;
        private String serviceId;
        private String instanceId;
        private String name;
        private String infos;
        private String operator;
        private Date operateTime;

        GrayTrackDOBuilder() {
        }

        public GrayTrackDOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public GrayTrackDOBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public GrayTrackDOBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public GrayTrackDOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GrayTrackDOBuilder infos(String infos) {
            this.infos = infos;
            return this;
        }

        public GrayTrackDOBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public GrayTrackDOBuilder operateTime(Date operateTime) {
            this.operateTime = operateTime;
            return this;
        }

        public GrayTrackDO build() {
            return new GrayTrackDO(this.id, this.serviceId, this.instanceId, this.name, this.infos, this.operator, this.operateTime);
        }

        public String toString() {
            return "GrayTrackDO.GrayTrackDOBuilder(id=" + this.id + ", serviceId=" + this.serviceId + ", instanceId=" + this.instanceId + ", name=" + this.name + ", infos=" + this.infos + ", operator=" + this.operator + ", operateTime=" + this.operateTime + ")";
        }
    }

}

