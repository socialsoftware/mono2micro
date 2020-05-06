/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.bean.properties.ConfigurationProperties
 */
package cn.springcloud.gray.eureka.server.configuration.properties;

import cn.springcloud.gray.bean.properties.ConfigurationProperties;

@ConfigurationProperties(value="gray.server")
public class GrayServerProperties {
    private String url;
    private boolean retryable = true;
    private int retryNumberOfRetries = 3;

    public String getUrl() {
        return this.url;
    }

    public boolean isRetryable() {
        return this.retryable;
    }

    public int getRetryNumberOfRetries() {
        return this.retryNumberOfRetries;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }

    public void setRetryNumberOfRetries(int retryNumberOfRetries) {
        this.retryNumberOfRetries = retryNumberOfRetries;
    }
}

