/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.EventType
 *  cn.springcloud.gray.event.GrayEventMsg
 *  cn.springcloud.gray.event.SourceType
 *  cn.springcloud.gray.model.GrayInstance
 */
package cn.springcloud.gray.event.sourcehander;

import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.event.EventType;
import cn.springcloud.gray.event.GrayEventMsg;
import cn.springcloud.gray.event.SourceType;
import cn.springcloud.gray.event.sourcehander.GraySourceEventHandler;
import cn.springcloud.gray.local.InstanceLocalInfo;
import cn.springcloud.gray.local.InstanceLocalInfoInitiralizer;
import cn.springcloud.gray.model.GrayInstance;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrayInstanceEventHandler
implements GraySourceEventHandler {
    private static final Logger log = LoggerFactory.getLogger(GrayInstanceEventHandler.class);
    private GrayManager grayManager;
    private InstanceLocalInfoInitiralizer instanceLocalInfoInitiralizer;

    public GrayInstanceEventHandler(GrayManager grayManager, InstanceLocalInfoInitiralizer instanceLocalInfoInitiralizer) {
        this.grayManager = grayManager;
        this.instanceLocalInfoInitiralizer = instanceLocalInfoInitiralizer;
    }

    @Override
    public void handle(GrayEventMsg eventMsg) {
        if (!Objects.equals((Object)eventMsg.getSourceType(), (Object)SourceType.GRAY_INSTANCE)) {
            return;
        }
        InstanceLocalInfo instanceLocalInfo = this.instanceLocalInfoInitiralizer.getInstanceLocalInfo();
        if (instanceLocalInfo != null && StringUtils.equals(eventMsg.getServiceId(), instanceLocalInfo.getServiceId())) {
            return;
        }
        if (Objects.equals((Object)eventMsg.getEventType(), (Object)EventType.UPDATE)) {
            this.grayManager.updateGrayInstance((GrayInstance)eventMsg.getSource());
        } else {
            this.grayManager.closeGray(eventMsg.getServiceId(), eventMsg.getInstanceId());
        }
    }
}

