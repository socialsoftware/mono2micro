/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.bind;

import cn.springcloud.gray.bean.properties.source.ConfigurationProperty;
import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnboundConfigurationPropertiesException
extends RuntimeException {
    private final Set<ConfigurationProperty> unboundProperties;

    public UnboundConfigurationPropertiesException(Set<ConfigurationProperty> unboundProperties) {
        super(UnboundConfigurationPropertiesException.buildMessage(unboundProperties));
        this.unboundProperties = Collections.unmodifiableSet(unboundProperties);
    }

    public Set<ConfigurationProperty> getUnboundProperties() {
        return this.unboundProperties;
    }

    private static String buildMessage(Set<ConfigurationProperty> unboundProperties) {
        StringBuilder builder = new StringBuilder();
        builder.append("The elements [");
        String message = unboundProperties.stream().map(p -> p.getName().toString()).collect(Collectors.joining(","));
        builder.append(message).append("] were left unbound.");
        return builder.toString();
    }
}

