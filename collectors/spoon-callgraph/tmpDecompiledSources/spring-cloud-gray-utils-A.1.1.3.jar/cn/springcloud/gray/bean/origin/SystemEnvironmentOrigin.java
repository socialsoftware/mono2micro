/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.Assert
 *  org.springframework.util.ObjectUtils
 */
package cn.springcloud.gray.bean.origin;

import cn.springcloud.gray.bean.origin.Origin;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public class SystemEnvironmentOrigin
implements Origin {
    private final String property;

    public SystemEnvironmentOrigin(String property) {
        Assert.notNull((Object)property, (String)"Property name must not be null");
        Assert.hasText((String)property, (String)"Property name must not be empty");
        this.property = property;
    }

    public String getProperty() {
        return this.property;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        SystemEnvironmentOrigin other = (SystemEnvironmentOrigin)obj;
        return ObjectUtils.nullSafeEquals((Object)this.property, (Object)other.property);
    }

    public int hashCode() {
        return ObjectUtils.nullSafeHashCode((Object)this.property);
    }

    public String toString() {
        return "System Environment Property \"" + this.property + "\"";
    }
}

