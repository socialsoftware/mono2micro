/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.routing.connectionpoint.RoutingConnectPointContext
 *  cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint
 *  com.netflix.zuul.ZuulFilter
 */
package cn.springcloud.gray.client.netflix.zuul;

import cn.springcloud.gray.routing.connectionpoint.RoutingConnectPointContext;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint;
import com.netflix.zuul.ZuulFilter;

public class GrayPostZuulFilter
extends ZuulFilter {
    private RoutingConnectionPoint routingConnectionPoint;

    public GrayPostZuulFilter(RoutingConnectionPoint routingConnectionPoint) {
        this.routingConnectionPoint = routingConnectionPoint;
    }

    public String filterType() {
        return "post";
    }

    public int filterOrder() {
        return 0;
    }

    public boolean shouldFilter() {
        return true;
    }

    public Object run() {
        RoutingConnectPointContext cpc = RoutingConnectPointContext.getContextLocal();
        if (cpc != null) {
            this.routingConnectionPoint.shutdownconnectPoint(cpc);
        }
        return null;
    }
}

