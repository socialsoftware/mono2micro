/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.web.tracker;

import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.TrackArgs;
import cn.springcloud.gray.web.HttpRequest;
import cn.springcloud.gray.web.tracker.HttpGrayInfoTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpMethodGrayInfoTracker
implements HttpGrayInfoTracker {
    private static final Logger log = LoggerFactory.getLogger(HttpMethodGrayInfoTracker.class);

    public void call(GrayHttpTrackInfo trackInfo, HttpRequest request) {
        trackInfo.setMethod(request.getMethod());
        log.debug("\u8bb0\u5f55\u4e0b\u65b9\u6cd5:{}", (Object)request.getMethod());
    }

    @Override
    public void call(TrackArgs<GrayHttpTrackInfo, HttpRequest> args) {
        this.call(args.getTrackInfo(), args.getRequest());
    }
}

