/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.annotation.AliasFor
 */
package cn.springcloud.gray.bean.properties;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

@Target(value={ElementType.TYPE, ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {
    @AliasFor(value="prefix")
    public String value() default "";

    @AliasFor(value="value")
    public String prefix() default "";

    public boolean ignoreInvalidFields() default false;

    public boolean ignoreUnknownFields() default true;
}

