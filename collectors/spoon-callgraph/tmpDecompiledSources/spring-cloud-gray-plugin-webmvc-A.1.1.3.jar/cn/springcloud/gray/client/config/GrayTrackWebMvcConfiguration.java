/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.client.config.properties.GrayTrackProperties
 *  cn.springcloud.gray.client.config.properties.GrayTrackProperties$Web
 *  cn.springcloud.gray.client.netflix.hystrix.HystrixLocalStorageCycle
 *  cn.springcloud.gray.client.netflix.hystrix.HystrixRequestLocalStorage
 *  cn.springcloud.gray.request.LocalStorageLifeCycle
 *  cn.springcloud.gray.request.RequestLocalStorage
 *  cn.springcloud.gray.request.track.GrayTrackHolder
 *  javax.servlet.Filter
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.boot.web.servlet.FilterRegistrationBean
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Import
 */
package cn.springcloud.gray.client.config;

import cn.springcloud.gray.client.config.HystrixGrayTrackWebConfiguration;
import cn.springcloud.gray.client.config.properties.GrayTrackProperties;
import cn.springcloud.gray.client.netflix.hystrix.HystrixLocalStorageCycle;
import cn.springcloud.gray.client.netflix.hystrix.HystrixRequestLocalStorage;
import cn.springcloud.gray.request.LocalStorageLifeCycle;
import cn.springcloud.gray.request.RequestLocalStorage;
import cn.springcloud.gray.request.track.GrayTrackHolder;
import cn.springcloud.gray.web.GrayTrackFilter;
import javax.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnBean(value={GrayTrackHolder.class})
@ConditionalOnProperty(value={"gray.client.runenv"}, havingValue="web", matchIfMissing=true)
public class GrayTrackWebMvcConfiguration {
    @Autowired
    private GrayTrackProperties grayTrackProperties;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={Filter.class})
    public GrayTrackFilter grayTrackFilter(GrayTrackHolder grayTrackHolder, RequestLocalStorage requestLocalStorage) {
        return new GrayTrackFilter(grayTrackHolder, requestLocalStorage);
    }

    @Bean
    @ConditionalOnBean(value={GrayTrackFilter.class})
    @ConditionalOnClass(value={FilterRegistrationBean.class})
    public FilterRegistrationBean grayTraceFilter(GrayTrackFilter filter) {
        GrayTrackProperties.Web webProperties = this.grayTrackProperties.getWeb();
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter((Filter)filter);
        for (String pattern : webProperties.getPathPatterns()) {
            registration.addUrlPatterns(new String[]{pattern});
        }
        registration.setName("GrayTrackFilter");
        registration.setOrder(Integer.MAX_VALUE);
        return registration;
    }

    @ConditionalOnProperty(value={"gray.hystrix.threadTransmitStrategy"}, havingValue="HYSTRIX_REQUEST_LOCAL_STORAGE")
    @Import(value={HystrixGrayTrackWebConfiguration.class})
    public static class HystrixRequestLocalStorageConfiguration {
        @Bean
        public RequestLocalStorage requestLocalStorage() {
            return new HystrixRequestLocalStorage();
        }

        @Bean
        public LocalStorageLifeCycle localStorageLifeCycle() {
            return new HystrixLocalStorageCycle();
        }
    }

}

