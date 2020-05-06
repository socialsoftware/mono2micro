/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.bind.handler;

import cn.springcloud.gray.bean.properties.bind.AbstractBindHandler;
import cn.springcloud.gray.bean.properties.bind.BindContext;
import cn.springcloud.gray.bean.properties.bind.BindHandler;
import cn.springcloud.gray.bean.properties.bind.Bindable;
import cn.springcloud.gray.bean.properties.bind.UnboundConfigurationPropertiesException;
import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import cn.springcloud.gray.bean.properties.source.IterableConfigurationPropertySource;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

public class NoUnboundElementsBindHandler
extends AbstractBindHandler {
    private final Set<ConfigurationPropertyName> boundNames = new HashSet<ConfigurationPropertyName>();
    private final Function<ConfigurationPropertySource, Boolean> filter;

    NoUnboundElementsBindHandler() {
        this(BindHandler.DEFAULT, configurationPropertySource -> true);
    }

    public NoUnboundElementsBindHandler(BindHandler parent) {
        this(parent, configurationPropertySource -> true);
    }

    public NoUnboundElementsBindHandler(BindHandler parent, Function<ConfigurationPropertySource, Boolean> filter) {
        super(parent);
        this.filter = filter;
    }

    @Override
    public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        this.boundNames.add(name);
        return super.onSuccess(name, target, context, result);
    }

    @Override
    public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) throws Exception {
        if (context.getDepth() == 0) {
            this.checkNoUnboundElements(name, context);
        }
    }

    private void checkNoUnboundElements(ConfigurationPropertyName name, BindContext context) {
        TreeSet<ConfigurationProperty> unbound = new TreeSet<ConfigurationProperty>();
        for (ConfigurationPropertySource source : context.getSources()) {
            if (!(source instanceof IterableConfigurationPropertySource) || !this.filter.apply(source).booleanValue()) continue;
            this.collectUnbound(name, unbound, (IterableConfigurationPropertySource)source);
        }
        if (!unbound.isEmpty()) {
            throw new UnboundConfigurationPropertiesException(unbound);
        }
    }

    private void collectUnbound(ConfigurationPropertyName name, Set<ConfigurationProperty> unbound, IterableConfigurationPropertySource source) {
        ConfigurationPropertySource filtered = source.filter(candidate -> this.isUnbound(name, (ConfigurationPropertyName)candidate));
        Iterator<ConfigurationPropertyName> iterator = filtered.iterator();
        while (iterator.hasNext()) {
            ConfigurationPropertyName unboundName = iterator.next();
            try {
                unbound.add(source.filter(candidate -> this.isUnbound(name, (ConfigurationPropertyName)candidate)).getConfigurationProperty(unboundName));
            }
            catch (Exception exception) {}
        }
    }

    private boolean isUnbound(ConfigurationPropertyName name, ConfigurationPropertyName candidate) {
        return name.isAncestorOf(candidate) && !this.boundNames.contains(candidate) && !this.isOverriddenCollectionElement(candidate);
    }

    private boolean isOverriddenCollectionElement(ConfigurationPropertyName candidate) {
        int length = candidate.getNumberOfElements();
        if (candidate.isNumericIndex(length - 1)) {
            ConfigurationPropertyName propertyName = candidate.chop(candidate.getNumberOfElements() - 1);
            return this.boundNames.contains(propertyName);
        }
        return false;
    }
}

