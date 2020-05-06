/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Import
 */
package cn.springcloud.gray.bean.properties;

import cn.springcloud.gray.bean.properties.EnableConfigurationPropertiesImportSelector;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
@Documented
@Import(value={EnableConfigurationPropertiesImportSelector.class})
public @interface EnableConfigurationProperties {
    public Class<?>[] value() default {};
}

