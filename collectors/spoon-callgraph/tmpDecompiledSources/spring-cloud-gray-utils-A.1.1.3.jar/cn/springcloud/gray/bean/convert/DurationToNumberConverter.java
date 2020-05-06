/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.TypeDescriptor
 *  org.springframework.core.convert.converter.GenericConverter
 *  org.springframework.core.convert.converter.GenericConverter$ConvertiblePair
 *  org.springframework.util.ReflectionUtils
 */
package cn.springcloud.gray.bean.convert;

import cn.springcloud.gray.bean.convert.DurationStyle;
import cn.springcloud.gray.bean.convert.DurationUnit;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.ReflectionUtils;

final class DurationToNumberConverter
implements GenericConverter {
    DurationToNumberConverter() {
    }

    public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new GenericConverter.ConvertiblePair(Duration.class, Number.class));
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        return this.convert((Duration)source, this.getDurationUnit(sourceType), targetType.getObjectType());
    }

    private ChronoUnit getDurationUnit(TypeDescriptor sourceType) {
        DurationUnit annotation = (DurationUnit)sourceType.getAnnotation(DurationUnit.class);
        return annotation != null ? annotation.value() : null;
    }

    private Object convert(Duration source, ChronoUnit unit, Class<?> type) {
        try {
            return type.getConstructor(String.class).newInstance(String.valueOf(DurationStyle.Unit.fromChronoUnit(unit).longValue(source)));
        }
        catch (Exception ex) {
            ReflectionUtils.rethrowRuntimeException((Throwable)ex);
            throw new IllegalStateException(ex);
        }
    }
}

