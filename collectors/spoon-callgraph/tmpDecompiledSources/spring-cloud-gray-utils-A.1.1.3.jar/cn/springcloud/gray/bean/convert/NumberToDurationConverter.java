/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.TypeDescriptor
 *  org.springframework.core.convert.converter.GenericConverter
 *  org.springframework.core.convert.converter.GenericConverter$ConvertiblePair
 */
package cn.springcloud.gray.bean.convert;

import cn.springcloud.gray.bean.convert.StringToDurationConverter;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

final class NumberToDurationConverter
implements GenericConverter {
    private final StringToDurationConverter delegate = new StringToDurationConverter();

    NumberToDurationConverter() {
    }

    public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new GenericConverter.ConvertiblePair(Number.class, Duration.class));
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return this.delegate.convert((Object)(source != null ? source.toString() : null), TypeDescriptor.valueOf(String.class), targetType);
    }
}

