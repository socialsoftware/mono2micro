/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.aspectj.lang.ProceedingJoinPoint
 *  org.aspectj.lang.annotation.Around
 *  org.aspectj.lang.annotation.Aspect
 *  org.aspectj.lang.annotation.Pointcut
 */
package cn.springcloud.gray.concurrent.aspect;

import cn.springcloud.gray.concurrent.GrayCallable;
import cn.springcloud.gray.concurrent.GrayConcurrentHelper;
import cn.springcloud.gray.concurrent.GrayExecutor;
import cn.springcloud.gray.concurrent.GrayExecutorService;
import cn.springcloud.gray.concurrent.GrayRunnable;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ExecutorServiceGrayAspect {
    @Pointcut(value="execution(* java.util.concurrent.ExecutorService.execute(..)) && args(command)")
    public void executePointCut(Runnable command) {
    }

    @Pointcut(value="execution(* java.util.concurrent.ExecutorService.submit(..)) && args(command)")
    public void submitPointCut(Callable command) {
    }

    @Pointcut(value="execution(* java.util.concurrent.ExecutorService.submit(..)) && args(command, result)")
    public void submitPointCut2(Runnable command, Object result) {
    }

    @Pointcut(value="execution(* java.util.concurrent.ExecutorService.submit(..)) && args(command)")
    public void submitPointCut3(Runnable command) {
    }

    @Pointcut(value="(execution(* java.util.concurrent.ExecutorService.invokeAll(..)) || execution(* java.util.concurrent.ExecutorService.invokeAny(..)) ) && args(tasks)")
    public <V> void invokeAllOrAnyPointCut(Collection<? extends Callable<V>> tasks) {
    }

    @Pointcut(value="(execution(* java.util.concurrent.ExecutorService.invokeAll(..)) || execution(* java.util.concurrent.ExecutorService.invokeAny(..))) && args(tasks, timeout, unit)")
    public <V> void invokeAllOrAnyPointCut2(Collection<? extends Callable<V>> tasks, long timeout, TimeUnit unit) {
    }

    @Around(value="executePointCut(command)")
    public Object executeAround(ProceedingJoinPoint joinpoint, Runnable command) throws Throwable {
        if (joinpoint.getTarget() instanceof GrayExecutor || joinpoint.getTarget() instanceof GrayExecutorService || command instanceof GrayRunnable) {
            return joinpoint.proceed();
        }
        Runnable runnable = GrayConcurrentHelper.createDelegateRunnable(command);
        return joinpoint.proceed(new Object[]{runnable});
    }

    @Around(value="submitPointCut(command)")
    public Object submitAround(ProceedingJoinPoint joinpoint, Callable command) throws Throwable {
        if (joinpoint.getTarget() instanceof GrayExecutorService || command instanceof GrayCallable) {
            return joinpoint.proceed();
        }
        Callable runnable = GrayConcurrentHelper.createDelegateCallable(command);
        return joinpoint.proceed(new Object[]{runnable});
    }

    @Around(value="submitPointCut2(command, result)")
    public Object submitAround(ProceedingJoinPoint joinpoint, Runnable command, Object result) throws Throwable {
        if (joinpoint.getTarget() instanceof GrayExecutorService || command instanceof GrayRunnable) {
            return joinpoint.proceed();
        }
        Runnable runnable = GrayConcurrentHelper.createDelegateRunnable(command);
        return joinpoint.proceed(new Object[]{runnable, result});
    }

    @Around(value="submitPointCut3(command)")
    public Object submitAround(ProceedingJoinPoint joinpoint, Runnable command) throws Throwable {
        if (joinpoint.getTarget() instanceof GrayExecutorService || command instanceof GrayRunnable) {
            return joinpoint.proceed();
        }
        Runnable runnable = GrayConcurrentHelper.createDelegateRunnable(command);
        return joinpoint.proceed(new Object[]{runnable});
    }

    @Around(value="invokeAllOrAnyPointCut(tasks)")
    public <V> Object invokeAllOrAnyAround(ProceedingJoinPoint joinpoint, Collection<? extends Callable<V>> tasks) throws Throwable {
        if (joinpoint.getTarget() instanceof GrayExecutorService) {
            return joinpoint.proceed();
        }
        return joinpoint.proceed(new Object[]{GrayConcurrentHelper.mapDelegateCallables(tasks)});
    }

    @Around(value="invokeAllOrAnyPointCut2(tasks, timeout, unit)")
    public <V> Object invokeAllOrAnyAround(ProceedingJoinPoint joinpoint, Collection<? extends Callable<V>> tasks, long timeout, TimeUnit unit) throws Throwable {
        if (joinpoint.getTarget() instanceof GrayExecutorService) {
            return joinpoint.proceed();
        }
        return joinpoint.proceed(new Object[]{GrayConcurrentHelper.mapDelegateCallables(tasks), timeout, unit});
    }
}

