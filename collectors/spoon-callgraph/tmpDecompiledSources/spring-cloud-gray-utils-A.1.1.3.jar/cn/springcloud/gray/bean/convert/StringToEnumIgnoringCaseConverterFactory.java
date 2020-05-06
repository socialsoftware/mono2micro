/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.converter.Converter
 *  org.springframework.core.convert.converter.ConverterFactory
 *  org.springframework.util.Assert
 */
package cn.springcloud.gray.bean.convert;

import java.util.EnumSet;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.Assert;

final class StringToEnumIgnoringCaseConverterFactory
implements ConverterFactory<String, Enum> {
    StringToEnumIgnoringCaseConverterFactory() {
    }

    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        Class<T> enumType;
        for (enumType = targetType; enumType != null && !enumType.isEnum(); enumType = enumType.getSuperclass()) {
        }
        Assert.notNull(enumType, (String)("The target type " + targetType.getName() + " does not refer to an enum"));
        return new StringToEnum<T>(enumType);
    }

    private class StringToEnum<T extends Enum>
    implements Converter<String, T> {
        private final Class<T> enumType;

        StringToEnum(Class<T> enumType) {
            this.enumType = enumType;
        }

        public T convert(String source) {
            if (source.isEmpty()) {
                return null;
            }
            source = source.trim();
            try {
                return Enum.valueOf(this.enumType, source);
            }
            catch (Exception ex) {
                return this.findEnum(source);
            }
        }

        private T findEnum(String source) {
            String name = this.getLettersAndDigits(source);
            for (Enum candidate : EnumSet.allOf(this.enumType)) {
                if (!this.getLettersAndDigits(candidate.name()).equals(name)) continue;
                return (T)candidate;
            }
            throw new IllegalArgumentException("No enum constant " + this.enumType.getCanonicalName() + "." + source);
        }

        private String getLettersAndDigits(String name) {
            StringBuilder canonicalName = new StringBuilder(name.length());
            name.chars().map(c -> (char)c).filter(Character::isLetterOrDigit).map(Character::toLowerCase).forEach(canonicalName::append);
            return canonicalName.toString();
        }
    }

}

