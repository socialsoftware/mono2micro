/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.request;

import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.RequestLocalStorage;

public class ThreadLocalRequestStorage
implements RequestLocalStorage {
    private ThreadLocal<GrayRequest> grayRequestThreadLocal = new ThreadLocal();
    private ThreadLocal<GrayTrackInfo> grayTrackInfoThreadLocal = new ThreadLocal();

    @Override
    public void setGrayTrackInfo(GrayTrackInfo grayTrackInfo) {
        this.grayTrackInfoThreadLocal.set(grayTrackInfo);
    }

    @Override
    public void removeGrayTrackInfo() {
        this.grayTrackInfoThreadLocal.remove();
    }

    @Override
    public GrayTrackInfo getGrayTrackInfo() {
        return this.grayTrackInfoThreadLocal.get();
    }

    @Override
    public void setGrayRequest(GrayRequest grayRequest) {
        this.grayRequestThreadLocal.set(grayRequest);
    }

    @Override
    public void removeGrayRequest() {
        this.grayRequestThreadLocal.remove();
    }

    @Override
    public GrayRequest getGrayRequest() {
        return this.grayRequestThreadLocal.get();
    }
}

