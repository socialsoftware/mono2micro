/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.core.convert.converter.Converter
 *  org.springframework.format.FormatterRegistry
 *  org.springframework.format.datetime.standard.DateTimeFormatterRegistrar
 *  org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
 */
package cn.springcloud.gray.server.configuration;

import cn.springcloud.gray.server.resources.converter.StringToDateConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class DateTimeFormatConfiguration
extends WebMvcConfigurerAdapter {
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);
        registry.addConverter((Converter)new StringToDateConverter());
    }
}

