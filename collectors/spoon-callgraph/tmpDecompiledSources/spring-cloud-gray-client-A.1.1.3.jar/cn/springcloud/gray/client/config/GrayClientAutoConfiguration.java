/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.Cache
 *  com.github.benmanes.caffeine.cache.Cache
 *  com.github.benmanes.caffeine.cache.Caffeine
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.boot.context.properties.EnableConfigurationProperties
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Import
 */
package cn.springcloud.gray.client.config;

import cn.springcloud.gray.Cache;
import cn.springcloud.gray.CommunicableGrayManager;
import cn.springcloud.gray.DefaultGrayManager;
import cn.springcloud.gray.GrayClientConfig;
import cn.springcloud.gray.GrayClientInitializer;
import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.cache.CaffeineCache;
import cn.springcloud.gray.choose.DefaultGrayPredicate;
import cn.springcloud.gray.choose.GrayPredicate;
import cn.springcloud.gray.client.GrayClientEnrollInitializingDestroyBean;
import cn.springcloud.gray.client.config.GrayDecisionFactoryConfiguration;
import cn.springcloud.gray.client.config.GrayTrackConfiguration;
import cn.springcloud.gray.client.config.InformationClientConfiguration;
import cn.springcloud.gray.client.config.properties.CacheProperties;
import cn.springcloud.gray.client.config.properties.GrayClientProperties;
import cn.springcloud.gray.client.config.properties.GrayHoldoutServerProperties;
import cn.springcloud.gray.client.config.properties.GrayLoadProperties;
import cn.springcloud.gray.client.config.properties.GrayProperties;
import cn.springcloud.gray.client.config.properties.GrayRequestProperties;
import cn.springcloud.gray.client.config.properties.GrayServerProperties;
import cn.springcloud.gray.client.switcher.EnvGraySwitcher;
import cn.springcloud.gray.client.switcher.GraySwitcher;
import cn.springcloud.gray.communication.InformationClient;
import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.decision.GrayDecisionFactoryKeeper;
import cn.springcloud.gray.local.InstanceLocalInfo;
import cn.springcloud.gray.request.LocalStorageLifeCycle;
import cn.springcloud.gray.request.RequestLocalStorage;
import cn.springcloud.gray.request.ThreadLocalRequestStorage;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(value={GrayProperties.class, GrayClientProperties.class, GrayServerProperties.class, GrayRequestProperties.class, GrayLoadProperties.class, GrayHoldoutServerProperties.class})
@ConditionalOnProperty(value={"gray.enabled"})
@Import(value={InformationClientConfiguration.class, GrayDecisionFactoryConfiguration.class, GrayTrackConfiguration.class})
public class GrayClientAutoConfiguration {
    @Autowired
    private GrayClientProperties grayClientProperties;
    @Autowired
    private GrayProperties grayProperties;

    @Bean
    @ConditionalOnMissingBean
    public GrayManager grayManager(@Autowired(required=false) GrayLoadProperties grayLoadProperties, GrayDecisionFactoryKeeper grayDecisionFactoryKeeper, @Autowired(required=false) InformationClient informationClient) {
        CacheProperties cacheProperties = this.grayClientProperties.getCacheProperties("grayDecision");
        com.github.benmanes.caffeine.cache.Cache cache = Caffeine.newBuilder().expireAfterWrite(cacheProperties.getExpireSeconds(), TimeUnit.SECONDS).initialCapacity(10).maximumSize(cacheProperties.getMaximumSize()).recordStats().build();
        DefaultGrayManager grayManager = new DefaultGrayManager(this.grayClientProperties, grayLoadProperties, grayDecisionFactoryKeeper, informationClient, new CaffeineCache<String, List<GrayDecision>>(cache));
        return grayManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public GraySwitcher graySwitcher() {
        return new EnvGraySwitcher(this.grayProperties);
    }

    @Bean
    @ConditionalOnProperty(value={"gray.client.instance.grayEnroll"})
    public GrayClientEnrollInitializingDestroyBean grayClientEnrollInitializingDestroyBean(CommunicableGrayManager grayManager, InstanceLocalInfo instanceLocalInfo) {
        return new GrayClientEnrollInitializingDestroyBean(grayManager, this.grayClientProperties, instanceLocalInfo);
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestLocalStorage requestLocalStorage() {
        return new ThreadLocalRequestStorage();
    }

    @Bean
    @ConditionalOnMissingBean
    public LocalStorageLifeCycle localStorageLifeCycle() {
        return new LocalStorageLifeCycle.NoOpLocalStorageLifeCycle();
    }

    @Bean
    public GrayClientInitializer grayClientInitializer() {
        return new GrayClientInitializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public GrayPredicate grayPredicate(RequestLocalStorage requestLocalStorage, GrayManager grayManager) {
        return new DefaultGrayPredicate(requestLocalStorage, grayManager);
    }
}

