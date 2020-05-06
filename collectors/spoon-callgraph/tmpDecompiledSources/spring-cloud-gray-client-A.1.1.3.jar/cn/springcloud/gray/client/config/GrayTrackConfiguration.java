/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.boot.context.properties.EnableConfigurationProperties
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.client.config;

import cn.springcloud.gray.client.config.properties.GrayTrackProperties;
import cn.springcloud.gray.communication.InformationClient;
import cn.springcloud.gray.request.GrayInfoTracker;
import cn.springcloud.gray.request.GrayTrackInfo;
import cn.springcloud.gray.request.track.DefaultGrayTrackHolder;
import cn.springcloud.gray.request.track.GrayTrackHolder;
import cn.springcloud.gray.web.GrayTrackRequestInterceptor;
import cn.springcloud.gray.web.tracker.HttpHeaderGrayInfoTracker;
import cn.springcloud.gray.web.tracker.HttpIPGrayInfoTracker;
import cn.springcloud.gray.web.tracker.HttpMethodGrayInfoTracker;
import cn.springcloud.gray.web.tracker.HttpParameterGrayInfoTracker;
import cn.springcloud.gray.web.tracker.HttpReceiveGrayInfoTracker;
import cn.springcloud.gray.web.tracker.HttpURIGrayInfoTracker;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value={"gray.request.track.enabled"}, matchIfMissing=true)
@EnableConfigurationProperties(value={GrayTrackProperties.class})
public class GrayTrackConfiguration {
    @Bean(initMethod="setup")
    @ConditionalOnMissingBean
    public GrayTrackHolder grayTrackHolder(GrayTrackProperties grayTrackProperties, @Autowired(required=false) InformationClient informationClient, List<GrayInfoTracker<? extends GrayTrackInfo, ?>> trackers) {
        return new DefaultGrayTrackHolder(grayTrackProperties, informationClient, trackers);
    }

    @Configuration
    @ConditionalOnProperty(value={"gray.client.runenv"}, havingValue="web", matchIfMissing=true)
    public static class GrayHttpTrackerConfiguration {
        @Bean
        public HttpReceiveGrayInfoTracker httpReceiveGrayTracker() {
            return new HttpReceiveGrayInfoTracker();
        }

        @Bean
        public HttpHeaderGrayInfoTracker httpHeaderGrayTracker() {
            return new HttpHeaderGrayInfoTracker();
        }

        @Bean
        public HttpMethodGrayInfoTracker httpMethodGrayTracker() {
            return new HttpMethodGrayInfoTracker();
        }

        @Bean
        public HttpURIGrayInfoTracker httpURIGrayTracker() {
            return new HttpURIGrayInfoTracker();
        }

        @Bean
        public HttpIPGrayInfoTracker httpIPGrayTracker() {
            return new HttpIPGrayInfoTracker();
        }

        @Bean
        public HttpParameterGrayInfoTracker httpParameterGrayTracker() {
            return new HttpParameterGrayInfoTracker();
        }

        @Bean
        public GrayTrackRequestInterceptor grayTrackRequestInterceptor() {
            return new GrayTrackRequestInterceptor();
        }
    }

}

