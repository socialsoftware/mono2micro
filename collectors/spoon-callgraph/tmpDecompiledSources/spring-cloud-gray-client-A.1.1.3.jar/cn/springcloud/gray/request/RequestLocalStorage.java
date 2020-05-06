/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.request;

import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;

public interface RequestLocalStorage {
    public void setGrayTrackInfo(GrayTrackInfo var1);

    public void removeGrayTrackInfo();

    public GrayTrackInfo getGrayTrackInfo();

    public void setGrayRequest(GrayRequest var1);

    public void removeGrayRequest();

    public GrayRequest getGrayRequest();
}

