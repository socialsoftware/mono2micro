/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package cn.springcloud.gray.server.dao.mapper;

import cn.springcloud.gray.server.dao.mapper.GrayTrackMapper;
import cn.springcloud.gray.server.dao.model.GrayTrackDO;
import cn.springcloud.gray.server.module.gray.domain.GrayTrack;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GrayTrackMapperImpl
implements GrayTrackMapper {
    @Override
    public GrayTrackDO model2do(GrayTrack d) {
        if (d == null) {
            return null;
        }
        GrayTrackDO grayTrackDO = new GrayTrackDO();
        if (d.getId() != null) {
            grayTrackDO.setId(d.getId());
        }
        if (d.getServiceId() != null) {
            grayTrackDO.setServiceId(d.getServiceId());
        }
        if (d.getInstanceId() != null) {
            grayTrackDO.setInstanceId(d.getInstanceId());
        }
        if (d.getName() != null) {
            grayTrackDO.setName(d.getName());
        }
        if (d.getInfos() != null) {
            grayTrackDO.setInfos(d.getInfos());
        }
        if (d.getOperator() != null) {
            grayTrackDO.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            grayTrackDO.setOperateTime(d.getOperateTime());
        }
        return grayTrackDO;
    }

    @Override
    public List<GrayTrackDO> models2dos(Iterable<GrayTrack> d) {
        if (d == null) {
            return null;
        }
        ArrayList<GrayTrackDO> list = new ArrayList<GrayTrackDO>();
        for (GrayTrack grayTrack : d) {
            list.add(this.model2do(grayTrack));
        }
        return list;
    }

    @Override
    public GrayTrack do2model(GrayTrackDO d) {
        if (d == null) {
            return null;
        }
        GrayTrack grayTrack = new GrayTrack();
        if (d.getId() != null) {
            grayTrack.setId(d.getId());
        }
        if (d.getServiceId() != null) {
            grayTrack.setServiceId(d.getServiceId());
        }
        if (d.getInstanceId() != null) {
            grayTrack.setInstanceId(d.getInstanceId());
        }
        if (d.getName() != null) {
            grayTrack.setName(d.getName());
        }
        if (d.getInfos() != null) {
            grayTrack.setInfos(d.getInfos());
        }
        if (d.getOperator() != null) {
            grayTrack.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            grayTrack.setOperateTime(d.getOperateTime());
        }
        return grayTrack;
    }

    @Override
    public List<GrayTrack> dos2models(Iterable<GrayTrackDO> d) {
        if (d == null) {
            return null;
        }
        ArrayList<GrayTrack> list = new ArrayList<GrayTrack>();
        for (GrayTrackDO grayTrackDO : d) {
            list.add(this.do2model(grayTrackDO));
        }
        return list;
    }

    @Override
    public void do2model(GrayTrackDO d, GrayTrack m) {
        if (d == null) {
            return;
        }
        if (d.getId() != null) {
            m.setId(d.getId());
        }
        if (d.getServiceId() != null) {
            m.setServiceId(d.getServiceId());
        }
        if (d.getInstanceId() != null) {
            m.setInstanceId(d.getInstanceId());
        }
        if (d.getName() != null) {
            m.setName(d.getName());
        }
        if (d.getInfos() != null) {
            m.setInfos(d.getInfos());
        }
        if (d.getOperator() != null) {
            m.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            m.setOperateTime(d.getOperateTime());
        }
    }

    @Override
    public void model2do(GrayTrack m, GrayTrackDO d) {
        if (m == null) {
            return;
        }
        if (m.getId() != null) {
            d.setId(m.getId());
        }
        if (m.getServiceId() != null) {
            d.setServiceId(m.getServiceId());
        }
        if (m.getInstanceId() != null) {
            d.setInstanceId(m.getInstanceId());
        }
        if (m.getName() != null) {
            d.setName(m.getName());
        }
        if (m.getInfos() != null) {
            d.setInfos(m.getInfos());
        }
        if (m.getOperator() != null) {
            d.setOperator(m.getOperator());
        }
        if (m.getOperateTime() != null) {
            d.setOperateTime(m.getOperateTime());
        }
    }
}

