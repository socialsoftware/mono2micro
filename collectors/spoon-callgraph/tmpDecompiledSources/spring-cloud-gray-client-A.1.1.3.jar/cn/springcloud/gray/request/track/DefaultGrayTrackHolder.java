/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayTrackDefinition
 */
package cn.springcloud.gray.request.track;

import cn.springcloud.gray.GrayClientHolder;
import cn.springcloud.gray.client.config.properties.GrayTrackProperties;
import cn.springcloud.gray.communication.InformationClient;
import cn.springcloud.gray.local.InstanceLocalInfo;
import cn.springcloud.gray.local.InstanceLocalInfoAware;
import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.request.GrayInfoTracker;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.track.AbstractCommunicableGrayTrackHolder;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultGrayTrackHolder
extends AbstractCommunicableGrayTrackHolder
implements InstanceLocalInfoAware {
    private static final Logger log = LoggerFactory.getLogger(DefaultGrayTrackHolder.class);
    private Timer updateTimer = new Timer("Gray-Track-Update-Timer", true);
    private GrayTrackProperties grayTrackProperties;
    private InstanceLocalInfo instanceLocalInfo;
    private int scheduleOpenForWorkCount = 0;
    private int scheduleOpenForWorkLimit = 5;

    public DefaultGrayTrackHolder(GrayTrackProperties grayTrackProperties, InformationClient informationClient, List<GrayInfoTracker<? extends GrayTrackInfo, ?>> trackers) {
        this(grayTrackProperties, informationClient, trackers, null);
    }

    public DefaultGrayTrackHolder(GrayTrackProperties grayTrackProperties, InformationClient informationClient, List<GrayInfoTracker<? extends GrayTrackInfo, ?>> trackers, List<GrayTrackDefinition> trackDefinitions) {
        super(informationClient, trackers, trackDefinitions);
        this.grayTrackProperties = grayTrackProperties;
    }

    public void setup() {
        this.scheduleOpenForWork();
    }

    public void openForWork() {
        log.info("\u62c9\u53d6\u7070\u5ea6\u8ffd\u8e2a\u5217\u8868");
        if (this.getGrayInformationClient() != null) {
            boolean t = this.doUpdate();
            int timerMs = this.grayTrackProperties.getDefinitionsUpdateIntervalTimerInMs();
            if (timerMs > 0) {
                this.updateTimer.schedule((TimerTask)new UpdateTask(), timerMs, (long)timerMs);
            } else if (!t) {
                this.scheduleOpenForWork();
            }
        } else {
            this.loadPropertiesTrackDefinitions();
        }
    }

    private void scheduleOpenForWork() {
        if (this.scheduleOpenForWorkCount > this.scheduleOpenForWorkLimit) {
            return;
        }
        ++this.scheduleOpenForWorkCount;
        this.updateTimer.schedule(new TimerTask(){

            @Override
            public void run() {
                DefaultGrayTrackHolder.this.openForWork();
            }
        }, this.grayTrackProperties.getDefinitionsInitializeDelayTimeInMs());
    }

    private boolean doUpdate() {
        ConcurrentHashMap<String, GrayTrackDefinition> trackDefinitionMap = new ConcurrentHashMap<String, GrayTrackDefinition>();
        try {
            log.debug("\u66f4\u65b0\u7070\u5ea6\u8ffd\u8e2a\u5217\u8868...");
            InstanceLocalInfo instanceLocalInfo = this.getInstanceLocalInfo();
            if (instanceLocalInfo == null) {
                log.warn("\u672c\u5730\u5b9e\u4f8b\u4fe1\u606f\u4e3anull,\u8df3\u8fc7\u66f4\u65b0");
                return false;
            }
            List<GrayTrackDefinition> trackDefinitions = this.getGrayInformationClient().getTrackDefinitions(instanceLocalInfo.getServiceId(), instanceLocalInfo.getInstanceId());
            trackDefinitions.forEach(definition -> this.updateTrackDefinition((Map<String, GrayTrackDefinition>)trackDefinitionMap, (GrayTrackDefinition)definition));
        }
        catch (Exception e) {
            log.error("\u66f4\u65b0\u7070\u5ea6\u8ffd\u8e2a\u5217\u8868\u5931\u8d25", e);
            return false;
        }
        this.joinLoadedTrackDefinitions(trackDefinitionMap);
        this.setTrackDefinitions(trackDefinitionMap);
        return true;
    }

    private void loadPropertiesTrackDefinitions() {
        ConcurrentHashMap<String, GrayTrackDefinition> trackDefinitionMap = new ConcurrentHashMap<String, GrayTrackDefinition>();
        this.joinLoadedTrackDefinitions(trackDefinitionMap);
        this.setTrackDefinitions(trackDefinitionMap);
    }

    private void joinLoadedTrackDefinitions(Map<String, GrayTrackDefinition> definitionMap) {
        this.grayTrackProperties.getWeb().getTrackDefinitions().forEach(definition -> {
            if (!definitionMap.containsKey(definition.getName())) {
                this.updateTrackDefinition(definitionMap, (GrayTrackDefinition)definition);
            }
        });
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

    class UpdateTask
    extends TimerTask {
        UpdateTask() {
        }

        @Override
        public void run() {
            DefaultGrayTrackHolder.this.doUpdate();
        }
    }

}

