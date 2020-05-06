/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.properties.bind.Binder;
import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;

public interface BindContext {
    public Binder getBinder();

    public int getDepth();

    public Iterable<ConfigurationPropertySource> getSources();

    public ConfigurationProperty getConfigurationProperty();
}

