/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayTrackDefinition
 *  org.springframework.util.CollectionUtils
 *  org.springframework.util.StringUtils
 */
package cn.springcloud.gray.web.tracker;

import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.TrackArgs;
import cn.springcloud.gray.web.HttpRequest;
import cn.springcloud.gray.web.tracker.HttpGrayInfoTracker;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class HttpHeaderGrayInfoTracker
implements HttpGrayInfoTracker {
    private static final Logger log = LoggerFactory.getLogger(HttpHeaderGrayInfoTracker.class);

    @Override
    public void call(TrackArgs<GrayHttpTrackInfo, HttpRequest> args) {
        GrayHttpTrackInfo trackInfo = args.getTrackInfo();
        HttpRequest request = args.getRequest();
        String defValue = args.getTrackDefinition().getValue();
        if (StringUtils.isEmpty((Object)defValue)) {
            return;
        }
        for (String header : defValue.split(",")) {
            Enumeration<String> headerValues = request.getHeaders(header);
            ArrayList<String> values = null;
            if (headerValues instanceof List) {
                values = (ArrayList<String>)((Object)headerValues);
            } else {
                values = new ArrayList<String>();
                while (headerValues.hasMoreElements()) {
                    String value = headerValues.nextElement();
                    values.add(value);
                }
            }
            if (CollectionUtils.isEmpty(values)) continue;
            log.debug("\u8bb0\u5f55\u4e0bheader:{} -> {}", (Object)header, (Object)values);
            trackInfo.setHeader(header, values);
        }
    }
}

