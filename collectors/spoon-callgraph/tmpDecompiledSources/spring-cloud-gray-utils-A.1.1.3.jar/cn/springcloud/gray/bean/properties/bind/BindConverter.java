/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.BeanUtils
 *  org.springframework.beans.PropertyEditorRegistry
 *  org.springframework.beans.SimpleTypeConverter
 *  org.springframework.beans.propertyeditors.FileEditor
 *  org.springframework.core.ResolvableType
 *  org.springframework.core.convert.ConversionException
 *  org.springframework.core.convert.ConversionService
 *  org.springframework.core.convert.TypeDescriptor
 *  org.springframework.core.convert.converter.ConditionalGenericConverter
 *  org.springframework.core.convert.converter.ConverterRegistry
 *  org.springframework.core.convert.converter.GenericConverter
 *  org.springframework.core.convert.converter.GenericConverter$ConvertiblePair
 *  org.springframework.core.convert.support.GenericConversionService
 *  org.springframework.util.Assert
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.convert.ApplicationConversionService;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import java.beans.PropertyEditor;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.util.Assert;

final class BindConverter {
    private static final Set<Class<?>> EXCLUDED_EDITORS;
    private static BindConverter sharedInstance;
    private final ConversionService conversionService;

    private BindConverter(ConversionService conversionService, Consumer<PropertyEditorRegistry> propertyEditorInitializer) {
        Assert.notNull((Object)conversionService, (String)"ConversionService must not be null");
        List<ConversionService> conversionServices = this.getConversionServices(conversionService, propertyEditorInitializer);
        this.conversionService = new CompositeConversionService(conversionServices);
    }

    private List<ConversionService> getConversionServices(ConversionService conversionService, Consumer<PropertyEditorRegistry> propertyEditorInitializer) {
        ArrayList<ConversionService> services = new ArrayList<ConversionService>();
        services.add((ConversionService)new TypeConverterConversionService(propertyEditorInitializer));
        services.add(conversionService);
        if (!(conversionService instanceof ApplicationConversionService)) {
            services.add(ApplicationConversionService.getSharedInstance());
        }
        return services;
    }

    public boolean canConvert(Object value, ResolvableType type, Annotation ... annotations) {
        return this.conversionService.canConvert(TypeDescriptor.forObject((Object)value), (TypeDescriptor)new ResolvableTypeDescriptor(type, annotations));
    }

    public <T> T convert(Object result, Bindable<T> target) {
        return this.convert(result, target.getType(), target.getAnnotations());
    }

    public <T> T convert(Object value, ResolvableType type, Annotation ... annotations) {
        if (value == null) {
            return null;
        }
        return (T)this.conversionService.convert(value, TypeDescriptor.forObject((Object)value), (TypeDescriptor)new ResolvableTypeDescriptor(type, annotations));
    }

    static BindConverter get(ConversionService conversionService, Consumer<PropertyEditorRegistry> propertyEditorInitializer) {
        if (conversionService == ApplicationConversionService.getSharedInstance() && propertyEditorInitializer == null) {
            if (sharedInstance == null) {
                sharedInstance = new BindConverter(conversionService, propertyEditorInitializer);
            }
            return sharedInstance;
        }
        return new BindConverter(conversionService, propertyEditorInitializer);
    }

    static {
        HashSet<Class<FileEditor>> excluded = new HashSet<Class<FileEditor>>();
        excluded.add(FileEditor.class);
        EXCLUDED_EDITORS = Collections.unmodifiableSet(excluded);
    }

    private static class TypeConverterConverter
    implements ConditionalGenericConverter {
        private final SimpleTypeConverter typeConverter;

        TypeConverterConverter(SimpleTypeConverter typeConverter) {
            this.typeConverter = typeConverter;
        }

        public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(new GenericConverter.ConvertiblePair(String.class, Object.class));
        }

        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return this.getPropertyEditor(targetType.getType()) != null;
        }

        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            SimpleTypeConverter typeConverter = this.typeConverter;
            return typeConverter.convertIfNecessary(source, targetType.getType());
        }

        private PropertyEditor getPropertyEditor(Class<?> type) {
            SimpleTypeConverter typeConverter = this.typeConverter;
            if (type == null || type == Object.class || Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
                return null;
            }
            PropertyEditor editor = typeConverter.getDefaultEditor(type);
            if (editor == null) {
                editor = typeConverter.findCustomEditor(type, null);
            }
            if (editor == null && String.class != type) {
                editor = BeanUtils.findEditorByConvention(type);
            }
            if (editor == null || EXCLUDED_EDITORS.contains(editor.getClass())) {
                return null;
            }
            return editor;
        }
    }

    private static class TypeConverterConversionService
    extends GenericConversionService {
        TypeConverterConversionService(Consumer<PropertyEditorRegistry> initializer) {
            this.addConverter((GenericConverter)new TypeConverterConverter(this.createTypeConverter(initializer)));
            ApplicationConversionService.addDelimitedStringConverters((ConverterRegistry)this);
        }

        private SimpleTypeConverter createTypeConverter(Consumer<PropertyEditorRegistry> initializer) {
            SimpleTypeConverter typeConverter = new SimpleTypeConverter();
            if (initializer != null) {
                initializer.accept((PropertyEditorRegistry)typeConverter);
            }
            return typeConverter;
        }

        public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (targetType.isArray() && targetType.getElementTypeDescriptor().isPrimitive()) {
                return false;
            }
            return super.canConvert(sourceType, targetType);
        }
    }

    static class CompositeConversionService
    implements ConversionService {
        private final List<ConversionService> delegates;

        CompositeConversionService(List<ConversionService> delegates) {
            this.delegates = delegates;
        }

        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            Assert.notNull(targetType, (String)"Target type to convert to cannot be null");
            return this.canConvert(sourceType != null ? TypeDescriptor.valueOf(sourceType) : null, TypeDescriptor.valueOf(targetType));
        }

        public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
            for (ConversionService service : this.delegates) {
                if (!service.canConvert(sourceType, targetType)) continue;
                return true;
            }
            return false;
        }

        public <T> T convert(Object source, Class<T> targetType) {
            Assert.notNull(targetType, (String)"Target type to convert to cannot be null");
            return (T)this.convert(source, TypeDescriptor.forObject((Object)source), TypeDescriptor.valueOf(targetType));
        }

        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            for (int i = 0; i < this.delegates.size() - 1; ++i) {
                try {
                    ConversionService delegate = this.delegates.get(i);
                    if (!delegate.canConvert(sourceType, targetType)) continue;
                    return delegate.convert(source, sourceType, targetType);
                }
                catch (ConversionException delegate) {
                    // empty catch block
                }
            }
            return this.delegates.get(this.delegates.size() - 1).convert(source, sourceType, targetType);
        }
    }

    private static class ResolvableTypeDescriptor
    extends TypeDescriptor {
        ResolvableTypeDescriptor(ResolvableType resolvableType, Annotation[] annotations) {
            super(resolvableType, null, annotations);
        }
    }

}

