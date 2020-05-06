/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.bind.handler;

import cn.springcloud.gray.bean.properties.bind.AbstractBindHandler;
import cn.springcloud.gray.bean.properties.bind.BindContext;
import cn.springcloud.gray.bean.properties.bind.BindHandler;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import java.util.function.Supplier;

public class IgnoreErrorsBindHandler
extends AbstractBindHandler {
    public IgnoreErrorsBindHandler() {
    }

    public IgnoreErrorsBindHandler(BindHandler parent) {
        super(parent);
    }

    @Override
    public Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) throws Exception {
        return target.getValue() != null ? target.getValue().get() : null;
    }
}

