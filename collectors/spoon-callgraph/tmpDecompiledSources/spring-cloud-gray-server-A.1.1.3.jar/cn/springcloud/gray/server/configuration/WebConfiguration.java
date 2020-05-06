/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  javax.servlet.Filter
 *  org.springframework.boot.web.servlet.FilterRegistrationBean
 *  org.springframework.boot.web.servlet.ServletRegistrationBean
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Import
 *  org.springframework.web.cors.CorsConfiguration
 *  org.springframework.web.cors.CorsConfigurationSource
 *  org.springframework.web.cors.UrlBasedCorsConfigurationSource
 *  org.springframework.web.filter.CorsFilter
 *  org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
 */
package cn.springcloud.gray.server.configuration;

import cn.springcloud.gray.server.configuration.Swagger2Configuration;
import javax.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@Import(value={Swagger2Configuration.class})
public class WebConfiguration
extends WebMvcConfigurerAdapter {
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(Boolean.valueOf(true));
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setMaxAge(Long.valueOf(3600L));
        corsConfiguration.addExposedHeader("X-Total-Count");
        corsConfiguration.addExposedHeader("X-Pagination");
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        new CorsFilter((CorsConfigurationSource)urlBasedCorsConfigurationSource);
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean((Filter)new CorsFilter((CorsConfigurationSource)urlBasedCorsConfigurationSource), new ServletRegistrationBean[0]);
        filterRegistrationBean.addUrlPatterns(new String[]{"/*"});
        return filterRegistrationBean;
    }
}

