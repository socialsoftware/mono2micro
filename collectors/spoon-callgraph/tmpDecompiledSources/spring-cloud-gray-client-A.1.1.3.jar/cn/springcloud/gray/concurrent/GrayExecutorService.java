/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.concurrent;

import cn.springcloud.gray.concurrent.GrayConcurrentHelper;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GrayExecutorService
implements ExecutorService {
    private ExecutorService delegater;

    public GrayExecutorService(ExecutorService delegater) {
        this.delegater = delegater;
    }

    @Override
    public void shutdown() {
        this.delegater.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return this.delegater.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.delegater.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.delegater.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegater.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return this.delegater.submit(GrayConcurrentHelper.createDelegateCallable(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return this.delegater.submit(GrayConcurrentHelper.createDelegateRunnable(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return this.delegater.submit(GrayConcurrentHelper.createDelegateRunnable(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        if (GrayConcurrentHelper.getGrayTrackInfo() != null) {
            return this.delegater.invokeAll(GrayConcurrentHelper.mapDelegateCallables(tasks));
        }
        return this.delegater.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        if (GrayConcurrentHelper.getGrayTrackInfo() != null) {
            return this.delegater.invokeAll(GrayConcurrentHelper.mapDelegateCallables(tasks), timeout, unit);
        }
        return this.delegater.invokeAll(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        if (GrayConcurrentHelper.getGrayTrackInfo() != null) {
            return (T)this.delegater.invokeAny(GrayConcurrentHelper.mapDelegateCallables(tasks));
        }
        return this.delegater.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (GrayConcurrentHelper.getGrayTrackInfo() != null) {
            return (T)this.delegater.invokeAny(GrayConcurrentHelper.mapDelegateCallables(tasks), timeout, unit);
        }
        return this.delegater.invokeAny(tasks);
    }

    @Override
    public void execute(Runnable command) {
        this.delegater.execute(GrayConcurrentHelper.createDelegateRunnable(command));
    }

    public ExecutorService getDelegater() {
        return this.delegater;
    }
}

