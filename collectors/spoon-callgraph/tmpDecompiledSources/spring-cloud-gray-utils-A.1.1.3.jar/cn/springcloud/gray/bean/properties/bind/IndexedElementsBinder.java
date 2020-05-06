/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.ResolvableType
 *  org.springframework.util.LinkedMultiValueMap
 *  org.springframework.util.MultiValueMap
 *  org.springframework.util.StringUtils
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.properties.bind.AggregateBinder;
import cn.springcloud.gray.bean.properties.bind.AggregateElementBinder;
import cn.springcloud.gray.bean.properties.bind.BindConverter;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.bind.Binder;
import cn.springcloud.gray.bean.properties.bind.PlaceholdersResolver;
import cn.springcloud.gray.bean.properties.bind.UnboundConfigurationPropertiesException;
import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.IterableConfigurationPropertySource;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.ResolvableType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

abstract class IndexedElementsBinder<T>
extends AggregateBinder<T> {
    private static final String INDEX_ZERO = "[0]";

    IndexedElementsBinder(Binder.Context context) {
        super(context);
    }

    @Override
    protected boolean isAllowRecursiveBinding(ConfigurationPropertySource source) {
        return source == null || source instanceof IterableConfigurationPropertySource;
    }

    protected final void bindIndexed(ConfigurationPropertyName name, Bindable<?> target, AggregateElementBinder elementBinder, ResolvableType aggregateType, ResolvableType elementType, IndexedCollectionSupplier result) {
        for (ConfigurationPropertySource source : this.getContext().getSources()) {
            this.bindIndexed(source, name, target, elementBinder, result, aggregateType, elementType);
            if (!result.wasSupplied() || result.get() == null) continue;
            return;
        }
    }

    private void bindIndexed(ConfigurationPropertySource source, ConfigurationPropertyName root, Bindable<?> target, AggregateElementBinder elementBinder, IndexedCollectionSupplier collection, ResolvableType aggregateType, ResolvableType elementType) {
        ConfigurationProperty property = source.getConfigurationProperty(root);
        if (property != null) {
            this.bindValue(target, (Collection)collection.get(), aggregateType, elementType, property.getValue());
        } else {
            this.bindIndexed(source, root, elementBinder, collection, elementType);
        }
    }

    private void bindValue(Bindable<?> target, Collection<Object> collection, ResolvableType aggregateType, ResolvableType elementType, Object value) {
        if (value instanceof String && !StringUtils.hasText((String)((String)value))) {
            return;
        }
        C aggregate = this.convert(value, aggregateType, target.getAnnotations());
        ResolvableType collectionType = ResolvableType.forClassWithGenerics(collection.getClass(), (ResolvableType[])new ResolvableType[]{elementType});
        Collection elements = (Collection)this.convert(aggregate, collectionType, new Annotation[0]);
        collection.addAll(elements);
    }

    private void bindIndexed(ConfigurationPropertySource source, ConfigurationPropertyName root, AggregateElementBinder elementBinder, IndexedCollectionSupplier collection, ResolvableType elementType) {
        ConfigurationPropertyName name;
        Object value;
        MultiValueMap<String, ConfigurationProperty> knownIndexedChildren = this.getKnownIndexedChildren(source, root);
        for (int i = 0; i < Integer.MAX_VALUE && (value = elementBinder.bind(name = root.append(i != 0 ? "[" + i + "]" : INDEX_ZERO), Bindable.of(elementType), source)) != null; ++i) {
            knownIndexedChildren.remove((Object)name.getLastElement(ConfigurationPropertyName.Form.UNIFORM));
            ((Collection)collection.get()).add(value);
        }
        this.assertNoUnboundChildren(knownIndexedChildren);
    }

    private MultiValueMap<String, ConfigurationProperty> getKnownIndexedChildren(ConfigurationPropertySource source, ConfigurationPropertyName root) {
        LinkedMultiValueMap children = new LinkedMultiValueMap();
        if (!(source instanceof IterableConfigurationPropertySource)) {
            return children;
        }
        for (ConfigurationPropertyName name : (IterableConfigurationPropertySource)source.filter(root::isAncestorOf)) {
            ConfigurationPropertyName choppedName = name.chop(root.getNumberOfElements() + 1);
            if (!choppedName.isLastElementIndexed()) continue;
            String key = choppedName.getLastElement(ConfigurationPropertyName.Form.UNIFORM);
            ConfigurationProperty value = source.getConfigurationProperty(name);
            children.add((Object)key, (Object)value);
        }
        return children;
    }

    private void assertNoUnboundChildren(MultiValueMap<String, ConfigurationProperty> children) {
        if (!children.isEmpty()) {
            throw new UnboundConfigurationPropertiesException(children.values().stream().flatMap(Collection::stream).collect(Collectors.toCollection(TreeSet::new)));
        }
    }

    private <C> C convert(Object value, ResolvableType type, Annotation ... annotations) {
        value = this.getContext().getPlaceholdersResolver().resolvePlaceholders(value);
        return (C)this.getContext().getConverter().convert(value, type, annotations);
    }

    protected static class IndexedCollectionSupplier
    extends AggregateBinder.AggregateSupplier<Collection<Object>> {
        public IndexedCollectionSupplier(Supplier<Collection<Object>> supplier) {
            super(supplier);
        }
    }

}

