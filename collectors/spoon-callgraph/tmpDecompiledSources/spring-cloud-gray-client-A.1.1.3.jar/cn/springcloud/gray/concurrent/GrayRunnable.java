/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.concurrent;

import cn.springcloud.gray.concurrent.GrayConcurrentHelper;
import cn.springcloud.gray.concurrent.GrayRunnableContext;

public class GrayRunnable
implements Runnable {
    private GrayRunnableContext context;

    public GrayRunnable(GrayRunnableContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        GrayConcurrentHelper.initRequestLocalStorageContext(this.context);
        try {
            this.context.getTarget().run();
        }
        finally {
            GrayConcurrentHelper.cleanRequestLocalStorageContext(this.context);
        }
    }
}

