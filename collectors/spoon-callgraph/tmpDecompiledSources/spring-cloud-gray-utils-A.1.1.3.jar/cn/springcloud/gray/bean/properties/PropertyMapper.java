/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.Assert
 *  org.springframework.util.StringUtils
 */
package cn.springcloud.gray.bean.properties;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public final class PropertyMapper {
    private static final Predicate<?> ALWAYS = t -> true;
    private static final PropertyMapper INSTANCE = new PropertyMapper(null, null);
    private final PropertyMapper parent;
    private final SourceOperator sourceOperator;

    private PropertyMapper(PropertyMapper parent, SourceOperator sourceOperator) {
        this.parent = parent;
        this.sourceOperator = sourceOperator;
    }

    public PropertyMapper alwaysApplyingWhenNonNull() {
        return this.alwaysApplying(this::whenNonNull);
    }

    private <T> Source<T> whenNonNull(Source<T> source) {
        return source.whenNonNull();
    }

    public PropertyMapper alwaysApplying(SourceOperator operator) {
        Assert.notNull((Object)operator, (String)"Operator must not be null");
        return new PropertyMapper(this, operator);
    }

    public <T> Source<T> from(Supplier<T> supplier) {
        Assert.notNull(supplier, (String)"Supplier must not be null");
        Source<T> source = this.getSource(supplier);
        if (this.sourceOperator != null) {
            source = this.sourceOperator.apply(source);
        }
        return source;
    }

    public <T> Source<T> from(T value) {
        return this.from((T)((Supplier<Object>)() -> value));
    }

    private <T> Source<T> getSource(Supplier<T> supplier) {
        if (this.parent != null) {
            return this.parent.from((T)supplier);
        }
        return new Source(new CachingSupplier<T>(supplier), ALWAYS);
    }

    public static PropertyMapper get() {
        return INSTANCE;
    }

    private static class NullPointerExceptionSafeSupplier<T>
    implements Supplier<T> {
        private final Supplier<T> supplier;

        NullPointerExceptionSafeSupplier(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            try {
                return this.supplier.get();
            }
            catch (NullPointerException ex) {
                return null;
            }
        }
    }

    public static final class Source<T> {
        private final Supplier<T> supplier;
        private final Predicate<T> predicate;

        private Source(Supplier<T> supplier, Predicate<T> predicate) {
            Assert.notNull(predicate, (String)"Predicate must not be null");
            this.supplier = supplier;
            this.predicate = predicate;
        }

        public <R extends Number> Source<Integer> asInt(Function<T, R> adapter) {
            return this.as(adapter).as(Number::intValue);
        }

        public <R> Source<R> as(Function<T, R> adapter) {
            Assert.notNull(adapter, (String)"Adapter must not be null");
            Supplier<Boolean> test = () -> this.predicate.test(this.supplier.get());
            Predicate<Object> predicate = t -> (Boolean)test.get();
            Supplier<Object> supplier = () -> {
                if (((Boolean)test.get()).booleanValue()) {
                    return adapter.apply(this.supplier.get());
                }
                return null;
            };
            return new Source<Object>(supplier, predicate);
        }

        public Source<T> whenNonNull() {
            return new Source<Object>(new NullPointerExceptionSafeSupplier<T>(this.supplier), Objects::nonNull);
        }

        public Source<T> whenTrue() {
            return this.when(Boolean.TRUE::equals);
        }

        public Source<T> whenFalse() {
            return this.when(Boolean.FALSE::equals);
        }

        public Source<T> whenHasText() {
            return this.when(value -> StringUtils.hasText((String)Objects.toString(value, null)));
        }

        public Source<T> whenEqualTo(Object object) {
            return this.when(object::equals);
        }

        public <R extends T> Source<R> whenInstanceOf(Class<R> target) {
            return this.when(target::isInstance).as(target::cast);
        }

        public Source<T> whenNot(Predicate<T> predicate) {
            Assert.notNull(predicate, (String)"Predicate must not be null");
            return new Source<T>(this.supplier, predicate.negate());
        }

        public Source<T> when(Predicate<T> predicate) {
            Assert.notNull(predicate, (String)"Predicate must not be null");
            return new Source<T>(this.supplier, predicate);
        }

        public void to(Consumer<T> consumer) {
            Assert.notNull(consumer, (String)"Consumer must not be null");
            T value = this.supplier.get();
            if (this.predicate.test(value)) {
                consumer.accept(value);
            }
        }

        public <R> R toInstance(Function<T, R> factory) {
            Assert.notNull(factory, (String)"Factory must not be null");
            T value = this.supplier.get();
            if (!this.predicate.test(value)) {
                throw new NoSuchElementException("No value present");
            }
            return factory.apply(value);
        }

        public void toCall(Runnable runnable) {
            Assert.notNull((Object)runnable, (String)"Runnable must not be null");
            T value = this.supplier.get();
            if (this.predicate.test(value)) {
                runnable.run();
            }
        }
    }

    @FunctionalInterface
    public static interface SourceOperator {
        public <T> Source<T> apply(Source<T> var1);
    }

    private static class CachingSupplier<T>
    implements Supplier<T> {
        private final Supplier<T> supplier;
        private boolean hasResult;
        private T result;

        CachingSupplier(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (!this.hasResult) {
                this.result = this.supplier.get();
                this.hasResult = true;
            }
            return this.result;
        }
    }

}

