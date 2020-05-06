/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package cn.springcloud.gray.server.dao.mapper;

import cn.springcloud.gray.server.dao.mapper.GrayPolicyMapper;
import cn.springcloud.gray.server.dao.model.GrayPolicyDO;
import cn.springcloud.gray.server.module.gray.domain.GrayPolicy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GrayPolicyMapperImpl
implements GrayPolicyMapper {
    @Override
    public GrayPolicyDO model2do(GrayPolicy d) {
        if (d == null) {
            return null;
        }
        GrayPolicyDO grayPolicyDO = new GrayPolicyDO();
        if (d.getId() != null) {
            grayPolicyDO.setId(d.getId());
        }
        if (d.getInstanceId() != null) {
            grayPolicyDO.setInstanceId(d.getInstanceId());
        }
        if (d.getAlias() != null) {
            grayPolicyDO.setAlias(d.getAlias());
        }
        if (d.getOperator() != null) {
            grayPolicyDO.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            grayPolicyDO.setOperateTime(d.getOperateTime());
        }
        return grayPolicyDO;
    }

    @Override
    public List<GrayPolicyDO> models2dos(Iterable<GrayPolicy> d) {
        if (d == null) {
            return null;
        }
        ArrayList<GrayPolicyDO> list = new ArrayList<GrayPolicyDO>();
        for (GrayPolicy grayPolicy : d) {
            list.add(this.model2do(grayPolicy));
        }
        return list;
    }

    @Override
    public GrayPolicy do2model(GrayPolicyDO d) {
        if (d == null) {
            return null;
        }
        GrayPolicy grayPolicy = new GrayPolicy();
        if (d.getId() != null) {
            grayPolicy.setId(d.getId());
        }
        if (d.getInstanceId() != null) {
            grayPolicy.setInstanceId(d.getInstanceId());
        }
        if (d.getAlias() != null) {
            grayPolicy.setAlias(d.getAlias());
        }
        if (d.getOperator() != null) {
            grayPolicy.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            grayPolicy.setOperateTime(d.getOperateTime());
        }
        return grayPolicy;
    }

    @Override
    public List<GrayPolicy> dos2models(Iterable<GrayPolicyDO> d) {
        if (d == null) {
            return null;
        }
        ArrayList<GrayPolicy> list = new ArrayList<GrayPolicy>();
        for (GrayPolicyDO grayPolicyDO : d) {
            list.add(this.do2model(grayPolicyDO));
        }
        return list;
    }

    @Override
    public void do2model(GrayPolicyDO d, GrayPolicy m) {
        if (d == null) {
            return;
        }
        if (d.getId() != null) {
            m.setId(d.getId());
        }
        if (d.getInstanceId() != null) {
            m.setInstanceId(d.getInstanceId());
        }
        if (d.getAlias() != null) {
            m.setAlias(d.getAlias());
        }
        if (d.getOperator() != null) {
            m.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            m.setOperateTime(d.getOperateTime());
        }
    }

    @Override
    public void model2do(GrayPolicy m, GrayPolicyDO d) {
        if (m == null) {
            return;
        }
        if (m.getId() != null) {
            d.setId(m.getId());
        }
        if (m.getInstanceId() != null) {
            d.setInstanceId(m.getInstanceId());
        }
        if (m.getAlias() != null) {
            d.setAlias(m.getAlias());
        }
        if (m.getOperator() != null) {
            d.setOperator(m.getOperator());
        }
        if (m.getOperateTime() != null) {
            d.setOperateTime(m.getOperateTime());
        }
    }
}

