/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.BeanUtils
 *  org.springframework.core.MethodParameter
 *  org.springframework.core.ResolvableType
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.properties.bind.BeanBinder;
import cn.springcloud.gray.bean.properties.bind.BeanPropertyBinder;
import cn.springcloud.gray.bean.properties.bind.BeanPropertyName;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.bind.Binder;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyState;
import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;

class JavaBeanBinder
implements BeanBinder {
    JavaBeanBinder() {
    }

    @Override
    public <T> T bind(ConfigurationPropertyName name, Bindable<T> target, Binder.Context context, BeanPropertyBinder propertyBinder) {
        boolean hasKnownBindableProperties = this.hasKnownBindableProperties(name, context);
        Bean<T> bean = Bean.get(target, hasKnownBindableProperties);
        if (bean == null) {
            return null;
        }
        BeanSupplier<T> beanSupplier = bean.getSupplier(target);
        boolean bound = this.bind(propertyBinder, bean, beanSupplier);
        return bound ? (T)beanSupplier.get() : null;
    }

    private boolean hasKnownBindableProperties(ConfigurationPropertyName name, Binder.Context context) {
        for (ConfigurationPropertySource source : context.getSources()) {
            if (source.containsDescendantOf(name) != ConfigurationPropertyState.PRESENT) continue;
            return true;
        }
        return false;
    }

    private <T> boolean bind(BeanPropertyBinder propertyBinder, Bean<T> bean, BeanSupplier<T> beanSupplier) {
        boolean bound = false;
        for (BeanProperty beanProperty : bean.getProperties().values()) {
            bound |= this.bind(beanSupplier, propertyBinder, beanProperty);
        }
        return bound;
    }

    private <T> boolean bind(BeanSupplier<T> beanSupplier, BeanPropertyBinder propertyBinder, BeanProperty property) {
        String propertyName = property.getName();
        ResolvableType type = property.getType();
        Supplier<Object> value = property.getValue(beanSupplier);
        Annotation[] annotations = property.getAnnotations();
        Object bound = propertyBinder.bindProperty(propertyName, Bindable.of(type).withSuppliedValue(value).withAnnotations(annotations));
        if (bound == null) {
            return false;
        }
        if (property.isSettable()) {
            property.setValue(beanSupplier, bound);
        } else if (value == null || !bound.equals(value.get())) {
            throw new IllegalStateException("No setter found for property: " + property.getName());
        }
        return true;
    }

    private static class BeanProperty {
        private final String name;
        private final ResolvableType declaringClassType;
        private Method getter;
        private Method setter;
        private Field field;

        BeanProperty(String name, ResolvableType declaringClassType) {
            this.name = BeanPropertyName.toDashedForm(name);
            this.declaringClassType = declaringClassType;
        }

        public void addGetter(Method getter) {
            if (this.getter == null) {
                this.getter = getter;
            }
        }

        public void addSetter(Method setter) {
            if (this.setter == null) {
                this.setter = setter;
            }
        }

        public void addField(Field field) {
            if (this.field == null) {
                this.field = field;
            }
        }

        public String getName() {
            return this.name;
        }

        public ResolvableType getType() {
            if (this.setter != null) {
                MethodParameter methodParameter = new MethodParameter(this.setter, 0);
                return ResolvableType.forMethodParameter((MethodParameter)methodParameter, (ResolvableType)this.declaringClassType);
            }
            MethodParameter methodParameter = new MethodParameter(this.getter, -1);
            return ResolvableType.forMethodParameter((MethodParameter)methodParameter, (ResolvableType)this.declaringClassType);
        }

        public Annotation[] getAnnotations() {
            try {
                return this.field != null ? this.field.getDeclaredAnnotations() : null;
            }
            catch (Exception ex) {
                return null;
            }
        }

        public Supplier<Object> getValue(Supplier<?> instance) {
            if (this.getter == null) {
                return null;
            }
            return () -> {
                try {
                    this.getter.setAccessible(true);
                    return this.getter.invoke(instance.get(), new Object[0]);
                }
                catch (Exception ex) {
                    throw new IllegalStateException("Unable to get value for property " + this.name, ex);
                }
            };
        }

        public boolean isSettable() {
            return this.setter != null;
        }

        public void setValue(Supplier<?> instance, Object value) {
            try {
                this.setter.setAccessible(true);
                this.setter.invoke(instance.get(), value);
            }
            catch (Exception ex) {
                throw new IllegalStateException("Unable to set value for property " + this.name, ex);
            }
        }
    }

    private static class BeanSupplier<T>
    implements Supplier<T> {
        private final Supplier<T> factory;
        private T instance;

        BeanSupplier(Supplier<T> factory) {
            this.factory = factory;
        }

        @Override
        public T get() {
            if (this.instance == null) {
                this.instance = this.factory.get();
            }
            return this.instance;
        }
    }

    private static class Bean<T> {
        private static Bean<?> cached;
        private final Class<?> type;
        private final ResolvableType resolvableType;
        private final Map<String, BeanProperty> properties = new LinkedHashMap<String, BeanProperty>();

        Bean(ResolvableType resolvableType, Class<?> type) {
            this.resolvableType = resolvableType;
            this.type = type;
            this.putProperties(type);
        }

        private void putProperties(Class<?> type) {
            while (type != null && !Object.class.equals(type)) {
                for (Method method : type.getDeclaredMethods()) {
                    if (!this.isCandidate(method)) continue;
                    this.addMethod(method);
                }
                for (AccessibleObject field : type.getDeclaredFields()) {
                    this.addField((Field)field);
                }
                type = type.getSuperclass();
            }
        }

        private boolean isCandidate(Method method) {
            int modifiers = method.getModifiers();
            return Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers) && !Modifier.isStatic(modifiers) && !Object.class.equals(method.getDeclaringClass()) && !Class.class.equals(method.getDeclaringClass());
        }

        private void addMethod(Method method) {
            this.addMethodIfPossible(method, "get", 0, BeanProperty::addGetter);
            this.addMethodIfPossible(method, "is", 0, BeanProperty::addGetter);
            this.addMethodIfPossible(method, "set", 1, BeanProperty::addSetter);
        }

        private void addMethodIfPossible(Method method, String prefix, int parameterCount, BiConsumer<BeanProperty, Method> consumer) {
            if (method.getParameterCount() == parameterCount && method.getName().startsWith(prefix) && method.getName().length() > prefix.length()) {
                String propertyName = Introspector.decapitalize(method.getName().substring(prefix.length()));
                consumer.accept(this.properties.computeIfAbsent(propertyName, this::getBeanProperty), method);
            }
        }

        private BeanProperty getBeanProperty(String name) {
            return new BeanProperty(name, this.resolvableType);
        }

        private void addField(Field field) {
            BeanProperty property = this.properties.get(field.getName());
            if (property != null) {
                property.addField(field);
            }
        }

        public Class<?> getType() {
            return this.type;
        }

        public Map<String, BeanProperty> getProperties() {
            return this.properties;
        }

        public BeanSupplier<T> getSupplier(Bindable<T> target) {
            return new BeanSupplier<Object>(() -> {
                Object instance = null;
                if (target.getValue() != null) {
                    instance = target.getValue().get();
                }
                if (instance == null) {
                    instance = BeanUtils.instantiateClass(this.type);
                }
                return instance;
            });
        }

        public static <T> Bean<T> get(Bindable<T> bindable, boolean canCallGetValue) {
            Class<?> type = bindable.getType().resolve(Object.class);
            Supplier<T> value = bindable.getValue();
            Object instance = null;
            if (canCallGetValue && value != null) {
                instance = value.get();
                Class<?> class_ = type = instance != null ? instance.getClass() : type;
            }
            if (instance == null && !Bean.isInstantiable(type)) {
                return null;
            }
            Bean<Object> bean = cached;
            if (bean == null || !type.equals(bean.getType())) {
                bean = new Bean<T>(bindable.getType(), type);
                cached = bean;
            }
            return bean;
        }

        private static boolean isInstantiable(Class<?> type) {
            if (type.isInterface()) {
                return false;
            }
            try {
                type.getDeclaredConstructor(new Class[0]);
                return true;
            }
            catch (Exception ex) {
                return false;
            }
        }
    }

}

