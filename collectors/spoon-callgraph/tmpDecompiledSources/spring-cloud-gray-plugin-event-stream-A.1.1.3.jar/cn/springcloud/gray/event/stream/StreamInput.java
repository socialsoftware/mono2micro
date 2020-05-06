/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.cloud.stream.annotation.Input
 *  org.springframework.messaging.SubscribableChannel
 */
package cn.springcloud.gray.event.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface StreamInput {
    public static final String INPUT = "GrayEventInput";

    @Input(value="GrayEventInput")
    public SubscribableChannel input();
}

