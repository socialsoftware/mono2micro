/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayTrackDefinition
 *  org.springframework.util.StringUtils
 */
package cn.springcloud.gray.web.tracker;

import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.TrackArgs;
import cn.springcloud.gray.web.HttpRequest;
import cn.springcloud.gray.web.tracker.HttpGrayInfoTracker;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class HttpParameterGrayInfoTracker
implements HttpGrayInfoTracker {
    private static final Logger log = LoggerFactory.getLogger(HttpParameterGrayInfoTracker.class);

    @Override
    public void call(TrackArgs<GrayHttpTrackInfo, HttpRequest> args) {
        GrayHttpTrackInfo trackInfo = args.getTrackInfo();
        HttpRequest request = args.getRequest();
        String defValue = args.getTrackDefinition().getValue();
        if (StringUtils.isEmpty((Object)defValue)) {
            return;
        }
        for (String name : defValue.split(",")) {
            String[] values = request.getParameterValues(name);
            if (!ArrayUtils.isNotEmpty(values)) continue;
            trackInfo.setParameters(name, Arrays.asList(values));
            log.debug("\u8bb0\u5f55\u4e0bparameter:{} -> {}", (Object)name, (Object)values);
        }
    }
}

