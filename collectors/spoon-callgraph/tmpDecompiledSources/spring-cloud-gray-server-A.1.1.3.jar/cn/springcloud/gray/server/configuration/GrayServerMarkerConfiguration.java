/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package cn.springcloud.gray.server.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrayServerMarkerConfiguration {
    @Bean
    public GrayServerMarker grayServerMarkerBean() {
        return new GrayServerMarker();
    }

    class GrayServerMarker {
        GrayServerMarker() {
        }
    }

}

