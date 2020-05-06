/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.decision.factory;

import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.utils.NameUtils;
import java.util.function.Consumer;

public interface GrayDecisionFactory<C> {
    default public String name() {
        return NameUtils.normalizeDecisionFactoryName(this.getClass());
    }

    default public C newConfig() {
        throw new UnsupportedOperationException("newConfig() not implemented");
    }

    default public GrayDecision apply(Consumer<C> consumer) {
        C config = this.newConfig();
        consumer.accept(config);
        return this.apply(config);
    }

    public GrayDecision apply(C var1);
}

