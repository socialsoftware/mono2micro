/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.DecisionDefinitionMsg
 *  cn.springcloud.gray.event.EventType
 *  cn.springcloud.gray.event.SourceType
 *  cn.springcloud.gray.exceptions.EventException
 *  cn.springcloud.gray.model.DecisionDefinition
 */
package cn.springcloud.gray.server.event;

import cn.springcloud.gray.event.DecisionDefinitionMsg;
import cn.springcloud.gray.event.EventType;
import cn.springcloud.gray.event.SourceType;
import cn.springcloud.gray.exceptions.EventException;
import cn.springcloud.gray.model.DecisionDefinition;
import cn.springcloud.gray.server.event.GrayModuleAwareEventSourceConverter;
import cn.springcloud.gray.server.module.gray.GrayModule;
import cn.springcloud.gray.server.module.gray.domain.GrayDecision;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrayDecisionEventSourceConverter
extends GrayModuleAwareEventSourceConverter {
    private static final Logger log = LoggerFactory.getLogger(GrayDecisionEventSourceConverter.class);

    @Override
    public Object convert(EventType eventType, SourceType sourceType, Object source) {
        if (!Objects.equals((Object)sourceType, (Object)SourceType.GRAY_DECISION)) {
            return null;
        }
        if (source == null) {
            throw new NullPointerException("event msg source is null");
        }
        GrayDecision grayDecision = (GrayDecision)source;
        DecisionDefinition decisionDefinition = null;
        try {
            decisionDefinition = this.grayModule.ofGrayDecision(grayDecision);
        }
        catch (IOException e) {
            log.error("\u4eceGrayDecision\u8f6cDecisionDefinition\u5931\u8d25:{}", (Object)grayDecision);
            throw new EventException("\u4eceGrayDecision\u8f6cDecisionDefinition\u5931\u8d25", (Throwable)e);
        }
        DecisionDefinitionMsg decisionDefinitionMsg = new DecisionDefinitionMsg();
        decisionDefinitionMsg.setPolicyId(String.valueOf(grayDecision.getPolicyId()));
        decisionDefinitionMsg.setId(decisionDefinition.getId());
        if (!Objects.equals((Object)eventType, (Object)EventType.DOWN)) {
            decisionDefinitionMsg.setInfos(decisionDefinition.getInfos());
        }
        decisionDefinitionMsg.setName(decisionDefinition.getName());
        return decisionDefinitionMsg;
    }
}

