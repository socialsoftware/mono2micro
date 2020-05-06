/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.DecisionDefinitionMsg
 *  cn.springcloud.gray.event.GrayEventMsg
 *  cn.springcloud.gray.event.SourceType
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayTrackDefinition
 *  cn.springcloud.gray.model.PolicyDefinition
 */
package cn.springcloud.gray.event.sourcehander;

import cn.springcloud.gray.event.DecisionDefinitionMsg;
import cn.springcloud.gray.event.GrayEventMsg;
import cn.springcloud.gray.event.SourceType;
import cn.springcloud.gray.event.sourcehander.GraySourceEventHandler;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.model.PolicyDefinition;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface SourceHanderService {
    public void handle(GrayEventMsg var1);

    public static class Default
    implements SourceHanderService {
        private List<GraySourceEventHandler> handlers;
        private Map<SourceType, Class> sourceTypeClassMap = new HashMap<SourceType, Class>();
        private ObjectMapper objectMapper = new ObjectMapper();

        public Default(List<GraySourceEventHandler> handlers) {
            this.handlers = handlers;
            this.sourceTypeClassMap.put(SourceType.GRAY_INSTANCE, GrayInstance.class);
            this.sourceTypeClassMap.put(SourceType.GRAY_DECISION, DecisionDefinitionMsg.class);
            this.sourceTypeClassMap.put(SourceType.GRAY_POLICY, PolicyDefinition.class);
            this.sourceTypeClassMap.put(SourceType.GRAY_TRACK, GrayTrackDefinition.class);
            this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        @Override
        public void handle(GrayEventMsg eventMsg) {
            GrayEventMsg msg = this.convertSourceEventMsg(eventMsg);
            this.handlers.forEach(handler -> handler.handle(msg));
        }

        private GrayEventMsg convertSourceEventMsg(GrayEventMsg eventMsg) {
            Object source = eventMsg.getSource();
            if (source == null) {
                return eventMsg;
            }
            Class sourceType = this.sourceTypeClassMap.get((Object)eventMsg.getSourceType());
            if (sourceType == null) {
                return eventMsg;
            }
            if (sourceType.isInstance(source)) {
                return eventMsg;
            }
            eventMsg.setSource(this.objectMapper.convertValue(source, sourceType));
            return eventMsg;
        }
    }

}

