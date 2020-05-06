/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.GrayEventPublisher
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.cloud.stream.annotation.EnableBinding
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.server.event;

import cn.springcloud.gray.event.GrayEventPublisher;
import cn.springcloud.gray.server.event.stream.StreamGrayEventPublisher;
import cn.springcloud.gray.server.event.stream.StreamOutput;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(value={EnableBinding.class})
@ConditionalOnProperty(value={"spring.cloud.stream.bindings.GrayEventOutput.destination"})
@EnableBinding(value={StreamOutput.class})
public class GrayServerEventStreamConfiguration {
    @Bean
    public GrayEventPublisher grayEventPublisher(StreamOutput streamOutput) {
        return new StreamGrayEventPublisher(streamOutput);
    }
}

