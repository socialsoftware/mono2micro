/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.request.LocalStorageLifeCycle
 *  com.netflix.hystrix.strategy.concurrency.HystrixRequestContext
 */
package cn.springcloud.gray.client.netflix.hystrix;

import cn.springcloud.gray.request.LocalStorageLifeCycle;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

public class HystrixLocalStorageCycle
implements LocalStorageLifeCycle {
    private ThreadLocal<Boolean> hystrixRequestContextInitialized = new ThreadLocal();

    public void initContext() {
        if (!HystrixRequestContext.isCurrentThreadInitialized()) {
            HystrixRequestContext.initializeContext();
            this.hystrixRequestContextInitialized.set(true);
        }
    }

    public void closeContext() {
        Boolean hystrixReqCxtInited = this.hystrixRequestContextInitialized.get();
        if (hystrixReqCxtInited != null) {
            this.hystrixRequestContextInitialized.remove();
            if (hystrixReqCxtInited.booleanValue() && HystrixRequestContext.isCurrentThreadInitialized()) {
                HystrixRequestContext.getContextForCurrentThread().shutdown();
            }
        }
    }
}

