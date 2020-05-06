/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.concurrent.GrayCallable
 *  cn.springcloud.gray.concurrent.GrayConcurrentHelper
 *  com.netflix.hystrix.HystrixThreadPoolKey
 *  com.netflix.hystrix.HystrixThreadPoolProperties
 *  com.netflix.hystrix.strategy.HystrixPlugins
 *  com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy
 *  com.netflix.hystrix.strategy.concurrency.HystrixRequestVariable
 *  com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableLifecycle
 *  com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier
 *  com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook
 *  com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher
 *  com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy
 *  com.netflix.hystrix.strategy.properties.HystrixProperty
 */
package cn.springcloud.gray.client.netflix.hystrix;

import cn.springcloud.gray.concurrent.GrayCallable;
import cn.springcloud.gray.concurrent.GrayConcurrentHelper;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariable;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableLifecycle;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GrayHystrixContextConcurrencyStrategy
extends HystrixConcurrencyStrategy {
    private HystrixConcurrencyStrategy delegate = HystrixPlugins.getInstance().getConcurrencyStrategy();

    public GrayHystrixContextConcurrencyStrategy() {
        if (this.delegate instanceof GrayHystrixContextConcurrencyStrategy) {
            return;
        }
        HystrixCommandExecutionHook commandExecutionHook = HystrixPlugins.getInstance().getCommandExecutionHook();
        HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
        HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance().getMetricsPublisher();
        HystrixPropertiesStrategy propertiesStrategy = HystrixPlugins.getInstance().getPropertiesStrategy();
        HystrixPlugins.reset();
        HystrixPlugins.getInstance().registerConcurrencyStrategy((HystrixConcurrencyStrategy)this);
        HystrixPlugins.getInstance().registerCommandExecutionHook(commandExecutionHook);
        HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
        HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
        HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
    }

    public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
        return this.delegate.getBlockingQueue(maxQueueSize);
    }

    public <T> HystrixRequestVariable<T> getRequestVariable(HystrixRequestVariableLifecycle<T> rv) {
        return this.delegate.getRequestVariable(rv);
    }

    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixProperty<Integer> corePoolSize, HystrixProperty<Integer> maximumPoolSize, HystrixProperty<Integer> keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        return this.delegate.getThreadPool(threadPoolKey, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolProperties threadPoolProperties) {
        return this.delegate.getThreadPool(threadPoolKey, threadPoolProperties);
    }

    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        Callable delegateWrapCallable = this.delegate.wrapCallable(callable);
        if (delegateWrapCallable instanceof GrayCallable) {
            return delegateWrapCallable;
        }
        return GrayConcurrentHelper.createDelegateCallable((Callable)delegateWrapCallable);
    }
}

