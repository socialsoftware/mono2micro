/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.choose;

import cn.springcloud.gray.decision.GrayDecisionInputArgs;
import cn.springcloud.gray.servernode.ServerSpec;

public interface GrayPredicate {
    public boolean apply(ServerSpec var1);

    public boolean apply(GrayDecisionInputArgs var1);
}

