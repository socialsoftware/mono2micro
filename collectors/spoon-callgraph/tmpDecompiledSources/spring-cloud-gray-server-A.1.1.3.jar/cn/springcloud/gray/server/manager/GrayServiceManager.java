/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.manager;

import cn.springcloud.gray.server.module.gray.GrayServerModule;

public interface GrayServiceManager {
    public GrayServerModule getGrayServerModule();

    public void openForWork();

    public void shutdown();
}

