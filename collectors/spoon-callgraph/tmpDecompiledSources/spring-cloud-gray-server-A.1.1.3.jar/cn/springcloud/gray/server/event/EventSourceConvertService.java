/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.EventType
 *  cn.springcloud.gray.event.SourceType
 */
package cn.springcloud.gray.server.event;

import cn.springcloud.gray.event.EventType;
import cn.springcloud.gray.event.SourceType;
import cn.springcloud.gray.server.event.EventSourceConverter;
import java.util.List;

public interface EventSourceConvertService {
    public Object convert(EventType var1, SourceType var2, Object var3);

    public static class Default
    implements EventSourceConvertService {
        private List<EventSourceConverter> converters;

        public Default(List<EventSourceConverter> converters) {
            this.converters = converters;
        }

        @Override
        public Object convert(EventType eventType, SourceType sourceType, Object source) {
            for (EventSourceConverter converter : this.converters) {
                Object value = converter.convert(eventType, sourceType, source);
                if (value == null) continue;
                return value;
            }
            return null;
        }
    }

}

