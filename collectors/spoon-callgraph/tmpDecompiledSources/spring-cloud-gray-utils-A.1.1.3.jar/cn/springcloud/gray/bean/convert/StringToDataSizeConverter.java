/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.TypeDescriptor
 *  org.springframework.core.convert.converter.GenericConverter
 *  org.springframework.core.convert.converter.GenericConverter$ConvertiblePair
 *  org.springframework.util.ObjectUtils
 */
package cn.springcloud.gray.bean.convert;

import cn.springcloud.gray.bean.convert.DataSizeUnit;
import cn.springcloud.gray.bean.domain.DataSize;
import cn.springcloud.gray.bean.domain.DataUnit;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.ObjectUtils;

final class StringToDataSizeConverter
implements GenericConverter {
    StringToDataSizeConverter() {
    }

    public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new GenericConverter.ConvertiblePair(String.class, DataSize.class));
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (ObjectUtils.isEmpty((Object)source)) {
            return null;
        }
        return this.convert(source.toString(), this.getDataUnit(targetType));
    }

    private DataUnit getDataUnit(TypeDescriptor targetType) {
        DataSizeUnit annotation = (DataSizeUnit)targetType.getAnnotation(DataSizeUnit.class);
        return annotation != null ? annotation.value() : null;
    }

    private DataSize convert(String source, DataUnit unit) {
        return DataSize.parse(source, unit);
    }
}

