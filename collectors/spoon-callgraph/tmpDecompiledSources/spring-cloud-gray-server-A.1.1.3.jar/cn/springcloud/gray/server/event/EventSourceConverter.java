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

public interface EventSourceConverter {
    public Object convert(EventType var1, SourceType var2, Object var3);
}

