/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.DecisionDefinition
 */
package cn.springcloud.gray.decision;

import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.decision.factory.GrayDecisionFactory;
import cn.springcloud.gray.model.DecisionDefinition;

public interface GrayDecisionFactoryKeeper {
    public GrayDecisionFactory getDecisionFactory(String var1);

    public GrayDecision getGrayDecision(DecisionDefinition var1);
}

