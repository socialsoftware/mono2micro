/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package cn.springcloud.gray.client.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value="gray.server")
public class GrayServerProperties {
    private String url;
    private boolean loadbalanced;
    private boolean retryable = true;
    private int retryNumberOfRetries = 3;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLoadbalanced(boolean loadbalanced) {
        this.loadbalanced = loadbalanced;
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }

    public void setRetryNumberOfRetries(int retryNumberOfRetries) {
        this.retryNumberOfRetries = retryNumberOfRetries;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean isLoadbalanced() {
        return this.loadbalanced;
    }

    public boolean isRetryable() {
        return this.retryable;
    }

    public int getRetryNumberOfRetries() {
        return this.retryNumberOfRetries;
    }
}

