/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.routing.connectionpoint;

import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.RequestInterceptor;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.LocalStorageLifeCycle;
import cn.springcloud.gray.request.RequestLocalStorage;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectPointContext;
import cn.springcloud.gray.routing.connectionpoint.RoutingConnectionPoint;
import java.util.List;
import java.util.function.Consumer;

public class DefaultRoutingConnectionPoint
implements RoutingConnectionPoint {
    private GrayManager grayManager;
    private RequestLocalStorage requestLocalStorage;
    private LocalStorageLifeCycle localStorageLifeCycle;

    public DefaultRoutingConnectionPoint(GrayManager grayManager, RequestLocalStorage requestLocalStorage, LocalStorageLifeCycle localStorageLifeCycle) {
        this.grayManager = grayManager;
        this.requestLocalStorage = requestLocalStorage;
        this.localStorageLifeCycle = localStorageLifeCycle;
    }

    @Override
    public void executeConnectPoint(RoutingConnectPointContext connectPointContext) {
        this.localStorageLifeCycle.initContext();
        RoutingConnectPointContext.setContextLocal(connectPointContext);
        GrayRequest grayRequest = connectPointContext.getGrayRequest();
        GrayTrackInfo grayTrackInfo = this.requestLocalStorage.getGrayTrackInfo();
        grayRequest.setGrayTrackInfo(grayTrackInfo);
        this.requestLocalStorage.setGrayRequest(grayRequest);
        List<RequestInterceptor> interceptors = this.grayManager.getRequeestInterceptors(connectPointContext.getInterceptroType());
        interceptors.forEach(interceptor -> {
            if (interceptor.shouldIntercept() && !interceptor.pre(grayRequest)) {
                return;
            }
        });
    }

    @Override
    public void shutdownconnectPoint(RoutingConnectPointContext connectPointContext) {
        try {
            List<RequestInterceptor> interceptors = this.grayManager.getRequeestInterceptors(connectPointContext.getInterceptroType());
            interceptors.forEach(interceptor -> {
                if (interceptor.shouldIntercept() && !interceptor.after(connectPointContext.getGrayRequest())) {
                    return;
                }
            });
            RoutingConnectPointContext.removeContextLocal();
            this.requestLocalStorage.removeGrayRequest();
        }
        finally {
            this.localStorageLifeCycle.closeContext();
        }
    }
}

