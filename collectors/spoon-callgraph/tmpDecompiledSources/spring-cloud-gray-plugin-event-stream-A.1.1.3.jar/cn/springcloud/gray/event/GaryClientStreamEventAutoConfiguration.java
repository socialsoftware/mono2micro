/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.GrayManager
 *  cn.springcloud.gray.client.config.GrayEventAutoConfiguration
 *  cn.springcloud.gray.event.GrayEventListener
 *  org.springframework.boot.autoconfigure.AutoConfigureAfter
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.cloud.stream.annotation.EnableBinding
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.event;

import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.client.config.GrayEventAutoConfiguration;
import cn.springcloud.gray.event.GrayEventListener;
import cn.springcloud.gray.event.stream.StreamInput;
import cn.springcloud.gray.event.stream.StreamMessageListener;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(value={EnableBinding.class})
@EnableBinding(value={StreamInput.class})
@ConditionalOnBean(value={GrayManager.class})
@AutoConfigureAfter(value={GrayEventAutoConfiguration.class})
public class GaryClientStreamEventAutoConfiguration {
    @Bean
    @ConditionalOnProperty(value={"spring.cloud.stream.bindings.GrayEventInput.destination"})
    public StreamMessageListener streamMessageListener(GrayEventListener grayEventListener) {
        return new StreamMessageListener(grayEventListener);
    }
}

