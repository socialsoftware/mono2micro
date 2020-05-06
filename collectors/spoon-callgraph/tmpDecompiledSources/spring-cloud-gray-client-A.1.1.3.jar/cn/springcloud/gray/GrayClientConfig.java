/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray;

public interface GrayClientConfig {
    public String runenv();

    public boolean isGrayEnroll();

    public int grayEnrollDealyTimeInMs();

    public int getServiceUpdateIntervalTimerInMs();

    public int getServiceInitializeDelayTimeInMs();
}

