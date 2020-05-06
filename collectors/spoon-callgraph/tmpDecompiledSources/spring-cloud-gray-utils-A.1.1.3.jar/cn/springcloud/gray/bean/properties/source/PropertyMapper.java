/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.ConfigurationPropertyName;
import cn.springcloud.gray.bean.properties.source.PropertyMapping;

interface PropertyMapper {
    public static final PropertyMapping[] NO_MAPPINGS = new PropertyMapping[0];

    public PropertyMapping[] map(ConfigurationPropertyName var1);

    public PropertyMapping[] map(String var1);
}

