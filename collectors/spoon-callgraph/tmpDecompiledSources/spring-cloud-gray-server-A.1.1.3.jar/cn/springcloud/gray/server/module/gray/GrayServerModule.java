/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayStatus
 *  cn.springcloud.gray.model.InstanceStatus
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 */
package cn.springcloud.gray.server.module.gray;

import cn.springcloud.gray.model.GrayStatus;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.server.module.gray.domain.GrayDecision;
import cn.springcloud.gray.server.module.gray.domain.GrayInstance;
import cn.springcloud.gray.server.module.gray.domain.GrayPolicy;
import cn.springcloud.gray.server.module.gray.domain.GrayService;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GrayServerModule {
    public List<GrayService> allGrayServices();

    public List<GrayInstance> listGrayInstancesByServiceId(String var1);

    public List<GrayInstance> listGrayInstancesByServiceId(String var1, Collection<InstanceStatus> var2);

    public List<GrayInstance> listGrayInstancesByStatus(GrayStatus var1, Collection<InstanceStatus> var2);

    default public void closeGray(String instanceId) {
        this.updateGrayStatus(instanceId, GrayStatus.CLOSE);
    }

    public List<GrayInstance> listGrayInstancesByNormalInstanceStatus(Collection<InstanceStatus> var1);

    public void deleteGrayService(String var1);

    default public void instanceShutdown(String instanceId) {
        this.updateInstanceStatus(instanceId, InstanceStatus.DOWN);
    }

    default public void openGray(String instanceId) {
        this.updateGrayStatus(instanceId, GrayStatus.OPEN);
    }

    public void updateGrayStatus(String var1, GrayStatus var2);

    public GrayInstance saveGrayInstance(GrayInstance var1);

    public void updateInstanceStatus(String var1, InstanceStatus var2);

    public void updateInstanceStatus(GrayInstance var1, InstanceStatus var2);

    public void deleteGrayInstance(String var1);

    public GrayPolicy saveGrayPolicy(GrayPolicy var1);

    public void deleteGrayPolicy(Long var1);

    public GrayDecision saveGrayDecision(GrayDecision var1);

    public void deleteGrayDecision(Long var1);

    public GrayDecision getGrayDecision(Long var1);

    public List<GrayDecision> listGrayDecisionsByPolicyId(Long var1);

    public GrayInstance getGrayInstance(String var1);

    public List<GrayService> listAllGrayServices();

    public GrayService saveGrayService(GrayService var1);

    public GrayService getGrayService(String var1);

    default public String getServiceContextPath(String serviceId) {
        GrayService grayService = this.getGrayService(serviceId);
        return Objects.isNull(grayService) ? "" : grayService.getContextPath();
    }

    public List<GrayPolicy> listGrayPoliciesByInstanceId(String var1);

    public Page<GrayService> listAllGrayServices(Pageable var1);

    public List<GrayService> findGrayServices(Iterable<String> var1);

    public Page<GrayPolicy> listGrayPoliciesByInstanceId(String var1, Pageable var2);

    public Page<GrayInstance> listGrayInstancesByServiceId(String var1, Pageable var2);

    public Page<GrayDecision> listGrayDecisionsByPolicyId(Long var1, Pageable var2);
}

