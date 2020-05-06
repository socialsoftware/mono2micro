/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.client.config.properties.GrayRequestProperties
 *  cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint
 *  feign.Client
 *  feign.Request
 *  feign.Request$Options
 *  feign.Response
 *  org.springframework.beans.BeansException
 *  org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 *  org.springframework.core.env.Environment
 */
package cn.springcloud.gray.client.netflix.feign;

import cn.springcloud.gray.client.config.properties.GrayRequestProperties;
import cn.springcloud.gray.client.netflix.feign.GrayFeignClientWrapper;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint;
import feign.Client;
import feign.Request;
import feign.Response;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

public class GrayFeignClient
implements Client,
ApplicationContextAware {
    public static final String GRAY_REQUEST_ATTRIBUTE_NAME_FEIGN_REQUEST = "feign.request";
    public static final String GRAY_REQUEST_ATTRIBUTE_NAME_FEIGN_REQUEST_OPTIONS = "feign.request.options";
    private Client delegate;
    private RoutingConnectionPoint routingConnectionPoint;
    private GrayRequestProperties grayRequestProperties;
    private volatile GrayFeignClientWrapper proxy;
    private ApplicationContext applicationContext;

    public GrayFeignClient(Client delegate, RoutingConnectionPoint routingConnectionPoint, GrayRequestProperties grayRequestProperties) {
        this.delegate = delegate;
        this.routingConnectionPoint = routingConnectionPoint;
        this.grayRequestProperties = grayRequestProperties;
    }

    public Response execute(Request request, Request.Options options) throws IOException {
        return this.getProxyClient(request).execute(request, options);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Client getProxyClient(Request request) {
        if (this.proxy == null) {
            String serviceId = this.applicationContext.getEnvironment().getProperty("feign.client.name");
            boolean isLoadBalanced = StringUtils.equalsIgnoreCase(URI.create(request.url()).getHost(), serviceId);
            Client delegateClient = this.delegate;
            if (!isLoadBalanced && this.delegate instanceof LoadBalancerFeignClient) {
                delegateClient = ((LoadBalancerFeignClient)this.delegate).getDelegate();
            }
            this.proxy = new GrayFeignClientWrapper(delegateClient, this.routingConnectionPoint, this.grayRequestProperties);
        }
        return this.proxy;
    }
}

