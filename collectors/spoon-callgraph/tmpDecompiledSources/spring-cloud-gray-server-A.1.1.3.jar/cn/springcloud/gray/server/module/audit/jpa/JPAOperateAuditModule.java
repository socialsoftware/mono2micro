/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 */
package cn.springcloud.gray.server.module.audit.jpa;

import cn.springcloud.gray.server.module.audit.OperateAuditModule;
import cn.springcloud.gray.server.module.audit.domain.OperateQuery;
import cn.springcloud.gray.server.module.audit.domain.OperateRecord;
import cn.springcloud.gray.server.service.OperateRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class JPAOperateAuditModule
implements OperateAuditModule {
    private OperateRecordService operateRecordService;

    public JPAOperateAuditModule(OperateRecordService operateRecordService) {
        this.operateRecordService = operateRecordService;
    }

    @Override
    public void recordOperate(OperateRecord record) {
        this.operateRecordService.saveModel(record);
    }

    @Override
    public Page<OperateRecord> queryRecords(OperateQuery query, Pageable pageable) {
        return this.operateRecordService.query(query, pageable);
    }
}

