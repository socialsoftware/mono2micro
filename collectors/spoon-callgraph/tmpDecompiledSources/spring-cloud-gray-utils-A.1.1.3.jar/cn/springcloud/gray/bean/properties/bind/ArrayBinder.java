/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.ResolvableType
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.properties.bind.AggregateElementBinder;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.bind.Binder;
import cn.springcloud.gray.bean.properties.bind.IndexedElementsBinder;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.core.ResolvableType;

class ArrayBinder
extends IndexedElementsBinder<Object> {
    ArrayBinder(Binder.Context context) {
        super(context);
    }

    @Override
    protected Object bindAggregate(ConfigurationPropertyName name, Bindable<?> target, AggregateElementBinder elementBinder) {
        IndexedElementsBinder.IndexedCollectionSupplier result = new IndexedElementsBinder.IndexedCollectionSupplier(ArrayList::new);
        ResolvableType aggregateType = target.getType();
        ResolvableType elementType = target.getType().getComponentType();
        this.bindIndexed(name, target, elementBinder, aggregateType, elementType, result);
        if (result.wasSupplied()) {
            List list = (List)result.get();
            Object array = Array.newInstance(elementType.resolve(), list.size());
            for (int i = 0; i < list.size(); ++i) {
                Array.set(array, i, list.get(i));
            }
            return array;
        }
        return null;
    }

    @Override
    protected Object merge(Supplier<Object> existing, Object additional) {
        return additional;
    }
}

