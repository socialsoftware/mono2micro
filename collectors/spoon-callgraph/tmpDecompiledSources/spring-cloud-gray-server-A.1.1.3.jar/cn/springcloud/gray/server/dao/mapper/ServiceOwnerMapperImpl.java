/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package cn.springcloud.gray.server.dao.mapper;

import cn.springcloud.gray.server.dao.mapper.ServiceOwnerMapper;
import cn.springcloud.gray.server.dao.model.ServiceOwnerDO;
import cn.springcloud.gray.server.module.user.domain.ServiceOwner;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ServiceOwnerMapperImpl
implements ServiceOwnerMapper {
    @Override
    public ServiceOwnerDO model2do(ServiceOwner d) {
        if (d == null) {
            return null;
        }
        ServiceOwnerDO serviceOwnerDO = new ServiceOwnerDO();
        if (d.getServiceId() != null) {
            serviceOwnerDO.setServiceId(d.getServiceId());
        }
        if (d.getUserId() != null) {
            serviceOwnerDO.setUserId(d.getUserId());
        }
        if (d.getOperator() != null) {
            serviceOwnerDO.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            serviceOwnerDO.setOperateTime(d.getOperateTime());
        }
        return serviceOwnerDO;
    }

    @Override
    public List<ServiceOwnerDO> models2dos(Iterable<ServiceOwner> d) {
        if (d == null) {
            return null;
        }
        ArrayList<ServiceOwnerDO> list = new ArrayList<ServiceOwnerDO>();
        for (ServiceOwner serviceOwner : d) {
            list.add(this.model2do(serviceOwner));
        }
        return list;
    }

    @Override
    public ServiceOwner do2model(ServiceOwnerDO d) {
        if (d == null) {
            return null;
        }
        ServiceOwner serviceOwner = new ServiceOwner();
        if (d.getUserId() != null) {
            serviceOwner.setUserId(d.getUserId());
        }
        if (d.getServiceId() != null) {
            serviceOwner.setServiceId(d.getServiceId());
        }
        if (d.getOperator() != null) {
            serviceOwner.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            serviceOwner.setOperateTime(d.getOperateTime());
        }
        return serviceOwner;
    }

    @Override
    public List<ServiceOwner> dos2models(Iterable<ServiceOwnerDO> d) {
        if (d == null) {
            return null;
        }
        ArrayList<ServiceOwner> list = new ArrayList<ServiceOwner>();
        for (ServiceOwnerDO serviceOwnerDO : d) {
            list.add(this.do2model(serviceOwnerDO));
        }
        return list;
    }

    @Override
    public void do2model(ServiceOwnerDO d, ServiceOwner m) {
        if (d == null) {
            return;
        }
        if (d.getUserId() != null) {
            m.setUserId(d.getUserId());
        }
        if (d.getServiceId() != null) {
            m.setServiceId(d.getServiceId());
        }
        if (d.getOperator() != null) {
            m.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            m.setOperateTime(d.getOperateTime());
        }
    }

    @Override
    public void model2do(ServiceOwner m, ServiceOwnerDO d) {
        if (m == null) {
            return;
        }
        if (m.getServiceId() != null) {
            d.setServiceId(m.getServiceId());
        }
        if (m.getUserId() != null) {
            d.setUserId(m.getUserId());
        }
        if (m.getOperator() != null) {
            d.setOperator(m.getOperator());
        }
        if (m.getOperateTime() != null) {
            d.setOperateTime(m.getOperateTime());
        }
    }
}

