/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.request.GrayHttpTrackInfo
 *  cn.springcloud.gray.request.GrayTrackInfo
 *  cn.springcloud.gray.request.RequestLocalStorage
 *  cn.springcloud.gray.request.track.GrayTrackHolder
 *  javax.servlet.Filter
 *  javax.servlet.FilterChain
 *  javax.servlet.FilterConfig
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletRequest
 */
package cn.springcloud.gray.web;

import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.RequestLocalStorage;
import cn.springcloud.gray.request.track.GrayTrackHolder;
import cn.springcloud.gray.web.ServletHttpRequestWrapper;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class GrayTrackFilter
implements Filter {
    private RequestLocalStorage requestLocalStorage;
    private GrayTrackHolder grayTrackHolder;

    public GrayTrackFilter(GrayTrackHolder grayTrackHolder, RequestLocalStorage requestLocalStorage) {
        this.grayTrackHolder = grayTrackHolder;
        this.requestLocalStorage = requestLocalStorage;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        GrayHttpTrackInfo webTrack = new GrayHttpTrackInfo();
        this.grayTrackHolder.recordGrayTrack((GrayTrackInfo)webTrack, (Object)new ServletHttpRequestWrapper((HttpServletRequest)request));
        this.requestLocalStorage.setGrayTrackInfo((GrayTrackInfo)webTrack);
        try {
            chain.doFilter(request, response);
        }
        finally {
            this.requestLocalStorage.removeGrayTrackInfo();
        }
    }

    public void destroy() {
    }
}

