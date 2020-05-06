/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.DecisionDefinition
 *  cn.springcloud.gray.model.PolicyDefinition
 */
package cn.springcloud.gray;

import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.RequestInterceptor;
import cn.springcloud.gray.model.DecisionDefinition;
import cn.springcloud.gray.model.PolicyDefinition;
import java.util.Collection;

public interface UpdateableGrayManager
extends GrayManager {
    public void setGrayServices(Object var1);

    public void setRequestInterceptors(Collection<RequestInterceptor> var1);

    public void removeGrayService(String var1);

    public void removePolicyDefinition(String var1, String var2, String var3);

    public void updatePolicyDefinition(String var1, String var2, PolicyDefinition var3);

    public void removeDecisionDefinition(String var1, String var2, String var3, String var4);

    public void updateDecisionDefinition(String var1, String var2, String var3, DecisionDefinition var4);
}

