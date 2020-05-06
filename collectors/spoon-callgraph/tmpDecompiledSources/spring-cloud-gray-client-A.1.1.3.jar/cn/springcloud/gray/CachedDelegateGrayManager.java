/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.Cache
 *  cn.springcloud.gray.model.GrayInstance
 */
package cn.springcloud.gray;

import cn.springcloud.gray.Cache;
import cn.springcloud.gray.CacheableGrayManager;
import cn.springcloud.gray.CommunicableGrayManager;
import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.GrayManagerDelegater;
import cn.springcloud.gray.UpdateableGrayManager;
import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.model.GrayInstance;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class CachedDelegateGrayManager
extends GrayManagerDelegater
implements CacheableGrayManager,
CommunicableGrayManager {
    private Cache<String, List<GrayDecision>> grayDecisionCache;

    public CachedDelegateGrayManager(GrayManager delegate, Cache<String, List<GrayDecision>> grayDecisionCache) {
        super(delegate);
        this.grayDecisionCache = grayDecisionCache;
    }

    @Override
    public List<GrayDecision> getGrayDecision(GrayInstance instance) {
        return this.getCacheGrayDecision(instance.getInstanceId(), () -> this.delegate.getGrayDecision(instance));
    }

    @Override
    public List<GrayDecision> getGrayDecision(String serviceId, String instanceId) {
        return this.getCacheGrayDecision(instanceId, () -> this.delegate.getGrayDecision(serviceId, instanceId));
    }

    @Override
    public Cache<String, List<GrayDecision>> getGrayDecisionCache() {
        return this.grayDecisionCache;
    }

    @Override
    public void updateGrayInstance(GrayInstance instance) {
        this.delegate.updateGrayInstance(instance);
        this.invalidateCache(instance.getServiceId(), instance.getInstanceId());
    }

    @Override
    public void closeGray(GrayInstance instance) {
        this.delegate.closeGray(instance);
        this.invalidateCache(instance.getServiceId(), instance.getInstanceId());
    }

    @Override
    public void closeGray(String serviceId, String instanceId) {
        this.delegate.closeGray(serviceId, instanceId);
        this.invalidateCache(serviceId, instanceId);
    }

    @Override
    public void setGrayServices(Object grayServices) {
        if (this.delegate instanceof UpdateableGrayManager) {
            ((UpdateableGrayManager)this.delegate).setGrayServices(grayServices);
            this.grayDecisionCache.invalidateAll();
        }
    }

    private List<GrayDecision> getCacheGrayDecision(String key, Supplier<List<GrayDecision>> supplier) {
        return (List)this.grayDecisionCache.get((Object)key, k -> (List)supplier.get());
    }

    private void invalidateCache(String serviceId, String instanceId) {
        this.grayDecisionCache.invalidate((Object)instanceId);
    }
}

