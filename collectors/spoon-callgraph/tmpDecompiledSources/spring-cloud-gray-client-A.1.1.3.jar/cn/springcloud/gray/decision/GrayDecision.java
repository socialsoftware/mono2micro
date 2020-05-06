/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.decision;

import cn.springcloud.gray.decision.GrayDecisionInputArgs;

public interface GrayDecision {
    public boolean test(GrayDecisionInputArgs var1);

    public static AllowGraydecision allow() {
        return AllowGraydecision.INSTANCE;
    }

    public static RefuseGraydecision refuse() {
        return RefuseGraydecision.INSTANCE;
    }

    public static class RefuseGraydecision
    implements GrayDecision {
        private static RefuseGraydecision INSTANCE = new RefuseGraydecision();

        private RefuseGraydecision() {
        }

        @Override
        public boolean test(GrayDecisionInputArgs args) {
            return false;
        }
    }

    public static class AllowGraydecision
    implements GrayDecision {
        private static AllowGraydecision INSTANCE = new AllowGraydecision();

        private AllowGraydecision() {
        }

        @Override
        public boolean test(GrayDecisionInputArgs args) {
            return true;
        }
    }

}

