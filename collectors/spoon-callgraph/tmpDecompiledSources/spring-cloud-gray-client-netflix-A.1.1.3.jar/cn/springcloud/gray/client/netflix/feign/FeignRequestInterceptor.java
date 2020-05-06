/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.RequestInterceptor
 *  cn.springcloud.gray.request.GrayHttpTrackInfo
 *  cn.springcloud.gray.request.GrayRequest
 *  cn.springcloud.gray.request.GrayTrackInfo
 *  feign.Request
 *  org.apache.commons.collections.MapUtils
 */
package cn.springcloud.gray.client.netflix.feign;

import cn.springcloud.gray.RequestInterceptor;
import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import feign.Request;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

public class FeignRequestInterceptor
implements RequestInterceptor {
    public String interceptroType() {
        return "feign";
    }

    public boolean shouldIntercept() {
        return true;
    }

    public boolean pre(GrayRequest request) {
        Request feignRequest = (Request)request.getAttribute("feign.request");
        GrayHttpTrackInfo grayTrack = (GrayHttpTrackInfo)request.getGrayTrackInfo();
        if (grayTrack != null) {
            Map grayAttributes;
            if (StringUtils.isNotEmpty(grayTrack.getUri())) {
                feignRequest.headers().put("_g_t_uri", Arrays.asList(grayTrack.getUri()));
            }
            if (StringUtils.isNotEmpty(grayTrack.getTraceIp())) {
                feignRequest.headers().put("_g_t_track_ip", Arrays.asList(grayTrack.getTraceIp()));
            }
            if (StringUtils.isNotEmpty(grayTrack.getMethod())) {
                feignRequest.headers().put("_g_t_method", Arrays.asList(grayTrack.getMethod()));
            }
            if (grayTrack.getParameters() != null && !grayTrack.getParameters().isEmpty()) {
                grayTrack.getParameters().entrySet().forEach(entry -> {
                    String name = "_g_t_param" + "__" + (String)entry.getKey();
                    feignRequest.headers().put(name, entry.getValue());
                });
            }
            if (grayTrack.getHeaders() != null && !grayTrack.getHeaders().isEmpty()) {
                grayTrack.getHeaders().entrySet().forEach(entry -> {
                    String name = "_g_t_header" + "__" + (String)entry.getKey();
                    feignRequest.headers().put(name, entry.getValue());
                });
            }
            if (MapUtils.isNotEmpty((Map)(grayAttributes = grayTrack.getAttributes()))) {
                grayAttributes.entrySet().forEach(entry -> {
                    String name = "_g_t_attr" + "__" + (String)entry.getKey();
                    feignRequest.headers().put(name, Arrays.asList((String)entry.getValue()));
                });
            }
        }
        return true;
    }

    public boolean after(GrayRequest request) {
        return true;
    }
}

