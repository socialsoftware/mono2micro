/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.Cache
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayStatus
 */
package cn.springcloud.gray;

import cn.springcloud.gray.Cache;
import cn.springcloud.gray.CachedGrayManager;
import cn.springcloud.gray.CommunicableGrayManager;
import cn.springcloud.gray.GrayClientConfig;
import cn.springcloud.gray.GrayClientHolder;
import cn.springcloud.gray.client.config.properties.GrayLoadProperties;
import cn.springcloud.gray.communication.InformationClient;
import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.decision.GrayDecisionFactoryKeeper;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayService;
import cn.springcloud.gray.model.GrayStatus;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultGrayManager
extends CachedGrayManager
implements CommunicableGrayManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultGrayManager.class);
    private Timer updateTimer = new Timer("Gray-Update-Timer", true);
    private GrayLoadProperties grayLoadProperties;
    private GrayClientConfig grayClientConfig;
    private InformationClient informationClient;
    private int scheduleOpenForWorkCount = 0;
    private int scheduleOpenForWorkLimit = 5;

    public DefaultGrayManager(GrayClientConfig grayClientConfig, GrayLoadProperties grayLoadProperties, GrayDecisionFactoryKeeper grayDecisionFactoryKeeper, InformationClient informationClient, Cache<String, List<GrayDecision>> grayDecisionCache) {
        super(grayDecisionFactoryKeeper, grayDecisionCache);
        this.grayLoadProperties = grayLoadProperties;
        this.grayClientConfig = grayClientConfig;
        this.informationClient = informationClient;
    }

    @Override
    public void setup() {
        super.setup();
        this.scheduleOpenForWork();
    }

    @Override
    public void shutdown() {
        super.shutdown();
        this.updateTimer.cancel();
    }

    @Override
    public boolean hasGray(String serviceId) {
        return GrayClientHolder.getGraySwitcher().state() && super.hasGray(serviceId);
    }

    public void openForWork() {
        if (this.getGrayInformationClient() != null) {
            log.info("\u62c9\u53d6\u7070\u5ea6\u5217\u8868");
            boolean t = this.doUpdate();
            int timerMs = this.getGrayClientConfig().getServiceUpdateIntervalTimerInMs();
            if (timerMs > 0) {
                this.updateTimer.schedule((TimerTask)new UpdateTask(), timerMs, (long)timerMs);
            } else if (!t) {
                this.scheduleOpenForWork();
            }
        } else {
            this.loadPropertiesGrays();
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
                DefaultGrayManager.this.openForWork();
            }
        }, this.getGrayClientConfig().getServiceInitializeDelayTimeInMs());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean doUpdate() {
        this.lock.lock();
        try {
            log.debug("\u66f4\u65b0\u7070\u5ea6\u670d\u52a1\u5217\u8868...");
            List<GrayInstance> grayInstances = this.getGrayInformationClient().allGrayInstances();
            if (grayInstances == null) {
                throw new NullPointerException();
            }
            ConcurrentHashMap<String, GrayService> grayServices = new ConcurrentHashMap<String, GrayService>();
            grayInstances.forEach(instance -> this.updateGrayInstance((Map<String, GrayService>)grayServices, (GrayInstance)instance));
            this.joinLoadedGrays(grayServices);
            this.setGrayServices(grayServices);
            boolean bl = true;
            return bl;
        }
        catch (Exception e) {
            log.error("\u66f4\u65b0\u7070\u5ea6\u670d\u52a1\u5217\u8868\u5931\u8d25", e);
            boolean grayServices = false;
            return grayServices;
        }
        finally {
            this.lock.unlock();
        }
    }

    private void loadPropertiesGrays() {
        ConcurrentHashMap<String, GrayService> grayServices = new ConcurrentHashMap<String, GrayService>();
        this.joinLoadedGrays(grayServices);
        this.setGrayServices(grayServices);
    }

    private void joinLoadedGrays(Map<String, GrayService> grayServices) {
        if (this.grayLoadProperties != null && this.grayLoadProperties.isEnabled()) {
            this.grayLoadProperties.getGrayInstances().forEach(instance -> {
                if (!grayServices.containsKey(instance.getServiceId()) || ((GrayService)grayServices.get(instance.getServiceId())).getGrayInstance(instance.getInstanceId()) == null) {
                    if (instance.getGrayStatus() == null) {
                        instance.setGrayStatus(GrayStatus.OPEN);
                    }
                    this.updateGrayInstance(grayServices, (GrayInstance)instance);
                }
            });
        }
    }

    public GrayClientConfig getGrayClientConfig() {
        return this.grayClientConfig;
    }

    @Override
    public InformationClient getGrayInformationClient() {
        return this.informationClient;
    }

    class UpdateTask
    extends TimerTask {
        UpdateTask() {
        }

        @Override
        public void run() {
            DefaultGrayManager.this.doUpdate();
        }
    }

}

