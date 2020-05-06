/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.DecisionDefinitionMsg
 *  cn.springcloud.gray.event.EventType
 *  cn.springcloud.gray.event.GrayEventMsg
 *  cn.springcloud.gray.event.SourceType
 *  cn.springcloud.gray.model.DecisionDefinition
 */
package cn.springcloud.gray.event.sourcehander;

import cn.springcloud.gray.UpdateableGrayManager;
import cn.springcloud.gray.event.DecisionDefinitionMsg;
import cn.springcloud.gray.event.EventType;
import cn.springcloud.gray.event.GrayEventMsg;
import cn.springcloud.gray.event.SourceType;
import cn.springcloud.gray.event.sourcehander.GraySourceEventHandler;
import cn.springcloud.gray.local.InstanceLocalInfo;
import cn.springcloud.gray.local.InstanceLocalInfoInitiralizer;
import cn.springcloud.gray.model.DecisionDefinition;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrayDecisionEventHandler
implements GraySourceEventHandler {
    private static final Logger log = LoggerFactory.getLogger(GrayDecisionEventHandler.class);
    private UpdateableGrayManager grayManager;
    private InstanceLocalInfoInitiralizer instanceLocalInfoInitiralizer;

    public GrayDecisionEventHandler(UpdateableGrayManager grayManager, InstanceLocalInfoInitiralizer instanceLocalInfoInitiralizer) {
        this.grayManager = grayManager;
        this.instanceLocalInfoInitiralizer = instanceLocalInfoInitiralizer;
    }

    @Override
    public void handle(GrayEventMsg eventMsg) {
        if (!Objects.equals((Object)eventMsg.getSourceType(), (Object)SourceType.GRAY_DECISION)) {
            return;
        }
        if (eventMsg.getSource() == null) {
            throw new NullPointerException("event source is null");
        }
        InstanceLocalInfo instanceLocalInfo = this.instanceLocalInfoInitiralizer.getInstanceLocalInfo();
        if (instanceLocalInfo != null && StringUtils.equals(eventMsg.getServiceId(), instanceLocalInfo.getServiceId())) {
            return;
        }
        DecisionDefinitionMsg decisionDefinitionMsg = (DecisionDefinitionMsg)eventMsg.getSource();
        if (Objects.equals((Object)eventMsg.getEventType(), (Object)EventType.UPDATE)) {
            DecisionDefinition decisionDefinition = new DecisionDefinition();
            decisionDefinition.setInfos(decisionDefinitionMsg.getInfos());
            decisionDefinition.setName(decisionDefinitionMsg.getName());
            decisionDefinition.setId(decisionDefinitionMsg.getId());
            this.grayManager.updateDecisionDefinition(eventMsg.getServiceId(), eventMsg.getInstanceId(), decisionDefinitionMsg.getPolicyId(), decisionDefinition);
        } else {
            this.grayManager.removeDecisionDefinition(eventMsg.getServiceId(), eventMsg.getInstanceId(), decisionDefinitionMsg.getPolicyId(), decisionDefinitionMsg.getId());
        }
    }
}

