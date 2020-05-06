/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.GrayEventListener
 *  cn.springcloud.gray.event.GrayEventMsg
 *  org.springframework.cloud.stream.annotation.StreamListener
 */
package cn.springcloud.gray.event.stream;

import cn.springcloud.gray.event.GrayEventListener;
import cn.springcloud.gray.event.GrayEventMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;

public class StreamMessageListener {
    private static final Logger log = LoggerFactory.getLogger(StreamMessageListener.class);
    private GrayEventListener grayEventListener;

    public StreamMessageListener(GrayEventListener grayEventListener) {
        this.grayEventListener = grayEventListener;
    }

    @StreamListener(value="GrayEventInput")
    public void receive(GrayEventMsg msg) {
        log.debug("\u63a5\u6536\u5230\u6d88\u606f:{}", (Object)msg);
        this.grayEventListener.onEvent(msg);
    }
}

