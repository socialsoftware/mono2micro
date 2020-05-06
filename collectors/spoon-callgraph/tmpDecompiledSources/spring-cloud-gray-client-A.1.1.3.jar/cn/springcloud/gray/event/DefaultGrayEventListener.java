/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.EventType
 *  cn.springcloud.gray.event.GrayEventListener
 *  cn.springcloud.gray.event.GrayEventMsg
 *  cn.springcloud.gray.event.SourceType
 *  cn.springcloud.gray.exceptions.EventException
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayTrackDefinition
 */
package cn.springcloud.gray.event;

import cn.springcloud.gray.CommunicableGrayManager;
import cn.springcloud.gray.GrayClientHolder;
import cn.springcloud.gray.communication.InformationClient;
import cn.springcloud.gray.event.EventType;
import cn.springcloud.gray.event.GrayEventListener;
import cn.springcloud.gray.event.GrayEventMsg;
import cn.springcloud.gray.event.SourceType;
import cn.springcloud.gray.exceptions.EventException;
import cn.springcloud.gray.local.InstanceLocalInfo;
import cn.springcloud.gray.local.InstanceLocalInfoAware;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.request.track.CommunicableGrayTrackHolder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultGrayEventListener
implements GrayEventListener,
InstanceLocalInfoAware {
    private static final Logger log = LoggerFactory.getLogger(DefaultGrayEventListener.class);
    private CommunicableGrayManager grayManager;
    private CommunicableGrayTrackHolder grayTrackHolder;
    private InstanceLocalInfo instanceLocalInfo;
    private Map<SourceType, Consumer<GrayEventMsg>> handers = new HashMap<SourceType, Consumer<GrayEventMsg>>();

    public DefaultGrayEventListener(CommunicableGrayTrackHolder grayTrackHolder, CommunicableGrayManager grayManager) {
        this.grayManager = grayManager;
        this.initHandlers();
        this.grayTrackHolder = grayTrackHolder;
    }

    public void onEvent(GrayEventMsg msg) throws EventException {
        this.handleSource(msg);
    }

    private void handleSource(GrayEventMsg msg) {
        Optional.ofNullable(this.getHandler(msg.getSourceType())).orElse(msg1 -> this.handleUpdateInstance(msg1.getServiceId(), msg1.getInstanceId())).accept(msg);
    }

    private Consumer<GrayEventMsg> getHandler(SourceType type) {
        return this.handers.get((Object)type);
    }

    private void initHandlers() {
        this.putHandler(SourceType.GRAY_INSTANCE, this::handleGrayInstance).putHandler(SourceType.GRAY_TRACK, this::handleGrayTrack);
    }

    private DefaultGrayEventListener putHandler(SourceType sourceType, Consumer<GrayEventMsg> handler) {
        this.handers.put(sourceType, handler);
        return this;
    }

    private void handleGrayInstance(GrayEventMsg msg) {
        InstanceLocalInfo instanceLocalInfo = this.getInstanceLocalInfo();
        if (instanceLocalInfo != null && StringUtils.equals(msg.getServiceId(), instanceLocalInfo.getServiceId())) {
            return;
        }
        switch (msg.getEventType()) {
            case DOWN: {
                this.grayManager.closeGray(msg.getServiceId(), msg.getInstanceId());
            }
            case UPDATE: {
                this.handleUpdateInstance(msg.getServiceId(), msg.getInstanceId());
            }
        }
    }

    private void handleUpdateInstance(String serviceId, String instanceId) {
        GrayInstance grayInstance = this.grayManager.getGrayInformationClient().getGrayInstance(serviceId, instanceId);
        this.grayManager.updateGrayInstance(grayInstance);
    }

    private void handleGrayTrack(GrayEventMsg msg) {
        InstanceLocalInfo instanceLocalInfo = this.getInstanceLocalInfo();
        if (instanceLocalInfo == null) {
            log.warn("instanceLocalInfo is null");
            return;
        }
        if (!StringUtils.equals(msg.getServiceId(), instanceLocalInfo.getServiceId())) {
            return;
        }
        if (StringUtils.isNotEmpty(msg.getInstanceId()) && !StringUtils.equals(msg.getInstanceId(), instanceLocalInfo.getInstanceId())) {
            return;
        }
        if (Objects.isNull(this.grayTrackHolder)) {
            return;
        }
        GrayTrackDefinition definition = (GrayTrackDefinition)msg.getSource();
        if (definition == null) {
            List<GrayTrackDefinition> definitions = this.grayTrackHolder.getGrayInformationClient().getTrackDefinitions(msg.getServiceId(), msg.getInstanceId());
            if (definitions != null) {
                definitions.forEach(d -> this.grayTrackHolder.updateTrackDefinition((GrayTrackDefinition)d));
            }
        } else {
            switch (msg.getEventType()) {
                case DOWN: {
                    this.grayTrackHolder.deleteTrackDefinition(definition.getName());
                }
                case UPDATE: {
                    this.grayTrackHolder.updateTrackDefinition(definition);
                }
            }
        }
    }

    @Override
    public void setInstanceLocalInfo(InstanceLocalInfo instanceLocalInfo) {
        this.instanceLocalInfo = instanceLocalInfo;
    }

    public InstanceLocalInfo getInstanceLocalInfo() {
        if (this.instanceLocalInfo == null) {
            this.instanceLocalInfo = GrayClientHolder.getInstanceLocalInfo();
        }
        return this.instanceLocalInfo;
    }

}

