/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.properties.bind.Bindable;

interface BeanPropertyBinder {
    public Object bindProperty(String var1, Bindable<?> var2);
}

