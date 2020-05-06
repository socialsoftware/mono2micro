/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Qualifier
 */
package cn.springcloud.gray.bean.properties;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.beans.factory.annotation.Qualifier;

@Qualifier(value="org.springframework.boot.context.properties.ConfigurationPropertiesBinding")
@Target(value={ElementType.TYPE, ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationPropertiesBinding {
    public static final String VALUE = "org.springframework.boot.context.properties.ConfigurationPropertiesBinding";
}

