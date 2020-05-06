/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package cn.springcloud.gray.server.dao.mapper;

import cn.springcloud.gray.server.dao.mapper.OperateRecordMapper;
import cn.springcloud.gray.server.dao.model.OperateRecordDO;
import cn.springcloud.gray.server.module.audit.domain.OperateRecord;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OperateRecordMapperImpl
implements OperateRecordMapper {
    @Override
    public OperateRecordDO model2do(OperateRecord d) {
        if (d == null) {
            return null;
        }
        OperateRecordDO operateRecordDO = new OperateRecordDO();
        if (d.getIp() != null) {
            operateRecordDO.setIp(d.getIp());
        }
        if (d.getOperator() != null) {
            operateRecordDO.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            operateRecordDO.setOperateTime(d.getOperateTime());
        }
        if (d.getUri() != null) {
            operateRecordDO.setUri(d.getUri());
        }
        if (d.getHttpMethod() != null) {
            operateRecordDO.setHttpMethod(d.getHttpMethod());
        }
        if (d.getQueryString() != null) {
            operateRecordDO.setQueryString(d.getQueryString());
        }
        if (d.getHandler() != null) {
            operateRecordDO.setHandler(d.getHandler());
        }
        if (d.getHeadlerArgs() != null) {
            operateRecordDO.setHeadlerArgs(d.getHeadlerArgs());
        }
        if (d.getApiResCode() != null) {
            operateRecordDO.setApiResCode(d.getApiResCode());
        }
        operateRecordDO.setOperateState(d.getOperateState());
        return operateRecordDO;
    }

    @Override
    public List<OperateRecordDO> models2dos(Iterable<OperateRecord> d) {
        if (d == null) {
            return null;
        }
        ArrayList<OperateRecordDO> list = new ArrayList<OperateRecordDO>();
        for (OperateRecord operateRecord : d) {
            list.add(this.model2do(operateRecord));
        }
        return list;
    }

    @Override
    public OperateRecord do2model(OperateRecordDO d) {
        if (d == null) {
            return null;
        }
        OperateRecord operateRecord = new OperateRecord();
        if (d.getIp() != null) {
            operateRecord.setIp(d.getIp());
        }
        if (d.getOperator() != null) {
            operateRecord.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            operateRecord.setOperateTime(d.getOperateTime());
        }
        if (d.getUri() != null) {
            operateRecord.setUri(d.getUri());
        }
        if (d.getHttpMethod() != null) {
            operateRecord.setHttpMethod(d.getHttpMethod());
        }
        if (d.getQueryString() != null) {
            operateRecord.setQueryString(d.getQueryString());
        }
        if (d.getHandler() != null) {
            operateRecord.setHandler(d.getHandler());
        }
        if (d.getHeadlerArgs() != null) {
            operateRecord.setHeadlerArgs(d.getHeadlerArgs());
        }
        if (d.getApiResCode() != null) {
            operateRecord.setApiResCode(d.getApiResCode());
        }
        operateRecord.setOperateState(d.getOperateState());
        return operateRecord;
    }

    @Override
    public List<OperateRecord> dos2models(Iterable<OperateRecordDO> d) {
        if (d == null) {
            return null;
        }
        ArrayList<OperateRecord> list = new ArrayList<OperateRecord>();
        for (OperateRecordDO operateRecordDO : d) {
            list.add(this.do2model(operateRecordDO));
        }
        return list;
    }

    @Override
    public void do2model(OperateRecordDO d, OperateRecord m) {
        if (d == null) {
            return;
        }
        if (d.getIp() != null) {
            m.setIp(d.getIp());
        }
        if (d.getOperator() != null) {
            m.setOperator(d.getOperator());
        }
        if (d.getOperateTime() != null) {
            m.setOperateTime(d.getOperateTime());
        }
        if (d.getUri() != null) {
            m.setUri(d.getUri());
        }
        if (d.getHttpMethod() != null) {
            m.setHttpMethod(d.getHttpMethod());
        }
        if (d.getQueryString() != null) {
            m.setQueryString(d.getQueryString());
        }
        if (d.getHandler() != null) {
            m.setHandler(d.getHandler());
        }
        if (d.getHeadlerArgs() != null) {
            m.setHeadlerArgs(d.getHeadlerArgs());
        }
        if (d.getApiResCode() != null) {
            m.setApiResCode(d.getApiResCode());
        }
        m.setOperateState(d.getOperateState());
    }

    @Override
    public void model2do(OperateRecord m, OperateRecordDO d) {
        if (m == null) {
            return;
        }
        if (m.getIp() != null) {
            d.setIp(m.getIp());
        }
        if (m.getOperator() != null) {
            d.setOperator(m.getOperator());
        }
        if (m.getOperateTime() != null) {
            d.setOperateTime(m.getOperateTime());
        }
        if (m.getUri() != null) {
            d.setUri(m.getUri());
        }
        if (m.getHttpMethod() != null) {
            d.setHttpMethod(m.getHttpMethod());
        }
        if (m.getQueryString() != null) {
            d.setQueryString(m.getQueryString());
        }
        if (m.getHandler() != null) {
            d.setHandler(m.getHandler());
        }
        if (m.getHeadlerArgs() != null) {
            d.setHeadlerArgs(m.getHeadlerArgs());
        }
        if (m.getApiResCode() != null) {
            d.setApiResCode(m.getApiResCode());
        }
        d.setOperateState(m.getOperateState());
    }
}

