/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.origin;

import cn.springcloud.gray.bean.origin.OriginProvider;

public interface Origin {
    public static Origin from(Object source) {
        if (source instanceof Origin) {
            return (Origin)source;
        }
        Origin origin = null;
        if (source != null && source instanceof OriginProvider) {
            origin = ((OriginProvider)source).getOrigin();
        }
        if (origin == null && source != null && source instanceof Throwable) {
            return Origin.from(((Throwable)source).getCause());
        }
        return origin;
    }
}

