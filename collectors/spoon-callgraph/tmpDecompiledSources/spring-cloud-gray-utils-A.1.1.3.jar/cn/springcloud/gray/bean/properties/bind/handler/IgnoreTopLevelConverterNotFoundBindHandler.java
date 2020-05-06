/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.ConverterNotFoundException
 */
package cn.springcloud.gray.bean.properties.bind.handler;

import cn.springcloud.gray.bean.properties.bind.AbstractBindHandler;
import cn.springcloud.gray.bean.properties.bind.BindContext;
import cn.springcloud.gray.bean.properties.bind.BindHandler;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import org.springframework.core.convert.ConverterNotFoundException;

public class IgnoreTopLevelConverterNotFoundBindHandler
extends AbstractBindHandler {
    public IgnoreTopLevelConverterNotFoundBindHandler() {
    }

    public IgnoreTopLevelConverterNotFoundBindHandler(BindHandler parent) {
    }

    @Override
    public Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) throws Exception {
        if (context.getDepth() == 0 && error instanceof ConverterNotFoundException) {
            return null;
        }
        throw error;
    }
}

