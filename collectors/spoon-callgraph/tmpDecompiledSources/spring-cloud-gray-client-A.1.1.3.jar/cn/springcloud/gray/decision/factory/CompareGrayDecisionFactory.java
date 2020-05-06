/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.decision.factory;

import cn.springcloud.gray.decision.compare.CompareMode;
import cn.springcloud.gray.decision.factory.AbstractGrayDecisionFactory;

public abstract class CompareGrayDecisionFactory<C extends CompareConfig>
extends AbstractGrayDecisionFactory<C> {
    protected CompareGrayDecisionFactory(Class<C> configClass) {
        super(configClass);
    }

    public static class CompareConfig {
        private CompareMode compareMode;

        public void setCompareMode(CompareMode compareMode) {
            this.compareMode = compareMode;
        }

        public CompareMode getCompareMode() {
            return this.compareMode;
        }
    }

}

