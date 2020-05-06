/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.cloud.stream.annotation.Output
 *  org.springframework.messaging.MessageChannel
 */
package cn.springcloud.gray.server.event.stream;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface StreamOutput {
    public static final String OUTPUT = "GrayEventOutput";

    @Output(value="GrayEventOutput")
    public MessageChannel output();
}

