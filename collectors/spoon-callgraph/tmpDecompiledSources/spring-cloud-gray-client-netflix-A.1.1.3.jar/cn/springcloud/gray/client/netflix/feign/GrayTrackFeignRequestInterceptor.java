/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.request.GrayHttpTrackInfo
 *  cn.springcloud.gray.request.GrayTrackInfo
 *  cn.springcloud.gray.request.HttpGrayTrackRecordDevice
 *  cn.springcloud.gray.request.HttpGrayTrackRecordHelper
 *  cn.springcloud.gray.request.RequestLocalStorage
 *  feign.RequestInterceptor
 *  feign.RequestTemplate
 */
package cn.springcloud.gray.client.netflix.feign;

import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.HttpGrayTrackRecordDevice;
import cn.springcloud.gray.request.HttpGrayTrackRecordHelper;
import cn.springcloud.gray.request.RequestLocalStorage;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrayTrackFeignRequestInterceptor
implements RequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(GrayTrackFeignRequestInterceptor.class);
    private RequestLocalStorage requestLocalStorage;

    public GrayTrackFeignRequestInterceptor(RequestLocalStorage requestLocalStorage) {
        this.requestLocalStorage = requestLocalStorage;
    }

    public void apply(RequestTemplate template) {
        GrayHttpTrackInfo grayTrack = this.getGrayHttpTrackInfo(template);
        if (grayTrack == null) {
            return;
        }
        HttpGrayTrackRecordHelper.record((HttpGrayTrackRecordDevice)new FeignHttpGrayTrackRecordDevice(template), (GrayTrackInfo)grayTrack);
    }

    private GrayHttpTrackInfo getGrayHttpTrackInfo(RequestTemplate template) {
        try {
            return (GrayHttpTrackInfo)this.requestLocalStorage.getGrayTrackInfo();
        }
        catch (Exception e) {
            log.warn("\u4ecerequestLocalStorage\u4e2d\u83b7\u53d6GrayTrackInfo\u5bf9\u8c61\u5931\u8d25, url:{}", (Object)template.url(), (Object)e);
            return null;
        }
    }

    public static class FeignHttpGrayTrackRecordDevice
    implements HttpGrayTrackRecordDevice {
        private RequestTemplate template;

        public FeignHttpGrayTrackRecordDevice(RequestTemplate template) {
            this.template = template;
        }

        public void record(String name, String value) {
            this.template.header(name, new String[]{value});
        }

        public void record(String name, List<String> values) {
            this.template.header(name, values);
        }
    }

}

