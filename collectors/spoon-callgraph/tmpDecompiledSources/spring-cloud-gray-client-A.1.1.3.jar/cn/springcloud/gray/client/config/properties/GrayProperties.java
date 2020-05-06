/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package cn.springcloud.gray.client.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value="gray")
public class GrayProperties {
    private boolean enabled;
    private boolean grayRouting = true;

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isGrayRouting() {
        return this.grayRouting;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setGrayRouting(boolean grayRouting) {
        this.grayRouting = grayRouting;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GrayProperties)) {
            return false;
        }
        GrayProperties other = (GrayProperties)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isEnabled() != other.isEnabled()) {
            return false;
        }
        return this.isGrayRouting() == other.isGrayRouting();
    }

    protected boolean canEqual(Object other) {
        return other instanceof GrayProperties;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isEnabled() ? 79 : 97);
        result = result * 59 + (this.isGrayRouting() ? 79 : 97);
        return result;
    }

    public String toString() {
        return "GrayProperties(enabled=" + this.isEnabled() + ", grayRouting=" + this.isGrayRouting() + ")";
    }
}

