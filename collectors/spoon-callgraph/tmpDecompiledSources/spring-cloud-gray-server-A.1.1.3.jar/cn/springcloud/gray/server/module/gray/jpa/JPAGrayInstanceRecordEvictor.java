/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceStatus
 */
package cn.springcloud.gray.server.module.gray.jpa;

import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.server.module.gray.GrayInstanceRecordEvictor;
import cn.springcloud.gray.server.module.gray.domain.GrayInstance;
import cn.springcloud.gray.server.service.GrayInstanceService;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class JPAGrayInstanceRecordEvictor
implements GrayInstanceRecordEvictor {
    private GrayInstanceService grayInstanceService;
    private Set<InstanceStatus> evictionInstanceStatus;
    private int lastUpdateDateExpireDays;

    public JPAGrayInstanceRecordEvictor(GrayInstanceService grayInstanceService, Set<InstanceStatus> evictionInstanceStatus, int lastUpdateDateExpireDays) {
        this.grayInstanceService = grayInstanceService;
        this.evictionInstanceStatus = evictionInstanceStatus;
        this.lastUpdateDateExpireDays = lastUpdateDateExpireDays;
    }

    @Override
    public void evict() {
        List<GrayInstance> grayInstances = this.grayInstanceService.findAllByEvictableRecords(this.lastUpdateDateExpireDays, this.evictionInstanceStatus);
        grayInstances.forEach(grayInstance -> this.grayInstanceService.deleteReactById(grayInstance.getInstanceId()));
    }
}

