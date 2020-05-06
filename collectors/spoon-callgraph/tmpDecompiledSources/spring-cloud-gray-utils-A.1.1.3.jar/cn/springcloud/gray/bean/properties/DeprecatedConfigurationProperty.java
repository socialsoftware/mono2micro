/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
@Documented
public @interface DeprecatedConfigurationProperty {
    public String reason() default "";

    public String replacement() default "";
}

