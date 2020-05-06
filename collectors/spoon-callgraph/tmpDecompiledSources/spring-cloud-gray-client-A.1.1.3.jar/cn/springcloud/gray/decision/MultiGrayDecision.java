/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.decision;

import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.decision.GrayDecisionInputArgs;

public class MultiGrayDecision
implements GrayDecision {
    private GrayDecision decision;

    public MultiGrayDecision(GrayDecision decision) {
        this.decision = decision;
    }

    public MultiGrayDecision and(GrayDecision other) {
        GrayDecision cur = this.decision;
        this.decision = t -> cur.test(t) && other.test(t);
        return this;
    }

    @Override
    public boolean test(GrayDecisionInputArgs args) {
        return this.decision.test(args);
    }
}

