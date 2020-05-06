/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.concurrent;

import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.LocalStorageLifeCycle;
import cn.springcloud.gray.request.RequestLocalStorage;

public abstract class GrayAsyncContext {
    protected RequestLocalStorage requestLocalStorage;
    protected LocalStorageLifeCycle localStorageLifeCycle;
    protected GrayTrackInfo grayTrackInfo;
    protected GrayRequest grayRequest;

    public RequestLocalStorage getRequestLocalStorage() {
        return this.requestLocalStorage;
    }

    public LocalStorageLifeCycle getLocalStorageLifeCycle() {
        return this.localStorageLifeCycle;
    }

    public GrayTrackInfo getGrayTrackInfo() {
        return this.grayTrackInfo;
    }

    public GrayRequest getGrayRequest() {
        return this.grayRequest;
    }

    public void setRequestLocalStorage(RequestLocalStorage requestLocalStorage) {
        this.requestLocalStorage = requestLocalStorage;
    }

    public void setLocalStorageLifeCycle(LocalStorageLifeCycle localStorageLifeCycle) {
        this.localStorageLifeCycle = localStorageLifeCycle;
    }

    public void setGrayTrackInfo(GrayTrackInfo grayTrackInfo) {
        this.grayTrackInfo = grayTrackInfo;
    }

    public void setGrayRequest(GrayRequest grayRequest) {
        this.grayRequest = grayRequest;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GrayAsyncContext)) {
            return false;
        }
        GrayAsyncContext other = (GrayAsyncContext)o;
        if (!other.canEqual(this)) {
            return false;
        }
        RequestLocalStorage this$requestLocalStorage = this.getRequestLocalStorage();
        RequestLocalStorage other$requestLocalStorage = other.getRequestLocalStorage();
        if (this$requestLocalStorage == null ? other$requestLocalStorage != null : !this$requestLocalStorage.equals(other$requestLocalStorage)) {
            return false;
        }
        LocalStorageLifeCycle this$localStorageLifeCycle = this.getLocalStorageLifeCycle();
        LocalStorageLifeCycle other$localStorageLifeCycle = other.getLocalStorageLifeCycle();
        if (this$localStorageLifeCycle == null ? other$localStorageLifeCycle != null : !this$localStorageLifeCycle.equals(other$localStorageLifeCycle)) {
            return false;
        }
        GrayTrackInfo this$grayTrackInfo = this.getGrayTrackInfo();
        GrayTrackInfo other$grayTrackInfo = other.getGrayTrackInfo();
        if (this$grayTrackInfo == null ? other$grayTrackInfo != null : !this$grayTrackInfo.equals(other$grayTrackInfo)) {
            return false;
        }
        GrayRequest this$grayRequest = this.getGrayRequest();
        GrayRequest other$grayRequest = other.getGrayRequest();
        return !(this$grayRequest == null ? other$grayRequest != null : !this$grayRequest.equals(other$grayRequest));
    }

    protected boolean canEqual(Object other) {
        return other instanceof GrayAsyncContext;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        RequestLocalStorage $requestLocalStorage = this.getRequestLocalStorage();
        result = result * 59 + ($requestLocalStorage == null ? 43 : $requestLocalStorage.hashCode());
        LocalStorageLifeCycle $localStorageLifeCycle = this.getLocalStorageLifeCycle();
        result = result * 59 + ($localStorageLifeCycle == null ? 43 : $localStorageLifeCycle.hashCode());
        GrayTrackInfo $grayTrackInfo = this.getGrayTrackInfo();
        result = result * 59 + ($grayTrackInfo == null ? 43 : $grayTrackInfo.hashCode());
        GrayRequest $grayRequest = this.getGrayRequest();
        result = result * 59 + ($grayRequest == null ? 43 : $grayRequest.hashCode());
        return result;
    }

    public String toString() {
        return "GrayAsyncContext(requestLocalStorage=" + this.getRequestLocalStorage() + ", localStorageLifeCycle=" + this.getLocalStorageLifeCycle() + ", grayTrackInfo=" + this.getGrayTrackInfo() + ", grayRequest=" + this.getGrayRequest() + ")";
    }
}

