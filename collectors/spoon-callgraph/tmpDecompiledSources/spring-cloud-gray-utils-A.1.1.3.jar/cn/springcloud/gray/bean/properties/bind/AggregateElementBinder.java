/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;

@FunctionalInterface
interface AggregateElementBinder {
    default public Object bind(ConfigurationPropertyName name, Bindable<?> target) {
        return this.bind(name, target, null);
    }

    public Object bind(ConfigurationPropertyName var1, Bindable<?> var2, ConfigurationPropertySource var3);
}

