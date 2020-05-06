/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.BeansException
 *  org.springframework.beans.factory.InitializingBean
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 */
package cn.springcloud.gray;

import cn.springcloud.gray.GrayClientHolder;
import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.RequestInterceptor;
import cn.springcloud.gray.ServerChooser;
import cn.springcloud.gray.UpdateableGrayManager;
import cn.springcloud.gray.client.switcher.GraySwitcher;
import cn.springcloud.gray.local.InstanceLocalInfo;
import cn.springcloud.gray.local.InstanceLocalInfoInitiralizer;
import cn.springcloud.gray.request.LocalStorageLifeCycle;
import cn.springcloud.gray.request.RequestLocalStorage;
import cn.springcloud.gray.servernode.ServerExplainer;
import cn.springcloud.gray.servernode.ServerListProcessor;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class GrayClientInitializer
implements ApplicationContextAware,
InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(GrayClientInitializer.class);
    private ApplicationContext cxt;

    public void afterPropertiesSet() throws Exception {
        GrayClientHolder.setGrayManager(this.getBean("grayManager", GrayManager.class));
        GrayClientHolder.setRequestLocalStorage(this.getBean("requestLocalStorage", RequestLocalStorage.class));
        GrayClientHolder.setLocalStorageLifeCycle(this.getBean("localStorageLifeCycle", LocalStorageLifeCycle.class));
        GrayClientHolder.setServerExplainer(this.getBean("serverExplainer", ServerExplainer.class));
        GrayClientHolder.setServerListProcessor(this.getBean("serverListProcessor", ServerListProcessor.class, new ServerListProcessor.Default()));
        GrayClientHolder.setGraySwitcher(this.getBean("graySwitcher", GraySwitcher.class, new GraySwitcher.DefaultGraySwitcher()));
        GrayClientHolder.setServerChooser(this.getBean("serverChooser", ServerChooser.class));
        this.initGrayManagerRequestInterceptors();
        this.loadInstanceLocalInfo();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.cxt = applicationContext;
    }

    private void loadInstanceLocalInfo() {
        InstanceLocalInfoInitiralizer instanceLocalInfoInitiralizer = this.getBean("instanceLocalInfoInitiralizer", InstanceLocalInfoInitiralizer.class);
        if (instanceLocalInfoInitiralizer == null) {
            return;
        }
        GrayClientHolder.setInstanceLocalInfo(instanceLocalInfoInitiralizer.getInstanceLocalInfo());
    }

    private <T> T getBean(String beanName, Class<T> cls) {
        Object t = null;
        try {
            t = this.cxt.getBean(beanName, cls);
        }
        catch (BeansException e) {
            log.warn("\u6ca1\u6709\u4ecespring\u5bb9\u5668\u4e2d\u627e\u5230name\u4e3a'{}', class\u4e3a'{}'\u7684Bean", (Object)beanName, (Object)cls);
        }
        if (t == null) {
            t = this.cxt.getBean(cls);
        }
        return (T)t;
    }

    private <T> T getBean(String beanName, Class<T> cls, T defaultBean) {
        try {
            return this.getBean(beanName, cls);
        }
        catch (BeansException e) {
            log.warn("\u6ca1\u6709\u4ecespring\u5bb9\u5668\u4e2d\u627e\u5230name\u4e3a'{}', class\u4e3a'{}'\u7684Bean, \u8fd4\u56de\u9ed8\u8ba4\u7684bean:{}", beanName, cls, defaultBean);
            return defaultBean;
        }
    }

    private <T> T getBeanNullable(String beanName, Class<T> cls) {
        try {
            return this.getBean(beanName, cls);
        }
        catch (BeansException e) {
            log.warn("\u6ca1\u6709\u4ecespring\u5bb9\u5668\u4e2d\u627e\u5230name\u4e3a'{}', class\u4e3a'{}'\u7684Bean", (Object)beanName, (Object)cls);
            return null;
        }
    }

    private void initGrayManagerRequestInterceptors() {
        Map requestInterceptors = this.cxt.getBeansOfType(RequestInterceptor.class);
        GrayManager grayManager = GrayClientHolder.getGrayManager();
        if (grayManager instanceof UpdateableGrayManager) {
            ((UpdateableGrayManager)grayManager).setRequestInterceptors(requestInterceptors.values());
        }
        grayManager.setup();
    }
}

