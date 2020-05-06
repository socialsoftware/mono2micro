/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.TypeDescriptor
 *  org.springframework.core.convert.converter.GenericConverter
 *  org.springframework.core.convert.converter.GenericConverter$ConvertiblePair
 */
package cn.springcloud.gray.bean.convert;

import cn.springcloud.gray.bean.convert.DurationFormat;
import cn.springcloud.gray.bean.convert.DurationStyle;
import cn.springcloud.gray.bean.convert.DurationUnit;
import java.lang.annotation.Annotation;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

final class DurationToStringConverter
implements GenericConverter {
    DurationToStringConverter() {
    }

    public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new GenericConverter.ConvertiblePair(Duration.class, String.class));
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        return this.convert((Duration)source, this.getDurationStyle(sourceType), this.getDurationUnit(sourceType));
    }

    private ChronoUnit getDurationUnit(TypeDescriptor sourceType) {
        DurationUnit annotation = (DurationUnit)sourceType.getAnnotation(DurationUnit.class);
        return annotation != null ? annotation.value() : null;
    }

    private DurationStyle getDurationStyle(TypeDescriptor sourceType) {
        DurationFormat annotation = (DurationFormat)sourceType.getAnnotation(DurationFormat.class);
        return annotation != null ? annotation.value() : null;
    }

    private String convert(Duration source, DurationStyle style, ChronoUnit unit) {
        style = style != null ? style : DurationStyle.ISO8601;
        return style.print(source, unit);
    }
}

