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
 *  org.springframework.http.HttpHeaders
 *  org.springframework.http.HttpRequest
 */
package cn.springcloud.gray.client.netflix.resttemplate;

import cn.springcloud.gray.RequestInterceptor;
import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.HttpGrayTrackRecordDevice;
import cn.springcloud.gray.request.HttpGrayTrackRecordHelper;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;

public class RestTemplateRequestInterceptor
implements RequestInterceptor {
    public String interceptroType() {
        return "rest";
    }

    public boolean shouldIntercept() {
        return true;
    }

    public boolean pre(GrayRequest request) {
        GrayHttpTrackInfo grayTrack = (GrayHttpTrackInfo)request.getGrayTrackInfo();
        if (grayTrack != null) {
            HttpRequest httpRequest = (HttpRequest)request.getAttribute("restTemplate.request");
            HttpHeaders httpHeaders = httpRequest.getHeaders();
            HttpGrayTrackRecordHelper.record((HttpGrayTrackRecordDevice)new RestTemplateHttpGrayTrackRecordDevice(httpHeaders), (GrayTrackInfo)grayTrack);
        }
        return true;
    }

    public boolean after(GrayRequest request) {
        return true;
    }

    public static class RestTemplateHttpGrayTrackRecordDevice
    implements HttpGrayTrackRecordDevice {
        private HttpHeaders httpHeaders;

        public RestTemplateHttpGrayTrackRecordDevice(HttpHeaders httpHeaders) {
            this.httpHeaders = httpHeaders;
        }

        public void record(String name, String value) {
            this.httpHeaders.set(name, value);
        }

        public void record(String name, List<String> values) {
            this.httpHeaders.put(name, values);
        }
    }

}

