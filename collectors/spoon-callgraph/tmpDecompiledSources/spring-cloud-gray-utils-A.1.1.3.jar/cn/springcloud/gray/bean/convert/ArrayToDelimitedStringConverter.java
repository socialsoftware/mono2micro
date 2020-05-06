/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.ConversionService
 *  org.springframework.core.convert.TypeDescriptor
 *  org.springframework.core.convert.converter.ConditionalGenericConverter
 *  org.springframework.core.convert.converter.GenericConverter
 *  org.springframework.core.convert.converter.GenericConverter$ConvertiblePair
 *  org.springframework.util.ObjectUtils
 */
package cn.springcloud.gray.bean.convert;

import cn.springcloud.gray.bean.convert.CollectionToDelimitedStringConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.ObjectUtils;

final class ArrayToDelimitedStringConverter
implements ConditionalGenericConverter {
    private final CollectionToDelimitedStringConverter delegate;

    ArrayToDelimitedStringConverter(ConversionService conversionService) {
        this.delegate = new CollectionToDelimitedStringConverter(conversionService);
    }

    public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new GenericConverter.ConvertiblePair(Object[].class, String.class));
    }

    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return this.delegate.matches(sourceType, targetType);
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        List<Object> list = Arrays.asList(ObjectUtils.toObjectArray((Object)source));
        return this.delegate.convert(list, sourceType, targetType);
    }
}

