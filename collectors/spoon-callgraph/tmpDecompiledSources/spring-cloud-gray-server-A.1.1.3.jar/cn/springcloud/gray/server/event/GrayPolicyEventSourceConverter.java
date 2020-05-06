/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.EventType
 *  cn.springcloud.gray.event.SourceType
 *  cn.springcloud.gray.model.PolicyDefinition
 */
package cn.springcloud.gray.server.event;

import cn.springcloud.gray.event.EventType;
import cn.springcloud.gray.event.SourceType;
import cn.springcloud.gray.model.PolicyDefinition;
import cn.springcloud.gray.server.event.GrayModuleAwareEventSourceConverter;
import cn.springcloud.gray.server.module.gray.GrayModule;
import cn.springcloud.gray.server.module.gray.domain.GrayPolicy;

public class GrayPolicyEventSourceConverter
extends GrayModuleAwareEventSourceConverter {
    @Override
    public Object convert(EventType eventType, SourceType sourceType, Object source) {
        if (sourceType != SourceType.GRAY_POLICY) {
            return null;
        }
        if (source == null) {
            throw new NullPointerException("event msg source is null");
        }
        return this.grayModule.ofGrayPolicy((GrayPolicy)source);
    }
}

