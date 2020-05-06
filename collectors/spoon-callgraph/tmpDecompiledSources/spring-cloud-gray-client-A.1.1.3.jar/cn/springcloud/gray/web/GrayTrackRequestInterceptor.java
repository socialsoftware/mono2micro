/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.MapUtils
 */
package cn.springcloud.gray.web;

import cn.springcloud.gray.RequestInterceptor;
import cn.springcloud.gray.request.GrayHttpRequest;
import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.collections.MapUtils;

public class GrayTrackRequestInterceptor
implements RequestInterceptor {
    private List<Consumer<GrayHttpRequest>> handlers = new ArrayList<Consumer<GrayHttpRequest>>();

    public GrayTrackRequestInterceptor() {
        this.initHandlers();
    }

    @Override
    public String interceptroType() {
        return "all";
    }

    @Override
    public boolean shouldIntercept() {
        return true;
    }

    @Override
    public boolean pre(GrayRequest request) {
        GrayHttpRequest grayHttpRequest = (GrayHttpRequest)request;
        GrayHttpTrackInfo grayHttpTrackInfo = (GrayHttpTrackInfo)request.getGrayTrackInfo();
        if (grayHttpTrackInfo != null) {
            this.handlers.forEach(h -> h.accept(grayHttpRequest));
        }
        return true;
    }

    @Override
    public boolean after(GrayRequest request) {
        return true;
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private void initHandlers() {
        this.handlers.add(request -> {
            GrayHttpTrackInfo grayHttpTrackInfo = (GrayHttpTrackInfo)request.getGrayTrackInfo();
            if (MapUtils.isNotEmpty(grayHttpTrackInfo.getAttributes())) {
                grayHttpTrackInfo.getAttributes().entrySet().forEach(entry -> {
                    String name = "_g_t_attr" + "__" + (String)entry.getKey();
                    request.addHeader(name, (String)entry.getValue());
                });
            }
        });
        this.handlers.add(request -> {
            GrayHttpTrackInfo grayHttpTrackInfo = (GrayHttpTrackInfo)request.getGrayTrackInfo();
            if (MapUtils.isNotEmpty(grayHttpTrackInfo.getHeaders())) {
                Map<String, List<String>> h = request.getHeaders();
                grayHttpTrackInfo.getHeaders().entrySet().forEach(entry -> {
                    String name = "_g_t_header" + "__" + (String)entry.getKey();
                    h.put(name, (List<String>)entry.getValue());
                });
            }
        });
        this.handlers.add(request -> {
            GrayHttpTrackInfo grayHttpTrackInfo = (GrayHttpTrackInfo)request.getGrayTrackInfo();
            if (MapUtils.isNotEmpty(grayHttpTrackInfo.getParameters())) {
                Map<String, List<String>> h = request.getHeaders();
                grayHttpTrackInfo.getParameters().entrySet().forEach(entry -> {
                    String name = "_g_t_param" + "__" + (String)entry.getKey();
                    h.put(name, (List<String>)entry.getValue());
                });
            }
        });
    }
}

