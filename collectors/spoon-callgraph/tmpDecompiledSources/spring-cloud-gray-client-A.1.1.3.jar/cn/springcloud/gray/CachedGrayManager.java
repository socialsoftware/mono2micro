/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.Cache
 *  cn.springcloud.gray.model.DecisionDefinition
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.PolicyDefinition
 */
package cn.springcloud.gray;

import cn.springcloud.gray.Cache;
import cn.springcloud.gray.CacheableGrayManager;
import cn.springcloud.gray.SimpleGrayManager;
import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.decision.GrayDecisionFactoryKeeper;
import cn.springcloud.gray.model.DecisionDefinition;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayService;
import cn.springcloud.gray.model.PolicyDefinition;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CachedGrayManager
extends SimpleGrayManager
implements CacheableGrayManager {
    protected Cache<String, List<GrayDecision>> grayDecisionCache;

    public CachedGrayManager(GrayDecisionFactoryKeeper grayDecisionFactoryKeeper, Cache<String, List<GrayDecision>> grayDecisionCache) {
        super(grayDecisionFactoryKeeper);
        this.grayDecisionCache = grayDecisionCache;
    }

    @Override
    public List<GrayDecision> getGrayDecision(GrayInstance instance) {
        return this.getCacheGrayDecision(instance.getInstanceId(), () -> super.getGrayDecision(instance));
    }

    @Override
    public List<GrayDecision> getGrayDecision(String serviceId, String instanceId) {
        return this.getCacheGrayDecision(instanceId, () -> super.getGrayDecision(this.getGrayInstance(serviceId, instanceId)));
    }

    @Override
    public Cache<String, List<GrayDecision>> getGrayDecisionCache() {
        return this.grayDecisionCache;
    }

    @Override
    public void removeGrayService(String serviceId) {
        GrayService grayService = this.getGrayService(serviceId);
        super.removeGrayService(serviceId);
        if (grayService != null) {
            grayService.getGrayInstances().forEach(grayInstance -> this.invalidateCache(grayInstance.getServiceId(), grayInstance.getInstanceId()));
        }
    }

    @Override
    public void removePolicyDefinition(String serviceId, String instanceId, String policyId) {
        super.removePolicyDefinition(serviceId, instanceId, policyId);
        this.invalidateCache(serviceId, instanceId);
    }

    @Override
    public void updatePolicyDefinition(String serviceId, String instanceId, PolicyDefinition policyDefinition) {
        super.updatePolicyDefinition(serviceId, instanceId, policyDefinition);
        this.invalidateCache(serviceId, instanceId);
    }

    @Override
    public void removeDecisionDefinition(String serviceId, String instanceId, String policyId, String decesionId) {
        super.removeDecisionDefinition(serviceId, instanceId, policyId, decesionId);
        this.invalidateCache(serviceId, instanceId);
    }

    @Override
    public void updateDecisionDefinition(String serviceId, String instanceId, String policyId, DecisionDefinition decisionDefinition) {
        super.updateDecisionDefinition(serviceId, instanceId, policyId, decisionDefinition);
        this.invalidateCache(serviceId, instanceId);
    }

    @Override
    public void updateGrayInstance(GrayInstance instance) {
        super.updateGrayInstance(instance);
        this.invalidateCache(instance.getServiceId(), instance.getInstanceId());
    }

    @Override
    public void closeGray(GrayInstance instance) {
        super.closeGray(instance);
        this.invalidateCache(instance.getServiceId(), instance.getInstanceId());
    }

    @Override
    public void closeGray(String serviceId, String instanceId) {
        super.closeGray(serviceId, instanceId);
        this.invalidateCache(serviceId, instanceId);
    }

    @Override
    public void setGrayServices(Object grayServices) {
        super.setGrayServices(grayServices);
        this.grayDecisionCache.invalidateAll();
    }

    private List<GrayDecision> getCacheGrayDecision(String key, Supplier<List<GrayDecision>> supplier) {
        return (List)this.grayDecisionCache.get((Object)key, k -> (List)supplier.get());
    }

    private void invalidateCache(String serviceId, String instanceId) {
        this.grayDecisionCache.invalidate((Object)instanceId);
    }
}

