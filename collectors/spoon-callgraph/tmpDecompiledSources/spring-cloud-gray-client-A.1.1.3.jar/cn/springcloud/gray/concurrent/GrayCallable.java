/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.concurrent;

import cn.springcloud.gray.concurrent.GrayCallableContext;
import cn.springcloud.gray.concurrent.GrayConcurrentHelper;
import java.util.concurrent.Callable;

public class GrayCallable<V>
implements Callable<V> {
    private GrayCallableContext context;

    public GrayCallable(GrayCallableContext context) {
        this.context = context;
    }

    @Override
    public V call() throws Exception {
        GrayConcurrentHelper.initRequestLocalStorageContext(this.context);
        try {
            Object obj = this.context.getTarget().call();
            return (V)obj;
        }
        finally {
            GrayConcurrentHelper.cleanRequestLocalStorageContext(this.context);
        }
    }
}

