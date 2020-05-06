/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package cn.springcloud.gray.client.netflix.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value="gray.hystrix")
public class GrayHystrixProperties {
    private boolean enabled;
    private ThreadTransmitStrategy threadTransmitStrategy = ThreadTransmitStrategy.WRAP_CALLABLE;

    public boolean isEnabled() {
        return this.enabled;
    }

    public ThreadTransmitStrategy getThreadTransmitStrategy() {
        return this.threadTransmitStrategy;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setThreadTransmitStrategy(ThreadTransmitStrategy threadTransmitStrategy) {
        this.threadTransmitStrategy = threadTransmitStrategy;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GrayHystrixProperties)) {
            return false;
        }
        GrayHystrixProperties other = (GrayHystrixProperties)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isEnabled() != other.isEnabled()) {
            return false;
        }
        ThreadTransmitStrategy this$threadTransmitStrategy = this.getThreadTransmitStrategy();
        ThreadTransmitStrategy other$threadTransmitStrategy = other.getThreadTransmitStrategy();
        return !(this$threadTransmitStrategy == null ? other$threadTransmitStrategy != null : !((Object)((Object)this$threadTransmitStrategy)).equals((Object)other$threadTransmitStrategy));
    }

    protected boolean canEqual(Object other) {
        return other instanceof GrayHystrixProperties;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isEnabled() ? 79 : 97);
        ThreadTransmitStrategy $threadTransmitStrategy = this.getThreadTransmitStrategy();
        result = result * 59 + ($threadTransmitStrategy == null ? 43 : ((Object)((Object)$threadTransmitStrategy)).hashCode());
        return result;
    }

    public String toString() {
        return "GrayHystrixProperties(enabled=" + this.isEnabled() + ", threadTransmitStrategy=" + (Object)((Object)this.getThreadTransmitStrategy()) + ")";
    }

    public static enum ThreadTransmitStrategy {
        WRAP_CALLABLE,
        HYSTRIX_REQUEST_LOCAL_STORAGE;
        
    }

}

