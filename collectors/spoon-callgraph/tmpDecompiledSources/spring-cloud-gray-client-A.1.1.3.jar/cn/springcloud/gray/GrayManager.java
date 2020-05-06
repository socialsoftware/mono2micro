/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayInstance
 */
package cn.springcloud.gray;

import cn.springcloud.gray.RequestInterceptor;
import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface GrayManager {
    public boolean hasGray(String var1);

    public Collection<GrayService> allGrayServices();

    public GrayService getGrayService(String var1);

    public GrayInstance getGrayInstance(String var1, String var2);

    public List<GrayDecision> getGrayDecision(GrayInstance var1);

    public List<GrayDecision> getGrayDecision(String var1, String var2);

    public void updateGrayInstance(GrayInstance var1);

    public void closeGray(GrayInstance var1);

    public void closeGray(String var1, String var2);

    public List<RequestInterceptor> getRequeestInterceptors(String var1);

    default public Map<String, Collection<GrayInstance>> getMapByAllGrayServices() {
        return this.allGrayServices().stream().collect(Collectors.toMap(GrayService::getServiceId, GrayService::getGrayInstances));
    }

    public void setup();

    public void shutdown();
}

