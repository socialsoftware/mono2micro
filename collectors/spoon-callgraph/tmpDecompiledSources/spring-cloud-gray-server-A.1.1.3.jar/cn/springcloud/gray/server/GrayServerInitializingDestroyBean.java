/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.concurrent.DefaultThreadFactory
 *  org.springframework.beans.BeansException
 *  org.springframework.beans.factory.InitializingBean
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 */
package cn.springcloud.gray.server;

import cn.springcloud.gray.concurrent.DefaultThreadFactory;
import cn.springcloud.gray.server.configuration.properties.GrayServerProperties;
import cn.springcloud.gray.server.manager.GrayServiceManager;
import cn.springcloud.gray.server.module.gray.GrayInstanceRecordEvictor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class GrayServerInitializingDestroyBean
implements InitializingBean,
ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(GrayServerInitializingDestroyBean.class);
    private GrayServiceManager grayServiceManager;
    private GrayServerProperties grayServerProperties;
    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, (ThreadFactory)new DefaultThreadFactory("initDestory"));
    private ApplicationContext appCxt;

    public GrayServerInitializingDestroyBean(GrayServiceManager grayServiceManager, GrayServerProperties grayServerProperties) {
        this.grayServiceManager = grayServiceManager;
        this.grayServerProperties = grayServerProperties;
    }

    public void afterPropertiesSet() {
        this.initToWork();
    }

    private void initToWork() {
        this.grayServiceManager.openForWork();
        this.initGrayInstanceRecordEvictionTask();
    }

    @PreDestroy
    public void shutdown() {
        this.grayServiceManager.shutdown();
        this.scheduledExecutorService.shutdown();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appCxt = applicationContext;
    }

    private void initGrayInstanceRecordEvictionTask() {
        GrayServerProperties.InstanceRecordEvictProperties evictProperties = this.grayServerProperties.getInstance().getEviction();
        if (!evictProperties.isEnabled()) {
            return;
        }
        String beanName = "grayInstanceRecordEvictor";
        GrayInstanceRecordEvictor instanceRecordEvictor = this.getBean(beanName, GrayInstanceRecordEvictor.class);
        if (instanceRecordEvictor == null) {
            log.error("\u6ca1\u6709\u627e\u5230\u540d\u4e3a{}\u7684GrayInstanceRecordEvictor\u7c7b\u578b\u6216\u8005\u4e3aGrayInstanceRecordEvictor\u7c7b\u578b\u7684\u5b9e\u4f8b", (Object)beanName);
            throw new NullPointerException("\u6ca1\u6709\u627e\u5230GrayInstanceRecordEvictor\u7c7b\u578b\u7684\u5b9e\u4f8b");
        }
        this.scheduledExecutorService.schedule(() -> instanceRecordEvictor.evict(), evictProperties.getEvictionIntervalTimerInMs(), TimeUnit.MILLISECONDS);
    }

    private <T> T getBean(String beanName, Class<T> cls) {
        Object t = null;
        try {
            t = this.appCxt.getBean(beanName, cls);
        }
        catch (BeansException e) {
            log.warn("\u6ca1\u6709\u4ecespring\u5bb9\u5668\u4e2d\u627e\u5230name\u4e3a'{}', class\u4e3a'{}'\u7684Bean", (Object)beanName, (Object)cls);
        }
        if (t == null) {
            t = this.appCxt.getBean(cls);
        }
        return (T)t;
    }
}

