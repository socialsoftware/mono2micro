/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.evictor;

import cn.springcloud.gray.server.evictor.GrayServerEvictor;
import cn.springcloud.gray.server.module.gray.GrayServerModule;

public class NoActionGrayServerEvictor
implements GrayServerEvictor {
    public static NoActionGrayServerEvictor INSTANCE = new NoActionGrayServerEvictor();

    private NoActionGrayServerEvictor() {
    }

    @Override
    public void evict(GrayServerModule grayServerModule) {
    }
}

