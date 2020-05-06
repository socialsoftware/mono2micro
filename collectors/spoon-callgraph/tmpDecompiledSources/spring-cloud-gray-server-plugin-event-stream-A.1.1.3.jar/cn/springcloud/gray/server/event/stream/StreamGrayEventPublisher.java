/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.GrayEventMsg
 *  cn.springcloud.gray.event.GrayEventPublisher
 *  cn.springcloud.gray.exceptions.EventException
 *  org.springframework.messaging.Message
 *  org.springframework.messaging.MessageChannel
 *  org.springframework.messaging.support.MessageBuilder
 */
package cn.springcloud.gray.server.event.stream;

import cn.springcloud.gray.event.GrayEventMsg;
import cn.springcloud.gray.event.GrayEventPublisher;
import cn.springcloud.gray.exceptions.EventException;
import cn.springcloud.gray.server.event.stream.StreamOutput;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

public class StreamGrayEventPublisher
implements GrayEventPublisher {
    private ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
    private StreamOutput sender;

    public StreamGrayEventPublisher(StreamOutput sender) {
        this.sender = sender;
    }

    public boolean send(Object obj) {
        return this.sender.output().send(MessageBuilder.withPayload((Object)obj).build());
    }

    public void publishEvent(GrayEventMsg msg) throws EventException {
        this.executorService.schedule(() -> this.send((Object)msg), 100L, TimeUnit.MILLISECONDS);
    }
}

