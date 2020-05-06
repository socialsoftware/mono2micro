/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.concurrent;

import cn.springcloud.gray.GrayClientHolder;
import cn.springcloud.gray.concurrent.GrayAsyncContext;
import cn.springcloud.gray.concurrent.GrayCallable;
import cn.springcloud.gray.concurrent.GrayCallableContext;
import cn.springcloud.gray.concurrent.GrayRunnable;
import cn.springcloud.gray.concurrent.GrayRunnableContext;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.LocalStorageLifeCycle;
import cn.springcloud.gray.request.RequestLocalStorage;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrayConcurrentHelper {
    private static final Logger log = LoggerFactory.getLogger(GrayConcurrentHelper.class);

    private GrayConcurrentHelper() {
    }

    public static <V> Collection<Callable<V>> mapDelegateCallables(Collection<? extends Callable<V>> tasks) {
        return tasks.stream().map(GrayConcurrentHelper::createDelegateCallable).collect(Collectors.toList());
    }

    public static <V> Callable<V> createDelegateCallable(Callable<V> callable) {
        if (GrayConcurrentHelper.getGrayTrackInfo() != null) {
            return new GrayCallable(GrayConcurrentHelper.createGrayCallableContext(callable));
        }
        return callable;
    }

    public static Runnable createDelegateRunnable(Runnable runnable) {
        if (GrayConcurrentHelper.getGrayTrackInfo() != null) {
            return new GrayRunnable(GrayConcurrentHelper.createGrayRunnableContext(runnable));
        }
        return runnable;
    }

    public static GrayRunnableContext createGrayRunnableContext(Runnable runnable) {
        GrayRunnableContext context = new GrayRunnableContext();
        context.setLocalStorageLifeCycle(GrayClientHolder.getLocalStorageLifeCycle());
        context.setRequestLocalStorage(GrayClientHolder.getRequestLocalStorage());
        context.setGrayTrackInfo(GrayConcurrentHelper.getGrayTrackInfo());
        context.setGrayRequest(GrayConcurrentHelper.getGrayRequest());
        context.setTarget(runnable);
        return context;
    }

    public static <V> GrayCallableContext createGrayCallableContext(Callable<V> callable) {
        GrayCallableContext context = new GrayCallableContext();
        context.setRequestLocalStorage(GrayClientHolder.getRequestLocalStorage());
        context.setLocalStorageLifeCycle(GrayClientHolder.getLocalStorageLifeCycle());
        context.setGrayTrackInfo(GrayConcurrentHelper.getGrayTrackInfo());
        context.setGrayRequest(GrayConcurrentHelper.getGrayRequest());
        context.setTarget(callable);
        return context;
    }

    public static GrayTrackInfo getGrayTrackInfo() {
        RequestLocalStorage requestLocalStorage = GrayClientHolder.getRequestLocalStorage();
        try {
            return requestLocalStorage == null ? null : requestLocalStorage.getGrayTrackInfo();
        }
        catch (Exception e) {
            log.warn("\u83b7\u53d6GrayTrackInfo\u5931\u8d25, \u7ebf\u7a0b\u540d\u662f {}", (Object)Thread.currentThread().getName(), (Object)e);
            return null;
        }
    }

    public static GrayRequest getGrayRequest() {
        RequestLocalStorage requestLocalStorage = GrayClientHolder.getRequestLocalStorage();
        try {
            return requestLocalStorage == null ? null : requestLocalStorage.getGrayRequest();
        }
        catch (Exception e) {
            log.warn("\u83b7\u53d6GrayRequest\u5931\u8d25, \u7ebf\u7a0b\u540d\u662f {}", (Object)Thread.currentThread().getName(), (Object)e);
            return null;
        }
    }

    public static void initRequestLocalStorageContext(GrayAsyncContext context) {
        GrayTrackInfo grayTrackInfo = context.getGrayTrackInfo();
        LocalStorageLifeCycle localStorageLifeCycle = context.getLocalStorageLifeCycle();
        localStorageLifeCycle.initContext();
        RequestLocalStorage requestLocalStorage = context.getRequestLocalStorage();
        requestLocalStorage.setGrayTrackInfo(grayTrackInfo);
        if (context.getGrayRequest() != null && requestLocalStorage.getGrayRequest() == null) {
            requestLocalStorage.setGrayRequest(context.getGrayRequest());
        }
    }

    public static void cleanRequestLocalStorageContext(GrayAsyncContext context) {
        LocalStorageLifeCycle localStorageLifeCycle = context.getLocalStorageLifeCycle();
        RequestLocalStorage requestLocalStorage = context.getRequestLocalStorage();
        requestLocalStorage.removeGrayTrackInfo();
        requestLocalStorage.removeGrayRequest();
        localStorageLifeCycle.closeContext();
    }
}

