/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.EventType
 *  cn.springcloud.gray.event.SourceType
 *  cn.springcloud.gray.model.GrayTrackDefinition
 */
package cn.springcloud.gray.server.event;

import cn.springcloud.gray.event.EventType;
import cn.springcloud.gray.event.SourceType;
import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.server.event.GrayModuleAwareEventSourceConverter;
import cn.springcloud.gray.server.module.gray.GrayModule;
import cn.springcloud.gray.server.module.gray.domain.GrayTrack;

public class GrayTrackEventSourceConverter
extends GrayModuleAwareEventSourceConverter {
    @Override
    public Object convert(EventType eventType, SourceType sourceType, Object source) {
        if (sourceType != SourceType.GRAY_TRACK) {
            return null;
        }
        if (source == null) {
            throw new NullPointerException("event msg source is null");
        }
        return this.grayModule.ofGrayTrack((GrayTrack)source);
    }
}

