/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.ResolvableType
 *  org.springframework.core.style.ToStringCreator
 *  org.springframework.util.Assert
 *  org.springframework.util.ObjectUtils
 */
package cn.springcloud.gray.bean.properties.bind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.core.ResolvableType;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public final class Bindable<T> {
    private static final Annotation[] NO_ANNOTATIONS = new Annotation[0];
    private final ResolvableType type;
    private final ResolvableType boxedType;
    private final Supplier<T> value;
    private final Annotation[] annotations;

    private Bindable(ResolvableType type, ResolvableType boxedType, Supplier<T> value, Annotation[] annotations) {
        this.type = type;
        this.boxedType = boxedType;
        this.value = value;
        this.annotations = annotations;
    }

    public ResolvableType getType() {
        return this.type;
    }

    public ResolvableType getBoxedType() {
        return this.boxedType;
    }

    public Supplier<T> getValue() {
        return this.value;
    }

    public Annotation[] getAnnotations() {
        return this.annotations;
    }

    public <A extends Annotation> A getAnnotation(Class<A> type) {
        for (Annotation annotation : this.annotations) {
            if (!type.isInstance(annotation)) continue;
            return (A)annotation;
        }
        return null;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Bindable other = (Bindable)obj;
        boolean result = true;
        result = result && this.nullSafeEquals(this.type.resolve(), other.type.resolve());
        result = result && this.nullSafeEquals(this.annotations, other.annotations);
        return result;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + ObjectUtils.nullSafeHashCode((Object)this.type);
        result = 31 * result + ObjectUtils.nullSafeHashCode((Object[])this.annotations);
        return result;
    }

    public String toString() {
        ToStringCreator creator = new ToStringCreator((Object)this);
        creator.append("type", (Object)this.type);
        creator.append("value", (Object)(this.value != null ? "provided" : "none"));
        creator.append("annotations", (Object)this.annotations);
        return creator.toString();
    }

    private boolean nullSafeEquals(Object o1, Object o2) {
        return ObjectUtils.nullSafeEquals((Object)o1, (Object)o2);
    }

    public Bindable<T> withAnnotations(Annotation ... annotations) {
        return new Bindable<T>(this.type, this.boxedType, this.value, annotations != null ? annotations : NO_ANNOTATIONS);
    }

    public Bindable<T> withExistingValue(T existingValue) {
        Assert.isTrue((boolean)(existingValue == null || this.type.isArray() || this.boxedType.resolve().isInstance(existingValue)), (String)("ExistingValue must be an instance of " + (Object)this.type));
        Supplier<Object> value = existingValue != null ? () -> existingValue : null;
        return new Bindable<Object>(this.type, this.boxedType, value, NO_ANNOTATIONS);
    }

    public Bindable<T> withSuppliedValue(Supplier<T> suppliedValue) {
        return new Bindable<T>(this.type, this.boxedType, suppliedValue, NO_ANNOTATIONS);
    }

    public static <T> Bindable<T> ofInstance(T instance) {
        Assert.notNull(instance, (String)"Instance must not be null");
        Class<?> type = instance.getClass();
        return Bindable.of(type).withExistingValue(instance);
    }

    public static <T> Bindable<T> of(Class<T> type) {
        Assert.notNull(type, (String)"Type must not be null");
        return Bindable.of(ResolvableType.forClass(type));
    }

    public static <E> Bindable<List<E>> listOf(Class<E> elementType) {
        return Bindable.of(ResolvableType.forClassWithGenerics(List.class, (Class[])new Class[]{elementType}));
    }

    public static <E> Bindable<Set<E>> setOf(Class<E> elementType) {
        return Bindable.of(ResolvableType.forClassWithGenerics(Set.class, (Class[])new Class[]{elementType}));
    }

    public static <K, V> Bindable<Map<K, V>> mapOf(Class<K> keyType, Class<V> valueType) {
        return Bindable.of(ResolvableType.forClassWithGenerics(Map.class, (Class[])new Class[]{keyType, valueType}));
    }

    public static <T> Bindable<T> of(ResolvableType type) {
        Assert.notNull((Object)type, (String)"Type must not be null");
        ResolvableType boxedType = Bindable.box(type);
        return new Bindable<T>(type, boxedType, null, NO_ANNOTATIONS);
    }

    private static ResolvableType box(ResolvableType type) {
        Class resolved = type.resolve();
        if (resolved != null && resolved.isPrimitive()) {
            Object array = Array.newInstance(resolved, 1);
            Class<?> wrapperType = Array.get(array, 0).getClass();
            return ResolvableType.forClass(wrapperType);
        }
        if (resolved != null && resolved.isArray()) {
            return ResolvableType.forArrayComponent((ResolvableType)Bindable.box(type.getComponentType()));
        }
        return type;
    }
}

