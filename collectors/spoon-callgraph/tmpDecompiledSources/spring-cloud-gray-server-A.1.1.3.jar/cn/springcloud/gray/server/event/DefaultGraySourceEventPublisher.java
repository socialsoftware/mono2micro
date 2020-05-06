/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.EventType
 *  cn.springcloud.gray.event.GrayEventMsg
 *  cn.springcloud.gray.event.GrayEventPublisher
 *  cn.springcloud.gray.event.GraySourceEventPublisher
 *  cn.springcloud.gray.event.SourceType
 *  cn.springcloud.gray.exceptions.EventException
 */
package cn.springcloud.gray.server.event;

import cn.springcloud.gray.event.EventType;
import cn.springcloud.gray.event.GrayEventMsg;
import cn.springcloud.gray.event.GrayEventPublisher;
import cn.springcloud.gray.event.GraySourceEventPublisher;
import cn.springcloud.gray.event.SourceType;
import cn.springcloud.gray.exceptions.EventException;
import cn.springcloud.gray.server.event.EventSourceConvertService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultGraySourceEventPublisher
implements GraySourceEventPublisher {
    private GrayEventPublisher publisherDelegater;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private EventSourceConvertService eventSourceConvertService;

    public DefaultGraySourceEventPublisher(GrayEventPublisher publisherDelegater, ExecutorService executorService, EventSourceConvertService eventSourceConvertService) {
        this.publisherDelegater = publisherDelegater;
        this.executorService = executorService;
        this.eventSourceConvertService = eventSourceConvertService;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    }

    public void publishEvent(GrayEventMsg msg, Object source) throws EventException {
        Object eventSource = this.getEventMsgSource(msg, source);
        msg.setSource(eventSource);
        this.publishEvent(msg);
    }

    public void publishEvent(GrayEventMsg msg, Object source, long delayTimeMs) throws EventException {
        this.scheduledExecutorService.schedule(() -> this.publishEvent(msg, source), delayTimeMs, TimeUnit.MILLISECONDS);
    }

    public void asyncPublishEvent(GrayEventMsg msg, Object source) throws EventException {
        this.executorService.submit(() -> this.publishEvent(msg, source));
    }

    public void asyncPublishEvent(GrayEventMsg msg, Object source, long delayTimeMs) throws EventException {
        this.executorService.submit(() -> this.publishEvent(msg, source, delayTimeMs));
    }

    public void publishEvent(GrayEventMsg msg) throws EventException {
        this.publisherDelegater.publishEvent(msg);
    }

    private Object getEventMsgSource(GrayEventMsg msg, Object source) {
        return this.eventSourceConvertService.convert(msg.getEventType(), msg.getSourceType(), source);
    }
}

