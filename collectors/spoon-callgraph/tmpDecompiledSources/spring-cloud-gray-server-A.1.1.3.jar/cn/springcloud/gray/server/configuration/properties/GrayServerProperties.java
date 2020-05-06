/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceStatus
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package cn.springcloud.gray.server.configuration.properties;

import cn.springcloud.gray.model.InstanceStatus;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="gray.server")
public class GrayServerProperties {
    private DiscoveryProperties discovery = new DiscoveryProperties();
    private InstanceProperties instance = new InstanceProperties();

    public void setDiscovery(DiscoveryProperties discovery) {
        this.discovery = discovery;
    }

    public void setInstance(InstanceProperties instance) {
        this.instance = instance;
    }

    public DiscoveryProperties getDiscovery() {
        return this.discovery;
    }

    public InstanceProperties getInstance() {
        return this.instance;
    }

    public static class InstanceRecordEvictProperties {
        private boolean enabled;
        private long evictionIntervalTimerInMs = TimeUnit.DAYS.toMillis(1L);
        private Set<InstanceStatus> evictionInstanceStatus = new HashSet<InstanceStatus>(Arrays.asList(new InstanceStatus[]{InstanceStatus.DOWN, InstanceStatus.UNKNOWN}));
        private int lastUpdateDateExpireDays = 1;

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setEvictionIntervalTimerInMs(long evictionIntervalTimerInMs) {
            this.evictionIntervalTimerInMs = evictionIntervalTimerInMs;
        }

        public void setEvictionInstanceStatus(Set<InstanceStatus> evictionInstanceStatus) {
            this.evictionInstanceStatus = evictionInstanceStatus;
        }

        public void setLastUpdateDateExpireDays(int lastUpdateDateExpireDays) {
            this.lastUpdateDateExpireDays = lastUpdateDateExpireDays;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public long getEvictionIntervalTimerInMs() {
            return this.evictionIntervalTimerInMs;
        }

        public Set<InstanceStatus> getEvictionInstanceStatus() {
            return this.evictionInstanceStatus;
        }

        public int getLastUpdateDateExpireDays() {
            return this.lastUpdateDateExpireDays;
        }
    }

    public static class DiscoveryProperties {
        private boolean evictionEnabled = true;
        private long evictionIntervalTimerInMs = TimeUnit.SECONDS.toMillis(60L);

        public void setEvictionEnabled(boolean evictionEnabled) {
            this.evictionEnabled = evictionEnabled;
        }

        public void setEvictionIntervalTimerInMs(long evictionIntervalTimerInMs) {
            this.evictionIntervalTimerInMs = evictionIntervalTimerInMs;
        }

        public boolean isEvictionEnabled() {
            return this.evictionEnabled;
        }

        public long getEvictionIntervalTimerInMs() {
            return this.evictionIntervalTimerInMs;
        }
    }

    public static class InstanceProperties {
        private Set<InstanceStatus> normalInstanceStatus = new HashSet<InstanceStatus>(Arrays.asList(new InstanceStatus[]{InstanceStatus.STARTING, InstanceStatus.UP}));
        private InstanceRecordEvictProperties eviction = new InstanceRecordEvictProperties();

        public void setNormalInstanceStatus(Set<InstanceStatus> normalInstanceStatus) {
            this.normalInstanceStatus = normalInstanceStatus;
        }

        public void setEviction(InstanceRecordEvictProperties eviction) {
            this.eviction = eviction;
        }

        public Set<InstanceStatus> getNormalInstanceStatus() {
            return this.normalInstanceStatus;
        }

        public InstanceRecordEvictProperties getEviction() {
            return this.eviction;
        }
    }

}

