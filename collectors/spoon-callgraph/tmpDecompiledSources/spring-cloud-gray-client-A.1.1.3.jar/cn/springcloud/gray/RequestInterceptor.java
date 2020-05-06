/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.Ordered
 */
package cn.springcloud.gray;

import cn.springcloud.gray.request.GrayRequest;
import org.springframework.core.Ordered;

public interface RequestInterceptor
extends Ordered {
    public String interceptroType();

    public boolean shouldIntercept();

    public boolean pre(GrayRequest var1);

    public boolean after(GrayRequest var1);

    default public int getOrder() {
        return Integer.MAX_VALUE;
    }
}

