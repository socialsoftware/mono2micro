/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.properties.bind.BindContext;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;

public interface BindHandler {
    public static final BindHandler DEFAULT = new BindHandler(){};

    default public <T> Bindable<T> onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
        return target;
    }

    default public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        return result;
    }

    default public Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) throws Exception {
        throw error;
    }

    default public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) throws Exception {
    }

}

