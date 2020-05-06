/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.properties.bind.BeanPropertyBinder;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.bind.Binder;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;

interface BeanBinder {
    public <T> T bind(ConfigurationPropertyName var1, Bindable<T> var2, Binder.Context var3, BeanPropertyBinder var4);
}

