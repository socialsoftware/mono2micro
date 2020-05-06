/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.request.GrayRequest
 *  cn.springcloud.gray.request.GrayTrackInfo
 *  cn.springcloud.gray.request.RequestLocalStorage
 *  com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault
 */
package cn.springcloud.gray.client.netflix.hystrix;

import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.RequestLocalStorage;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

public class HystrixRequestLocalStorage
implements RequestLocalStorage {
    private static final HystrixRequestVariableDefault<GrayTrackInfo> grayTrackInfoLocal = new HystrixRequestVariableDefault();
    private static final HystrixRequestVariableDefault<GrayRequest> grayRequestLocal = new HystrixRequestVariableDefault();

    public void setGrayTrackInfo(GrayTrackInfo grayTrackInfo) {
        grayTrackInfoLocal.set((Object)grayTrackInfo);
    }

    public void removeGrayTrackInfo() {
        grayTrackInfoLocal.remove();
    }

    public GrayTrackInfo getGrayTrackInfo() {
        return (GrayTrackInfo)grayTrackInfoLocal.get();
    }

    public void setGrayRequest(GrayRequest grayRequest) {
        grayRequestLocal.set((Object)grayRequest);
    }

    public void removeGrayRequest() {
        grayRequestLocal.remove();
    }

    public GrayRequest getGrayRequest() {
        return (GrayRequest)grayRequestLocal.get();
    }
}

