/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.client.config;

import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.concurrent.aspect.ExecutorGrayAspect;
import cn.springcloud.gray.concurrent.aspect.ExecutorServiceGrayAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(value={GrayManager.class})
@ConditionalOnProperty(value={"gray.client.threadpool.transparent-local-store.enabled"})
public class GrayConcurrnetConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ExecutorServiceGrayAspect executorServiceGrayAspect() {
        return new ExecutorServiceGrayAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExecutorGrayAspect executorGrayAspect() {
        return new ExecutorGrayAspect();
    }
}

