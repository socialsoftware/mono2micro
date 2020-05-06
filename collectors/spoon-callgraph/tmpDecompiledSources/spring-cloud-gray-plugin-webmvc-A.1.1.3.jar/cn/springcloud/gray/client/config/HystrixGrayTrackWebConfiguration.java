/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.request.RequestLocalStorage
 *  cn.springcloud.gray.request.track.GrayTrackHolder
 *  com.netflix.hystrix.strategy.concurrency.HystrixRequestContext
 *  javax.servlet.FilterChain
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.client.config;

import cn.springcloud.gray.request.RequestLocalStorage;
import cn.springcloud.gray.request.track.GrayTrackHolder;
import cn.springcloud.gray.web.GrayTrackFilter;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value={"gray.request.track.enabled"}, matchIfMissing=true)
public class HystrixGrayTrackWebConfiguration {
    @Bean
    @ConditionalOnProperty(value={"gray.client.runenv"}, havingValue="web", matchIfMissing=true)
    public GrayTrackFilter grayTrackFilter(GrayTrackHolder grayTrackHolder, RequestLocalStorage requestLocalStorage) {
        return new GrayTrackFilter(grayTrackHolder, requestLocalStorage){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                if (!HystrixRequestContext.isCurrentThreadInitialized()) {
                    HystrixRequestContext.initializeContext();
                }
                try {
                    super.doFilter(request, response, chain);
                }
                finally {
                    if (HystrixRequestContext.isCurrentThreadInitialized()) {
                        HystrixRequestContext.getContextForCurrentThread().shutdown();
                    }
                }
            }
        };
    }

}

