/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package cn.springcloud.gray.server.dao.mapper;

import cn.springcloud.gray.server.dao.mapper.GrayDecisionMapper;
import cn.springcloud.gray.server.dao.model.GrayDecisionDO;
import cn.springcloud.gray.server.module.gray.domain.GrayDecision;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GrayDecisionMapperImpl
implements GrayDecisionMapper {
    @Override
    public GrayDecisionDO model2do(GrayDecision d) {
        if (d == null) {
            return null;
        }
        GrayDecisionDO grayDecisionDO = new GrayDecisionDO();
        if (d.getId() != null) {
            grayDecisionDO.setId(d.getId());
        }
        if (d.getPolicyId() != null) {
            grayDecisionDO.setPolicyId(d.getPolicyId());
        }
        if (d.getInstanceId() != null) {
            grayDecisionDO.setInstanceId(d.getInstanceId());
        }
        if (d.getName() != null) {
            grayDecisionDO.setName(d.getName());
        }
        if (d.getInfos() != null) {
            grayDecisionDO.setInfos(d.getInfos());
        }
        if (d.getOperator() != null) {
            grayDecisionDO.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            grayDecisionDO.setOperateTime(d.getOperateTime());
        }
        return grayDecisionDO;
    }

    @Override
    public List<GrayDecisionDO> models2dos(Iterable<GrayDecision> d) {
        if (d == null) {
            return null;
        }
        ArrayList<GrayDecisionDO> list = new ArrayList<GrayDecisionDO>();
        for (GrayDecision grayDecision : d) {
            list.add(this.model2do(grayDecision));
        }
        return list;
    }

    @Override
    public GrayDecision do2model(GrayDecisionDO d) {
        if (d == null) {
            return null;
        }
        GrayDecision grayDecision = new GrayDecision();
        if (d.getId() != null) {
            grayDecision.setId(d.getId());
        }
        if (d.getInstanceId() != null) {
            grayDecision.setInstanceId(d.getInstanceId());
        }
        if (d.getPolicyId() != null) {
            grayDecision.setPolicyId(d.getPolicyId());
        }
        if (d.getName() != null) {
            grayDecision.setName(d.getName());
        }
        if (d.getInfos() != null) {
            grayDecision.setInfos(d.getInfos());
        }
        if (d.getOperator() != null) {
            grayDecision.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            grayDecision.setOperateTime(d.getOperateTime());
        }
        return grayDecision;
    }

    @Override
    public List<GrayDecision> dos2models(Iterable<GrayDecisionDO> d) {
        if (d == null) {
            return null;
        }
        ArrayList<GrayDecision> list = new ArrayList<GrayDecision>();
        for (GrayDecisionDO grayDecisionDO : d) {
            list.add(this.do2model(grayDecisionDO));
        }
        return list;
    }

    @Override
    public void do2model(GrayDecisionDO d, GrayDecision m) {
        if (d == null) {
            return;
        }
        if (d.getId() != null) {
            m.setId(d.getId());
        }
        if (d.getInstanceId() != null) {
            m.setInstanceId(d.getInstanceId());
        }
        if (d.getPolicyId() != null) {
            m.setPolicyId(d.getPolicyId());
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
    public void model2do(GrayDecision m, GrayDecisionDO d) {
        if (m == null) {
            return;
        }
        if (m.getId() != null) {
            d.setId(m.getId());
        }
        if (m.getPolicyId() != null) {
            d.setPolicyId(m.getPolicyId());
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

