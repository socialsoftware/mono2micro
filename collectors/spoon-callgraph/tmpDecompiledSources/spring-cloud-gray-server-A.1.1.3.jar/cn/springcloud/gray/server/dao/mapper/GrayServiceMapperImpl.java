/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package cn.springcloud.gray.server.dao.mapper;

import cn.springcloud.gray.server.dao.mapper.GrayServiceMapper;
import cn.springcloud.gray.server.dao.model.GrayServiceDO;
import cn.springcloud.gray.server.module.gray.domain.GrayService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GrayServiceMapperImpl
implements GrayServiceMapper {
    @Override
    public GrayServiceDO model2do(GrayService d) {
        if (d == null) {
            return null;
        }
        GrayServiceDO grayServiceDO = new GrayServiceDO();
        if (d.getServiceId() != null) {
            grayServiceDO.setServiceId(d.getServiceId());
        }
        if (d.getServiceName() != null) {
            grayServiceDO.setServiceName(d.getServiceName());
        }
        if (d.getContextPath() != null) {
            grayServiceDO.setContextPath(d.getContextPath());
        }
        if (d.getInstanceNumber() != null) {
            grayServiceDO.setInstanceNumber(d.getInstanceNumber());
        }
        if (d.getGrayInstanceNumber() != null) {
            grayServiceDO.setGrayInstanceNumber(d.getGrayInstanceNumber());
        }
        if (d.getDescribe() != null) {
            grayServiceDO.setDescribe(d.getDescribe());
        }
        if (d.getOperator() != null) {
            grayServiceDO.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            grayServiceDO.setOperateTime(d.getOperateTime());
        }
        return grayServiceDO;
    }

    @Override
    public List<GrayServiceDO> models2dos(Iterable<GrayService> d) {
        if (d == null) {
            return null;
        }
        ArrayList<GrayServiceDO> list = new ArrayList<GrayServiceDO>();
        for (GrayService grayService : d) {
            list.add(this.model2do(grayService));
        }
        return list;
    }

    @Override
    public GrayService do2model(GrayServiceDO d) {
        if (d == null) {
            return null;
        }
        GrayService grayService = new GrayService();
        if (d.getServiceId() != null) {
            grayService.setServiceId(d.getServiceId());
        }
        if (d.getServiceName() != null) {
            grayService.setServiceName(d.getServiceName());
        }
        if (d.getContextPath() != null) {
            grayService.setContextPath(d.getContextPath());
        }
        if (d.getInstanceNumber() != null) {
            grayService.setInstanceNumber(d.getInstanceNumber());
        }
        if (d.getGrayInstanceNumber() != null) {
            grayService.setGrayInstanceNumber(d.getGrayInstanceNumber());
        }
        if (d.getDescribe() != null) {
            grayService.setDescribe(d.getDescribe());
        }
        if (d.getOperator() != null) {
            grayService.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            grayService.setOperateTime(d.getOperateTime());
        }
        return grayService;
    }

    @Override
    public List<GrayService> dos2models(Iterable<GrayServiceDO> d) {
        if (d == null) {
            return null;
        }
        ArrayList<GrayService> list = new ArrayList<GrayService>();
        for (GrayServiceDO grayServiceDO : d) {
            list.add(this.do2model(grayServiceDO));
        }
        return list;
    }

    @Override
    public void do2model(GrayServiceDO d, GrayService m) {
        if (d == null) {
            return;
        }
        if (d.getServiceId() != null) {
            m.setServiceId(d.getServiceId());
        }
        if (d.getServiceName() != null) {
            m.setServiceName(d.getServiceName());
        }
        if (d.getContextPath() != null) {
            m.setContextPath(d.getContextPath());
        }
        if (d.getInstanceNumber() != null) {
            m.setInstanceNumber(d.getInstanceNumber());
        }
        if (d.getGrayInstanceNumber() != null) {
            m.setGrayInstanceNumber(d.getGrayInstanceNumber());
        }
        if (d.getDescribe() != null) {
            m.setDescribe(d.getDescribe());
        }
        if (d.getOperator() != null) {
            m.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            m.setOperateTime(d.getOperateTime());
        }
    }

    @Override
    public void model2do(GrayService m, GrayServiceDO d) {
        if (m == null) {
            return;
        }
        if (m.getServiceId() != null) {
            d.setServiceId(m.getServiceId());
        }
        if (m.getServiceName() != null) {
            d.setServiceName(m.getServiceName());
        }
        if (m.getContextPath() != null) {
            d.setContextPath(m.getContextPath());
        }
        if (m.getInstanceNumber() != null) {
            d.setInstanceNumber(m.getInstanceNumber());
        }
        if (m.getGrayInstanceNumber() != null) {
            d.setGrayInstanceNumber(m.getGrayInstanceNumber());
        }
        if (m.getDescribe() != null) {
            d.setDescribe(m.getDescribe());
        }
        if (m.getOperator() != null) {
            d.setOperator(m.getOperator());
        }
        if (m.getOperateTime() != null) {
            d.setOperateTime(m.getOperateTime());
        }
    }
}

