/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.BeanUtils
 */
package cn.springcloud.gray.decision.factory;

import cn.springcloud.gray.decision.factory.GrayDecisionFactory;
import org.springframework.beans.BeanUtils;

public abstract class AbstractGrayDecisionFactory<C>
implements GrayDecisionFactory<C> {
    private Class<C> configClass;

    protected AbstractGrayDecisionFactory(Class<C> configClass) {
        this.configClass = configClass;
    }

    public Class<C> getConfigClass() {
        return this.configClass;
    }

    @Override
    public C newConfig() {
        return (C)BeanUtils.instantiateClass(this.getConfigClass());
    }
}

