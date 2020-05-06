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
 *  feign.Client
 *  feign.Request
 *  feign.Request$Options
 *  feign.Response
 */
package cn.springcloud.gray.client.netflix.feign;

import cn.springcloud.gray.client.config.properties.GrayRequestProperties;
import cn.springcloud.gray.request.GrayHttpRequest;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectPointContext;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint;
import cn.springcloud.gray.utils.WebUtils;
import feign.Client;
import feign.Request;
import feign.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

class GrayFeignClientWrapper
implements Client {
    private Client delegate;
    private RoutingConnectionPoint routingConnectionPoint;
    private GrayRequestProperties grayRequestProperties;

    public GrayFeignClientWrapper(Client delegate, RoutingConnectionPoint routingConnectionPoint, GrayRequestProperties grayRequestProperties) {
        this.delegate = delegate;
        this.routingConnectionPoint = routingConnectionPoint;
        this.grayRequestProperties = grayRequestProperties;
    }

    public Response execute(Request request, Request.Options options) throws IOException {
        URI uri = URI.create(request.url());
        GrayHttpRequest grayRequest = new GrayHttpRequest();
        grayRequest.setUri(uri);
        grayRequest.setServiceId(uri.getHost());
        grayRequest.setParameters(WebUtils.getQueryParams((String)uri.getQuery()));
        grayRequest.addHeaders(request.headers());
        grayRequest.setMethod(request.method());
        if (this.grayRequestProperties.isLoadBody()) {
            grayRequest.setBody(request.body());
        }
        grayRequest.setAttribute("feign.request", (Object)request);
        grayRequest.setAttribute("feign.request.options", (Object)options);
        RoutingConnectPointContext connectPointContext = RoutingConnectPointContext.builder().interceptroType("feign").grayRequest((GrayRequest)grayRequest).build();
        return (Response)this.routingConnectionPoint.execute(connectPointContext, () -> this.delegate.execute(request, options));
    }

    public Client getTargetClient() {
        return this.delegate;
    }

    RoutingConnectionPoint getRoutingConnectionPoint() {
        return this.routingConnectionPoint;
    }

    GrayRequestProperties getGrayRequestProperties() {
        return this.grayRequestProperties;
    }
}

