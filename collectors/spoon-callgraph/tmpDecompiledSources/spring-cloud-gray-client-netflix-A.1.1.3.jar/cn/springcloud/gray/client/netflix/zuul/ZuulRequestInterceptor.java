/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.RequestInterceptor
 *  cn.springcloud.gray.request.GrayHttpTrackInfo
 *  cn.springcloud.gray.request.GrayRequest
 *  cn.springcloud.gray.request.GrayTrackInfo
 *  cn.springcloud.gray.request.HttpGrayTrackRecordDevice
 *  cn.springcloud.gray.request.HttpGrayTrackRecordHelper
 *  com.netflix.zuul.context.RequestContext
 */
package cn.springcloud.gray.client.netflix.zuul;

import cn.springcloud.gray.RequestInterceptor;
import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.HttpGrayTrackRecordDevice;
import cn.springcloud.gray.request.HttpGrayTrackRecordHelper;
import com.netflix.zuul.context.RequestContext;
import java.util.List;
import java.util.Map;

public class ZuulRequestInterceptor
implements RequestInterceptor {
    public String interceptroType() {
        return "zuul";
    }

    public boolean shouldIntercept() {
        return true;
    }

    public boolean pre(GrayRequest request) {
        GrayHttpTrackInfo grayTrack = (GrayHttpTrackInfo)request.getGrayTrackInfo();
        if (grayTrack != null) {
            RequestContext context = (RequestContext)request.getAttribute("zuul.requestContext");
            HttpGrayTrackRecordHelper.record((HttpGrayTrackRecordDevice)new ZuulHttpGrayTrackRecordDevice(context), (GrayTrackInfo)grayTrack);
        }
        return true;
    }

    public boolean after(GrayRequest request) {
        return true;
    }

    public static class ZuulHttpGrayTrackRecordDevice
    implements HttpGrayTrackRecordDevice {
        private RequestContext context;

        public ZuulHttpGrayTrackRecordDevice(RequestContext context) {
            this.context = context;
        }

        public void record(String name, String value) {
            this.context.getZuulRequestHeaders().put(name, value);
        }

        public void record(String name, List<String> values) {
            for (String v : values) {
                this.context.getZuulRequestHeaders().put(name, v);
            }
        }
    }

}

