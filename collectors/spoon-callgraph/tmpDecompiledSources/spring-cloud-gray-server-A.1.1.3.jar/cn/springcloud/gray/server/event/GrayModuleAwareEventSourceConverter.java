/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.context.annotation.Lazy
 */
package cn.springcloud.gray.server.event;

import cn.springcloud.gray.server.event.EventSourceConverter;
import cn.springcloud.gray.server.module.gray.GrayModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

public abstract class GrayModuleAwareEventSourceConverter
implements EventSourceConverter {
    @Autowired
    @Lazy
    protected GrayModule grayModule;
}

