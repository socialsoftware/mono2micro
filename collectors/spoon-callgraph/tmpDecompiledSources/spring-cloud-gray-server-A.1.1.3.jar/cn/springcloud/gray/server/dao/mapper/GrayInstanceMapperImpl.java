/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayStatus
 *  cn.springcloud.gray.model.InstanceStatus
 *  org.springframework.stereotype.Component
 */
package cn.springcloud.gray.server.dao.mapper;

import cn.springcloud.gray.model.GrayStatus;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.server.dao.mapper.GrayInstanceMapper;
import cn.springcloud.gray.server.dao.model.GrayInstanceDO;
import cn.springcloud.gray.server.module.gray.domain.GrayInstance;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GrayInstanceMapperImpl
implements GrayInstanceMapper {
    @Override
    public GrayInstanceDO model2do(GrayInstance d) {
        if (d == null) {
            return null;
        }
        GrayInstanceDO grayInstanceDO = new GrayInstanceDO();
        if (d.getInstanceId() != null) {
            grayInstanceDO.setInstanceId(d.getInstanceId());
        }
        if (d.getServiceId() != null) {
            grayInstanceDO.setServiceId(d.getServiceId());
        }
        if (d.getHost() != null) {
            grayInstanceDO.setHost(d.getHost());
        }
        if (d.getPort() != null) {
            grayInstanceDO.setPort(d.getPort());
        }
        if (d.getDes() != null) {
            grayInstanceDO.setDes(d.getDes());
        }
        if (d.getLastUpdateDate() != null) {
            grayInstanceDO.setLastUpdateDate(d.getLastUpdateDate());
        }
        if (d.getOperator() != null) {
            grayInstanceDO.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            grayInstanceDO.setOperateTime(d.getOperateTime());
        }
        if (d.getInstanceStatus() != null) {
            grayInstanceDO.setInstanceStatus(d.getInstanceStatus().name());
        }
        if (d.getGrayStatus() != null) {
            grayInstanceDO.setGrayStatus(d.getGrayStatus().name());
        }
        if (d.getGrayLock() != null) {
            grayInstanceDO.setGrayLock(d.getGrayLock());
        }
        return grayInstanceDO;
    }

    @Override
    public List<GrayInstanceDO> models2dos(Iterable<GrayInstance> d) {
        if (d == null) {
            return null;
        }
        ArrayList<GrayInstanceDO> list = new ArrayList<GrayInstanceDO>();
        for (GrayInstance grayInstance : d) {
            list.add(this.model2do(grayInstance));
        }
        return list;
    }

    @Override
    public GrayInstance do2model(GrayInstanceDO d) {
        if (d == null) {
            return null;
        }
        GrayInstance grayInstance = new GrayInstance();
        if (d.getInstanceId() != null) {
            grayInstance.setInstanceId(d.getInstanceId());
        }
        if (d.getServiceId() != null) {
            grayInstance.setServiceId(d.getServiceId());
        }
        if (d.getHost() != null) {
            grayInstance.setHost(d.getHost());
        }
        if (d.getPort() != null) {
            grayInstance.setPort(d.getPort());
        }
        if (d.getDes() != null) {
            grayInstance.setDes(d.getDes());
        }
        if (d.getLastUpdateDate() != null) {
            grayInstance.setLastUpdateDate(d.getLastUpdateDate());
        }
        if (d.getOperateTime() != null) {
            grayInstance.setOperateTime(d.getOperateTime());
        }
        if (d.getOperator() != null) {
            grayInstance.setOperator(d.getOperator());
        }
        if (d.getInstanceStatus() != null) {
            grayInstance.setInstanceStatus(Enum.valueOf(InstanceStatus.class, d.getInstanceStatus()));
        }
        if (d.getGrayStatus() != null) {
            grayInstance.setGrayStatus(Enum.valueOf(GrayStatus.class, d.getGrayStatus()));
        }
        if (d.getGrayLock() != null) {
            grayInstance.setGrayLock(d.getGrayLock());
        }
        return grayInstance;
    }

    @Override
    public List<GrayInstance> dos2models(Iterable<GrayInstanceDO> d) {
        if (d == null) {
            return null;
        }
        ArrayList<GrayInstance> list = new ArrayList<GrayInstance>();
        for (GrayInstanceDO grayInstanceDO : d) {
            list.add(this.do2model(grayInstanceDO));
        }
        return list;
    }

    @Override
    public void do2model(GrayInstanceDO d, GrayInstance m) {
        if (d == null) {
            return;
        }
        if (d.getInstanceId() != null) {
            m.setInstanceId(d.getInstanceId());
        }
        if (d.getServiceId() != null) {
            m.setServiceId(d.getServiceId());
        }
        if (d.getHost() != null) {
            m.setHost(d.getHost());
        }
        if (d.getPort() != null) {
            m.setPort(d.getPort());
        }
        if (d.getDes() != null) {
            m.setDes(d.getDes());
        }
        if (d.getLastUpdateDate() != null) {
            m.setLastUpdateDate(d.getLastUpdateDate());
        }
        if (d.getOperateTime() != null) {
            m.setOperateTime(d.getOperateTime());
        }
        if (d.getOperator() != null) {
            m.setOperator(d.getOperator());
        }
        if (d.getInstanceStatus() != null) {
            m.setInstanceStatus(Enum.valueOf(InstanceStatus.class, d.getInstanceStatus()));
        }
        if (d.getGrayStatus() != null) {
            m.setGrayStatus(Enum.valueOf(GrayStatus.class, d.getGrayStatus()));
        }
        if (d.getGrayLock() != null) {
            m.setGrayLock(d.getGrayLock());
        }
    }

    @Override
    public void model2do(GrayInstance m, GrayInstanceDO d) {
        if (m == null) {
            return;
        }
        if (m.getInstanceId() != null) {
            d.setInstanceId(m.getInstanceId());
        }
        if (m.getServiceId() != null) {
            d.setServiceId(m.getServiceId());
        }
        if (m.getHost() != null) {
            d.setHost(m.getHost());
        }
        if (m.getPort() != null) {
            d.setPort(m.getPort());
        }
        if (m.getDes() != null) {
            d.setDes(m.getDes());
        }
        if (m.getLastUpdateDate() != null) {
            d.setLastUpdateDate(m.getLastUpdateDate());
        }
        if (m.getOperator() != null) {
            d.setOperator(m.getOperator());
        }
        if (m.getOperateTime() != null) {
            d.setOperateTime(m.getOperateTime());
        }
        if (m.getInstanceStatus() != null) {
            d.setInstanceStatus(m.getInstanceStatus().name());
        }
        if (m.getGrayStatus() != null) {
            d.setGrayStatus(m.getGrayStatus().name());
        }
        if (m.getGrayLock() != null) {
            d.setGrayLock(m.getGrayLock());
        }
    }
}

