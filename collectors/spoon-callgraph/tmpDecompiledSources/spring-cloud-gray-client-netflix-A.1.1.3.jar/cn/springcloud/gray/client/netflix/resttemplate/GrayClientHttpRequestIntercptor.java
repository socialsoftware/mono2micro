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
 *  cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint$Supplier
 *  cn.springcloud.gray.utils.WebUtils
 *  org.springframework.http.HttpHeaders
 *  org.springframework.http.HttpMethod
 *  org.springframework.http.HttpRequest
 *  org.springframework.http.client.ClientHttpRequestExecution
 *  org.springframework.http.client.ClientHttpRequestInterceptor
 *  org.springframework.http.client.ClientHttpResponse
 */
package cn.springcloud.gray.client.netflix.resttemplate;

import cn.springcloud.gray.client.config.properties.GrayRequestProperties;
import cn.springcloud.gray.request.GrayHttpRequest;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectPointContext;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint;
import cn.springcloud.gray.utils.WebUtils;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class GrayClientHttpRequestIntercptor
implements ClientHttpRequestInterceptor {
    public static final String GRAY_REQUEST_ATTRIBUTE_RESTTEMPLATE_REQUEST = "restTemplate.request";
    private GrayRequestProperties grayRequestProperties;
    private RoutingConnectionPoint routingConnectionPoint;

    public GrayClientHttpRequestIntercptor(GrayRequestProperties grayRequestProperties, RoutingConnectionPoint routingConnectionPoint) {
        this.grayRequestProperties = grayRequestProperties;
        this.routingConnectionPoint = routingConnectionPoint;
    }

    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        URI uri = request.getURI();
        GrayHttpRequest grayRequest = new GrayHttpRequest();
        grayRequest.setUri(uri);
        grayRequest.setServiceId(uri.getHost());
        grayRequest.setParameters(WebUtils.getQueryParams((String)uri.getQuery()));
        if (this.grayRequestProperties.isLoadBody()) {
            grayRequest.setBody(body);
        }
        grayRequest.setMethod(request.getMethod().name());
        grayRequest.addHeaders((Map)request.getHeaders());
        grayRequest.setAttribute(GRAY_REQUEST_ATTRIBUTE_RESTTEMPLATE_REQUEST, (Object)request);
        RoutingConnectPointContext connectPointContext = RoutingConnectPointContext.builder().interceptroType("rest").grayRequest((GrayRequest)grayRequest).build();
        return (ClientHttpResponse)this.routingConnectionPoint.execute(connectPointContext, () -> execution.execute(request, body));
    }
}

