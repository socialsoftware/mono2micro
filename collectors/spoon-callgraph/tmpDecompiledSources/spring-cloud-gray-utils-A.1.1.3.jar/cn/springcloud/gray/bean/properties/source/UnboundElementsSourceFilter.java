/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.env.PropertySource
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.ConfigurationPropertySource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import org.springframework.core.env.PropertySource;

public class UnboundElementsSourceFilter
implements Function<ConfigurationPropertySource, Boolean> {
    private static final Set<String> BENIGN_PROPERTY_SOURCE_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("systemEnvironment", "systemProperties")));

    @Override
    public Boolean apply(ConfigurationPropertySource configurationPropertySource) {
        Object underlyingSource = configurationPropertySource.getUnderlyingSource();
        if (underlyingSource instanceof PropertySource) {
            String name = ((PropertySource)underlyingSource).getName();
            return !BENIGN_PROPERTY_SOURCE_NAMES.contains(name);
        }
        return true;
    }
}

