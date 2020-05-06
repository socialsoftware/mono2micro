/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package cn.springcloud.gray.client.config.properties;

import cn.springcloud.gray.GrayClientConfig;
import cn.springcloud.gray.client.config.properties.CacheProperties;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value="gray.client")
public class GrayClientProperties
implements GrayClientConfig {
    private String runenv = "web";
    private int serviceUpdateIntervalTimerInMs = 60000;
    private int serviceInitializeDelayTimeInMs = 40000;
    private InstanceConfig instance = new InstanceConfig();
    private Map<String, CacheProperties> caches = new HashMap<String, CacheProperties>();

    @Override
    public String runenv() {
        return this.runenv;
    }

    @Override
    public boolean isGrayEnroll() {
        return this.instance.isGrayEnroll();
    }

    @Override
    public int grayEnrollDealyTimeInMs() {
        return this.instance.getGrayEnrollDealyTimeInMs();
    }

    @Override
    public int getServiceUpdateIntervalTimerInMs() {
        return this.serviceUpdateIntervalTimerInMs;
    }

    public Map<String, CacheProperties> getCaches() {
        return this.caches;
    }

    public void setCaches(Map<String, CacheProperties> caches) {
        this.caches = caches;
    }

    public CacheProperties getCacheProperties(String key) {
        CacheProperties cacheProperties = this.getCaches().get(key);
        if (cacheProperties == null) {
            cacheProperties = new CacheProperties();
            this.caches.put(key, cacheProperties);
        }
        return cacheProperties;
    }

    public void setServiceUpdateIntervalTimerInMs(int serviceUpdateIntervalTimerInMs) {
        this.serviceUpdateIntervalTimerInMs = serviceUpdateIntervalTimerInMs;
    }

    @Override
    public int getServiceInitializeDelayTimeInMs() {
        return this.serviceInitializeDelayTimeInMs;
    }

    public void setServiceInitializeDelayTimeInMs(int serviceInitializeDelayTimeInMs) {
        this.serviceInitializeDelayTimeInMs = serviceInitializeDelayTimeInMs;
    }

    public InstanceConfig getInstance() {
        return this.instance;
    }

    public void setInstance(InstanceConfig instance) {
        this.instance = instance;
    }

    public class InstanceConfig {
        private boolean grayEnroll = false;
        private int grayEnrollDealyTimeInMs = 40000;
        private boolean useMultiVersion = false;

        public boolean isGrayEnroll() {
            return this.grayEnroll;
        }

        public void setGrayEnroll(boolean grayEnroll) {
            this.grayEnroll = grayEnroll;
        }

        public int getGrayEnrollDealyTimeInMs() {
            return this.grayEnrollDealyTimeInMs;
        }

        public void setGrayEnrollDealyTimeInMs(int grayEnrollDealyTimeInMs) {
            this.grayEnrollDealyTimeInMs = grayEnrollDealyTimeInMs;
        }

        public boolean isUseMultiVersion() {
            return this.useMultiVersion;
        }

        public void setUseMultiVersion(boolean useMultiVersion) {
            this.useMultiVersion = useMultiVersion;
        }
    }

}

