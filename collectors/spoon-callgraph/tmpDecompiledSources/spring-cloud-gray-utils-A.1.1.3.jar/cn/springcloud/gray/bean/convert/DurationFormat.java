/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.convert;

import cn.springcloud.gray.bean.convert.DurationStyle;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
@Documented
public @interface DurationFormat {
    public DurationStyle value();
}

