/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.manager;

import cn.springcloud.gray.server.configuration.properties.GrayServerProperties;
import cn.springcloud.gray.server.evictor.GrayServerEvictor;
import cn.springcloud.gray.server.manager.GrayServiceManager;
import cn.springcloud.gray.server.module.gray.GrayServerModule;
import java.util.Timer;
import java.util.TimerTask;

public class DefaultGrayServiceManager
implements GrayServiceManager {
    private GrayServerProperties grayServerProperties;
    private Timer evictionTimer = new Timer("Gray-EvictionTimer", true);
    private GrayServerModule grayServerModule;
    private GrayServerEvictor grayServerEvictor;

    public DefaultGrayServiceManager(GrayServerProperties grayServerProperties, GrayServerModule grayServerModule, GrayServerEvictor grayServerEvictor) {
        this.grayServerProperties = grayServerProperties;
        this.grayServerModule = grayServerModule;
        this.grayServerEvictor = grayServerEvictor;
    }

    @Override
    public GrayServerModule getGrayServerModule() {
        return this.grayServerModule;
    }

    @Override
    public void openForWork() {
        GrayServerProperties.DiscoveryProperties discoveryProperties = this.grayServerProperties.getDiscovery();
        long times = discoveryProperties.getEvictionIntervalTimerInMs();
        if (discoveryProperties.isEvictionEnabled() && times > 0L) {
            this.evictionTimer.schedule((TimerTask)new EvictionTask(), times, times);
        }
    }

    @Override
    public void shutdown() {
        this.evictionTimer.cancel();
    }

    protected void evict() {
        if (!this.grayServerProperties.getDiscovery().isEvictionEnabled()) {
            return;
        }
        this.grayServerEvictor.evict(this.getGrayServerModule());
    }

    class EvictionTask
    extends TimerTask {
        EvictionTask() {
        }

        @Override
        public void run() {
            DefaultGrayServiceManager.this.evict();
        }
    }

}

