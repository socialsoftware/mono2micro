/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceStatus
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package cn.springcloud.gray.client.config.properties;

import cn.springcloud.gray.model.InstanceStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value="gray.holdoutServer")
public class GrayHoldoutServerProperties {
    private boolean enabled;
    private boolean zoneAffinity;
    private boolean cacheable;
    private Map<String, List<InstanceStatus>> services = new HashMap<String, List<InstanceStatus>>();

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isZoneAffinity() {
        return this.zoneAffinity;
    }

    public boolean isCacheable() {
        return this.cacheable;
    }

    public Map<String, List<InstanceStatus>> getServices() {
        return this.services;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setZoneAffinity(boolean zoneAffinity) {
        this.zoneAffinity = zoneAffinity;
    }

    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    public void setServices(Map<String, List<InstanceStatus>> services) {
        this.services = services;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GrayHoldoutServerProperties)) {
            return false;
        }
        GrayHoldoutServerProperties other = (GrayHoldoutServerProperties)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isEnabled() != other.isEnabled()) {
            return false;
        }
        if (this.isZoneAffinity() != other.isZoneAffinity()) {
            return false;
        }
        if (this.isCacheable() != other.isCacheable()) {
            return false;
        }
        Map<String, List<InstanceStatus>> this$services = this.getServices();
        Map<String, List<InstanceStatus>> other$services = other.getServices();
        return !(this$services == null ? other$services != null : !((Object)this$services).equals(other$services));
    }

    protected boolean canEqual(Object other) {
        return other instanceof GrayHoldoutServerProperties;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isEnabled() ? 79 : 97);
        result = result * 59 + (this.isZoneAffinity() ? 79 : 97);
        result = result * 59 + (this.isCacheable() ? 79 : 97);
        Map<String, List<InstanceStatus>> $services = this.getServices();
        result = result * 59 + ($services == null ? 43 : ((Object)$services).hashCode());
        return result;
    }

    public String toString() {
        return "GrayHoldoutServerProperties(enabled=" + this.isEnabled() + ", zoneAffinity=" + this.isZoneAffinity() + ", cacheable=" + this.isCacheable() + ", services=" + this.getServices() + ")";
    }
}

