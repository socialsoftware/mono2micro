/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.event;

import cn.springcloud.gray.event.GrayEventMsg;
import cn.springcloud.gray.exceptions.EventException;

public interface GrayEventListener {
    public void onEvent(GrayEventMsg var1) throws EventException;
}

