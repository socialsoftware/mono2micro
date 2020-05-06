/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.EventType
 *  cn.springcloud.gray.event.SourceType
 *  cn.springcloud.gray.model.GrayInstance
 */
package cn.springcloud.gray.server.event;

import cn.springcloud.gray.event.EventType;
import cn.springcloud.gray.event.SourceType;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.server.event.GrayModuleAwareEventSourceConverter;
import cn.springcloud.gray.server.module.gray.GrayModule;
import java.util.Objects;

public class GrayInstanceEventSourceConverter
extends GrayModuleAwareEventSourceConverter {
    @Override
    public Object convert(EventType eventType, SourceType sourceType, Object source) {
        if (!Objects.equals((Object)sourceType, (Object)SourceType.GRAY_INSTANCE)) {
            return null;
        }
        if (Objects.equals((Object)eventType, (Object)EventType.DOWN) || source == null) {
            return null;
        }
        cn.springcloud.gray.server.module.gray.domain.GrayInstance grayInstance = (cn.springcloud.gray.server.module.gray.domain.GrayInstance)source;
        return this.grayModule.getGrayInstance(grayInstance.getServiceId(), grayInstance.getInstanceId());
    }
}

