/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 */
package cn.springcloud.gray.server.module.audit;

import cn.springcloud.gray.server.module.audit.domain.OperateQuery;
import cn.springcloud.gray.server.module.audit.domain.OperateRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OperateAuditModule {
    public void recordOperate(OperateRecord var1);

    public Page<OperateRecord> queryRecords(OperateQuery var1, Pageable var2);
}

