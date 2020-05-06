/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.GrayEventListener
 *  cn.springcloud.gray.event.GrayEventMsg
 *  cn.springcloud.gray.exceptions.EventException
 */
package cn.springcloud.gray.event;

import cn.springcloud.gray.event.GrayEventListener;
import cn.springcloud.gray.event.GrayEventMsg;
import cn.springcloud.gray.event.sourcehander.SourceHanderService;
import cn.springcloud.gray.exceptions.EventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraySourceEventListener
implements GrayEventListener {
    private static final Logger log = LoggerFactory.getLogger(GraySourceEventListener.class);
    private SourceHanderService sourceHanderService;

    public GraySourceEventListener(SourceHanderService sourceHanderService) {
        this.sourceHanderService = sourceHanderService;
    }

    public void onEvent(GrayEventMsg msg) throws EventException {
        log.info("access gray event msg: {}", (Object)msg);
        this.sourceHanderService.handle(msg);
    }
}

