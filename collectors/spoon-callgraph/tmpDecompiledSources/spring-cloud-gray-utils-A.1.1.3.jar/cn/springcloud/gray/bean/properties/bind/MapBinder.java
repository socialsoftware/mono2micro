/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.CollectionFactory
 *  org.springframework.core.ResolvableType
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.properties.bind.AggregateBinder;
import cn.springcloud.gray.bean.properties.bind.AggregateElementBinder;
import cn.springcloud.gray.bean.properties.bind.BindConverter;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.bind.Binder;
import cn.springcloud.gray.bean.properties.bind.PlaceholdersResolver;
import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyState;
import cn.springcloud.gray.bean.properties.source.IterableConfigurationPropertySource;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.springframework.core.CollectionFactory;
import org.springframework.core.ResolvableType;

class MapBinder
extends AggregateBinder<Map<Object, Object>> {
    private static final Bindable<Map<String, String>> STRING_STRING_MAP = Bindable.mapOf(String.class, String.class);

    MapBinder(Binder.Context context) {
        super(context);
    }

    @Override
    protected boolean isAllowRecursiveBinding(ConfigurationPropertySource source) {
        return true;
    }

    @Override
    protected Object bindAggregate(ConfigurationPropertyName name, Bindable<?> target, AggregateElementBinder elementBinder) {
        Map map = CollectionFactory.createMap(target.getValue() != null ? Map.class : target.getType().resolve(Object.class), (int)0);
        Bindable<?> resolvedTarget = this.resolveTarget(target);
        boolean hasDescendants = this.hasDescendants(name);
        for (ConfigurationPropertySource source : this.getContext().getSources()) {
            if (!ConfigurationPropertyName.EMPTY.equals(name)) {
                ConfigurationProperty property = source.getConfigurationProperty(name);
                if (property != null && !hasDescendants) {
                    return this.getContext().getConverter().convert(property.getValue(), target);
                }
                source = source.filter(name::isAncestorOf);
            }
            new EntryBinder(name, resolvedTarget, elementBinder).bindEntries(source, map);
        }
        return map.isEmpty() ? null : map;
    }

    private boolean hasDescendants(ConfigurationPropertyName name) {
        for (ConfigurationPropertySource source : this.getContext().getSources()) {
            if (source.containsDescendantOf(name) != ConfigurationPropertyState.PRESENT) continue;
            return true;
        }
        return false;
    }

    private Bindable<?> resolveTarget(Bindable<?> target) {
        Class type = target.getType().resolve(Object.class);
        if (Properties.class.isAssignableFrom(type)) {
            return STRING_STRING_MAP;
        }
        return target;
    }

    @Override
    protected Map<Object, Object> merge(Supplier<Map<Object, Object>> existing, Map<Object, Object> additional) {
        Map<Object, Object> existingMap = this.getExistingIfPossible(existing);
        if (existingMap == null) {
            return additional;
        }
        try {
            existingMap.putAll(additional);
            return this.copyIfPossible(existingMap);
        }
        catch (UnsupportedOperationException ex) {
            Map<Object, Object> result = this.createNewMap(additional.getClass(), existingMap);
            result.putAll(additional);
            return result;
        }
    }

    private Map<Object, Object> getExistingIfPossible(Supplier<Map<Object, Object>> existing) {
        try {
            return existing.get();
        }
        catch (Exception ex) {
            return null;
        }
    }

    private Map<Object, Object> copyIfPossible(Map<Object, Object> map) {
        try {
            return this.createNewMap(map.getClass(), map);
        }
        catch (Exception ex) {
            return map;
        }
    }

    private Map<Object, Object> createNewMap(Class<?> mapClass, Map<Object, Object> map) {
        Map result = CollectionFactory.createMap(mapClass, (int)map.size());
        result.putAll(map);
        return result;
    }

    private class EntryBinder {
        private final ConfigurationPropertyName root;
        private final AggregateElementBinder elementBinder;
        private final ResolvableType mapType;
        private final ResolvableType keyType;
        private final ResolvableType valueType;

        EntryBinder(ConfigurationPropertyName root, Bindable<?> target, AggregateElementBinder elementBinder) {
            this.root = root;
            this.elementBinder = elementBinder;
            this.mapType = target.getType().asMap();
            this.keyType = this.mapType.getGeneric(new int[]{0});
            this.valueType = this.mapType.getGeneric(new int[]{1});
        }

        public void bindEntries(ConfigurationPropertySource source, Map<Object, Object> map) {
            if (source instanceof IterableConfigurationPropertySource) {
                for (ConfigurationPropertyName name : (IterableConfigurationPropertySource)source) {
                    Bindable<?> valueBindable = this.getValueBindable(name);
                    ConfigurationPropertyName entryName = this.getEntryName(source, name);
                    Object key = MapBinder.this.getContext().getConverter().convert(this.getKeyName(entryName), this.keyType, new Annotation[0]);
                    map.computeIfAbsent(key, k -> this.elementBinder.bind(entryName, valueBindable));
                }
            }
        }

        private Bindable<?> getValueBindable(ConfigurationPropertyName name) {
            if (!this.root.isParentOf(name) && this.isValueTreatedAsNestedMap()) {
                return Bindable.of(this.mapType);
            }
            return Bindable.of(this.valueType);
        }

        private ConfigurationPropertyName getEntryName(ConfigurationPropertySource source, ConfigurationPropertyName name) {
            Class resolved = this.valueType.resolve(Object.class);
            if (Collection.class.isAssignableFrom(resolved) || this.valueType.isArray()) {
                return this.chopNameAtNumericIndex(name);
            }
            if (!(this.root.isParentOf(name) || !this.isValueTreatedAsNestedMap() && this.isScalarValue(source, name))) {
                return name.chop(this.root.getNumberOfElements() + 1);
            }
            return name;
        }

        private ConfigurationPropertyName chopNameAtNumericIndex(ConfigurationPropertyName name) {
            int start = this.root.getNumberOfElements() + 1;
            int size = name.getNumberOfElements();
            for (int i = start; i < size; ++i) {
                if (!name.isNumericIndex(i)) continue;
                return name.chop(i);
            }
            return name;
        }

        private boolean isValueTreatedAsNestedMap() {
            return Object.class.equals((Object)this.valueType.resolve(Object.class));
        }

        private boolean isScalarValue(ConfigurationPropertySource source, ConfigurationPropertyName name) {
            Class resolved = this.valueType.resolve(Object.class);
            if (!resolved.getName().startsWith("java.lang") && !resolved.isEnum()) {
                return false;
            }
            ConfigurationProperty property = source.getConfigurationProperty(name);
            if (property == null) {
                return false;
            }
            Object value = property.getValue();
            value = MapBinder.this.getContext().getPlaceholdersResolver().resolvePlaceholders(value);
            return MapBinder.this.getContext().getConverter().canConvert(value, this.valueType, new Annotation[0]);
        }

        private String getKeyName(ConfigurationPropertyName name) {
            StringBuilder result = new StringBuilder();
            for (int i = this.root.getNumberOfElements(); i < name.getNumberOfElements(); ++i) {
                if (result.length() != 0) {
                    result.append('.');
                }
                result.append(name.getElement(i, ConfigurationPropertyName.Form.ORIGINAL));
            }
            return result.toString();
        }
    }

}

