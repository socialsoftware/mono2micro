/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.BeanUtils
 *  org.springframework.util.Assert
 *  org.springframework.util.ObjectUtils
 */
package cn.springcloud.gray.bean.properties.bind;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public final class BindResult<T> {
    private static final BindResult<?> UNBOUND = new BindResult<Object>(null);
    private final T value;

    private BindResult(T value) {
        this.value = value;
    }

    public T get() throws NoSuchElementException {
        if (this.value == null) {
            throw new NoSuchElementException("No value bound");
        }
        return this.value;
    }

    public boolean isBound() {
        return this.value != null;
    }

    public void ifBound(Consumer<? super T> consumer) {
        Assert.notNull(consumer, (String)"Consumer must not be null");
        if (this.value != null) {
            consumer.accept(this.value);
        }
    }

    public <U> BindResult<U> map(Function<? super T, ? extends U> mapper) {
        Assert.notNull(mapper, (String)"Mapper must not be null");
        return BindResult.of(this.value != null ? (T)mapper.apply((T)this.value) : null);
    }

    public T orElse(T other) {
        return this.value != null ? this.value : other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        return this.value != null ? this.value : other.get();
    }

    public T orElseCreate(Class<? extends T> type) {
        Assert.notNull(type, (String)"Type must not be null");
        return (T)(this.value != null ? this.value : BeanUtils.instantiateClass(type));
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws Throwable {
        if (this.value == null) {
            throw (Throwable)exceptionSupplier.get();
        }
        return this.value;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        return ObjectUtils.nullSafeEquals(this.value, ((BindResult)obj).value);
    }

    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.value);
    }

    static <T> BindResult<T> of(T value) {
        if (value == null) {
            return UNBOUND;
        }
        return new BindResult<T>(value);
    }
}

