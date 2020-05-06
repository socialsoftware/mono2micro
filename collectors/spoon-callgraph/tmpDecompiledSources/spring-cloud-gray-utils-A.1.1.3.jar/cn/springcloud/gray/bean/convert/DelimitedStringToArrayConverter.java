/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.ConversionService
 *  org.springframework.core.convert.TypeDescriptor
 *  org.springframework.core.convert.converter.ConditionalGenericConverter
 *  org.springframework.core.convert.converter.GenericConverter
 *  org.springframework.core.convert.converter.GenericConverter$ConvertiblePair
 *  org.springframework.util.Assert
 *  org.springframework.util.StringUtils
 */
package cn.springcloud.gray.bean.convert;

import cn.springcloud.gray.bean.convert.Delimiter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Set;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

final class DelimitedStringToArrayConverter
implements ConditionalGenericConverter {
    private final ConversionService conversionService;

    DelimitedStringToArrayConverter(ConversionService conversionService) {
        Assert.notNull((Object)conversionService, (String)"ConversionService must not be null");
        this.conversionService = conversionService;
    }

    public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new GenericConverter.ConvertiblePair(String.class, Object[].class));
    }

    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return targetType.getElementTypeDescriptor() == null || this.conversionService.canConvert(sourceType, targetType.getElementTypeDescriptor());
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        return this.convert((String)source, sourceType, targetType);
    }

    private Object convert(String source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        Delimiter delimiter = (Delimiter)targetType.getAnnotation(Delimiter.class);
        String[] elements = this.getElements(source, delimiter != null ? delimiter.value() : ",");
        TypeDescriptor elementDescriptor = targetType.getElementTypeDescriptor();
        Object target = Array.newInstance(elementDescriptor.getType(), elements.length);
        for (int i = 0; i < elements.length; ++i) {
            String sourceElement = elements[i];
            Object targetElement = this.conversionService.convert((Object)sourceElement.trim(), sourceType, elementDescriptor);
            Array.set(target, i, targetElement);
        }
        return target;
    }

    private String[] getElements(String source, String delimiter) {
        return StringUtils.delimitedListToStringArray((String)source, (String)("".equals(delimiter) ? null : delimiter));
    }
}

