/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.Ordered
 */
package cn.springcloud.gray.request;

import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.TrackArgs;
import cn.springcloud.gray.utils.NameUtils;
import org.springframework.core.Ordered;

public interface GrayInfoTracker<TRACK extends GrayTrackInfo, REQ>
extends Ordered {
    public void call(TrackArgs<TRACK, REQ> var1);

    default public int getOrder() {
        return Integer.MAX_VALUE;
    }

    default public String name() {
        return NameUtils.normalizeName(this.getClass(), GrayInfoTracker.class);
    }
}

