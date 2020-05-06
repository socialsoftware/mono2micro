/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.ConversionService
 *  org.springframework.core.convert.TypeDescriptor
 *  org.springframework.core.convert.converter.ConditionalGenericConverter
 *  org.springframework.core.convert.converter.GenericConverter
 *  org.springframework.core.convert.converter.GenericConverter$ConvertiblePair
 */
package cn.springcloud.gray.bean.convert;

import cn.springcloud.gray.bean.convert.Delimiter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.GenericConverter;

final class CollectionToDelimitedStringConverter
implements ConditionalGenericConverter {
    private final ConversionService conversionService;

    CollectionToDelimitedStringConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new GenericConverter.ConvertiblePair(Collection.class, String.class));
    }

    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        TypeDescriptor sourceElementType = sourceType.getElementTypeDescriptor();
        if (targetType == null || sourceElementType == null) {
            return true;
        }
        return this.conversionService.canConvert(sourceElementType, targetType) || sourceElementType.getType().isAssignableFrom(targetType.getType());
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        Collection sourceCollection = (Collection)source;
        return this.convert(sourceCollection, sourceType, targetType);
    }

    private Object convert(Collection<?> source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source.isEmpty()) {
            return "";
        }
        return source.stream().map(element -> this.convertElement(element, sourceType, targetType)).collect(Collectors.joining(this.getDelimiter(sourceType)));
    }

    private CharSequence getDelimiter(TypeDescriptor sourceType) {
        Delimiter annotation = (Delimiter)sourceType.getAnnotation(Delimiter.class);
        return annotation != null ? annotation.value() : ",";
    }

    private String convertElement(Object element, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return String.valueOf(this.conversionService.convert(element, sourceType.elementTypeDescriptor(element), targetType));
    }
}

