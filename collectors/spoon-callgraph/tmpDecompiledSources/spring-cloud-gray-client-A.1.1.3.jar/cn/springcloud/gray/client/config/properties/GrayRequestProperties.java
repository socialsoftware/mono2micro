/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package cn.springcloud.gray.client.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="gray.request")
public class GrayRequestProperties {
    private boolean loadBody = false;

    public void setLoadBody(boolean loadBody) {
        this.loadBody = loadBody;
    }

    public boolean isLoadBody() {
        return this.loadBody;
    }
}

