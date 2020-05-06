/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayInstance
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package cn.springcloud.gray.client.config.properties;

import cn.springcloud.gray.model.GrayInstance;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value="gray.load")
public class GrayLoadProperties {
    private boolean enabled = false;
    private List<GrayInstance> grayInstances = new ArrayList<GrayInstance>();

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setGrayInstances(List<GrayInstance> grayInstances) {
        this.grayInstances = grayInstances;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public List<GrayInstance> getGrayInstances() {
        return this.grayInstances;
    }
}

