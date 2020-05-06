/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.client.config.properties;

public class CacheProperties {
    private long maximumSize = 500L;
    private long expireSeconds = 3600L;

    public static CachePropertiesBuilder builder() {
        return new CachePropertiesBuilder();
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public long getMaximumSize() {
        return this.maximumSize;
    }

    public long getExpireSeconds() {
        return this.expireSeconds;
    }

    public CacheProperties() {
    }

    public CacheProperties(long maximumSize, long expireSeconds) {
        this.maximumSize = maximumSize;
        this.expireSeconds = expireSeconds;
    }

    public static class CachePropertiesBuilder {
        private long maximumSize;
        private long expireSeconds;

        CachePropertiesBuilder() {
        }

        public CachePropertiesBuilder maximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }

        public CachePropertiesBuilder expireSeconds(long expireSeconds) {
            this.expireSeconds = expireSeconds;
            return this;
        }

        public CacheProperties build() {
            return new CacheProperties(this.maximumSize, this.expireSeconds);
        }

        public String toString() {
            return "CacheProperties.CachePropertiesBuilder(maximumSize=" + this.maximumSize + ", expireSeconds=" + this.expireSeconds + ")";
        }
    }

}

