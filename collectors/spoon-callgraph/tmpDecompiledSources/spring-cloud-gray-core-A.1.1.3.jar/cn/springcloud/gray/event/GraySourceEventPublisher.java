/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.event;

import cn.springcloud.gray.event.GrayEventMsg;
import cn.springcloud.gray.event.GrayEventPublisher;
import cn.springcloud.gray.exceptions.EventException;

public interface GraySourceEventPublisher
extends GrayEventPublisher {
    public void publishEvent(GrayEventMsg var1, Object var2) throws EventException;

    public void publishEvent(GrayEventMsg var1, Object var2, long var3) throws EventException;

    public void asyncPublishEvent(GrayEventMsg var1, Object var2) throws EventException;

    public void asyncPublishEvent(GrayEventMsg var1, Object var2, long var3) throws EventException;
}

