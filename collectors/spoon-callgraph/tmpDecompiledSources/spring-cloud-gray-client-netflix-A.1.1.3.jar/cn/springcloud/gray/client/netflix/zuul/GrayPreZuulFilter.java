/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.client.config.properties.GrayRequestProperties
 *  cn.springcloud.gray.request.GrayHttpRequest
 *  cn.springcloud.gray.request.GrayRequest
 *  cn.springcloud.gray.routing.connectionpoint.RoutingConnectPointContext
 *  cn.springcloud.gray.routing.connectionpoint.RoutingConnectPointContext$RoutingConnectPointContextBuilder
 *  cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint
 *  com.netflix.zuul.ZuulFilter
 *  com.netflix.zuul.context.RequestContext
 *  javax.servlet.ServletInputStream
 *  javax.servlet.http.HttpServletRequest
 *  org.springframework.util.LinkedMultiValueMap
 *  org.springframework.util.MultiValueMap
 */
package cn.springcloud.gray.client.netflix.zuul;

import cn.springcloud.gray.client.config.properties.GrayRequestProperties;
import cn.springcloud.gray.request.GrayHttpRequest;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectPointContext;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class GrayPreZuulFilter
extends ZuulFilter {
    private static final Logger log = LoggerFactory.getLogger(GrayPreZuulFilter.class);
    public static final String GRAY_REQUEST_ATTRIBUTE_NAME_ZUUL_REQUEST = "zuul.request";
    public static final String GRAY_REQUEST_ATTRIBUTE_NAME_ZUUL_REQUEST_CONTEXT = "zuul.requestContext";
    private GrayRequestProperties grayRequestProperties;
    private RoutingConnectionPoint routingConnectionPoint;

    public GrayPreZuulFilter(GrayRequestProperties grayRequestProperties, RoutingConnectionPoint routingConnectionPoint) {
        this.grayRequestProperties = grayRequestProperties;
        this.routingConnectionPoint = routingConnectionPoint;
    }

    public String filterType() {
        return "pre";
    }

    public int filterOrder() {
        return 10000;
    }

    public boolean shouldFilter() {
        return true;
    }

    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest servletRequest = context.getRequest();
        String serviceId = (String)context.get((Object)"serviceId");
        if (StringUtils.isEmpty(serviceId)) {
            return null;
        }
        GrayHttpRequest grayRequest = new GrayHttpRequest();
        URI uri = URI.create((String)context.get((Object)"requestURI"));
        grayRequest.setUri(uri);
        grayRequest.setServiceId(serviceId);
        grayRequest.addParameters(context.getRequestQueryParams());
        if (this.grayRequestProperties.isLoadBody()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)context.getRequest().getInputStream()));
                byte[] reqBody = IOUtils.toByteArray(reader);
                grayRequest.setBody(reqBody);
            }
            catch (IOException e) {
                String errorMsg = "\u83b7\u53d6request body\u51fa\u73b0\u5f02\u5e38";
                log.error(errorMsg, e);
            }
        }
        grayRequest.setMethod(servletRequest.getMethod());
        grayRequest.setHeaders(this.getHeaders(context));
        grayRequest.setAttribute(GRAY_REQUEST_ATTRIBUTE_NAME_ZUUL_REQUEST, (Object)servletRequest);
        grayRequest.setAttribute(GRAY_REQUEST_ATTRIBUTE_NAME_ZUUL_REQUEST_CONTEXT, (Object)context);
        RoutingConnectPointContext connectPointContext = RoutingConnectPointContext.builder().interceptroType("zuul").grayRequest((GrayRequest)grayRequest).build();
        this.routingConnectionPoint.executeConnectPoint(connectPointContext);
        return null;
    }

    private MultiValueMap<String, String> getHeaders(RequestContext context) {
        LinkedMultiValueMap headers = new LinkedMultiValueMap();
        context.getZuulRequestHeaders().entrySet().forEach(arg_0 -> GrayPreZuulFilter.lambda$getHeaders$0((MultiValueMap)headers, arg_0));
        HttpServletRequest servletRequest = context.getRequest();
        Enumeration headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String)headerNames.nextElement();
            headers.add((Object)headerName, (Object)servletRequest.getHeader(headerName));
        }
        return headers;
    }

    private static /* synthetic */ void lambda$getHeaders$0(MultiValueMap headers, Map.Entry entry) {
        headers.add(entry.getKey(), entry.getValue());
    }
}

