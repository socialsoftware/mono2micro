/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceStatus
 *  org.apache.commons.collections.CollectionUtils
 */
package cn.springcloud.gray.servernode;

import cn.springcloud.gray.client.config.properties.GrayHoldoutServerProperties;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.servernode.ServerListProcessor;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public abstract class AbstractServerListProcessor<S>
implements ServerListProcessor<S> {
    protected GrayHoldoutServerProperties grayHoldoutServerProperties;

    public AbstractServerListProcessor(GrayHoldoutServerProperties grayHoldoutServerProperties) {
        this.grayHoldoutServerProperties = grayHoldoutServerProperties;
    }

    @Override
    public List<S> process(String serviceId, List<S> servers) {
        if (!this.grayHoldoutServerProperties.isEnabled() || CollectionUtils.isEmpty(this.getHoldoutInstanceStatus(serviceId))) {
            return servers;
        }
        return this.getServers(serviceId, servers);
    }

    protected List<InstanceStatus> getHoldoutInstanceStatus(String serviceId) {
        return this.grayHoldoutServerProperties.getServices().get(serviceId);
    }

    protected abstract List<S> getServers(String var1, List<S> var2);
}

