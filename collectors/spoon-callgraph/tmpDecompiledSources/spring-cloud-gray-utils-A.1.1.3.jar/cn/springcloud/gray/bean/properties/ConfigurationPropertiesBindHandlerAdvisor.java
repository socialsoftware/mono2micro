/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties;

import cn.springcloud.gray.bean.properties.bind.BindHandler;

@FunctionalInterface
public interface ConfigurationPropertiesBindHandlerAdvisor {
    public BindHandler apply(BindHandler var1);
}

