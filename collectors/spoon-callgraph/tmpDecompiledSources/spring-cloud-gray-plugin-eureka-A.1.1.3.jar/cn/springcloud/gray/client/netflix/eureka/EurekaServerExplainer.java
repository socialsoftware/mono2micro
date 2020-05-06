/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.servernode.ServerExplainer
 *  cn.springcloud.gray.servernode.ServerSpec
 *  cn.springcloud.gray.servernode.ServerSpec$ServerSpecBuilder
 *  com.netflix.loadbalancer.Server
 *  com.netflix.loadbalancer.Server$MetaInfo
 *  org.springframework.cloud.netflix.ribbon.DefaultServerIntrospector
 *  org.springframework.cloud.netflix.ribbon.ServerIntrospector
 *  org.springframework.cloud.netflix.ribbon.SpringClientFactory
 */
package cn.springcloud.gray.client.netflix.eureka;

import cn.springcloud.gray.servernode.ServerExplainer;
import cn.springcloud.gray.servernode.ServerSpec;
import com.netflix.loadbalancer.Server;
import java.util.Map;
import org.springframework.cloud.netflix.ribbon.DefaultServerIntrospector;
import org.springframework.cloud.netflix.ribbon.ServerIntrospector;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;

public class EurekaServerExplainer
implements ServerExplainer<Server> {
    private SpringClientFactory springClientFactory;

    public EurekaServerExplainer(SpringClientFactory springClientFactory) {
        this.springClientFactory = springClientFactory;
    }

    public ServerSpec apply(Server server) {
        Map<String, String> metadata = this.getServerMetadata(server.getMetaInfo().getServiceIdForDiscovery(), server);
        return ServerSpec.builder().instanceId(server.getMetaInfo().getInstanceId()).serviceId(server.getMetaInfo().getServiceIdForDiscovery()).metadatas(metadata).build();
    }

    public ServerIntrospector serverIntrospector(String serviceId) {
        if (this.springClientFactory == null) {
            return new DefaultServerIntrospector();
        }
        ServerIntrospector serverIntrospector = (ServerIntrospector)this.springClientFactory.getInstance(serviceId, ServerIntrospector.class);
        if (serverIntrospector == null) {
            serverIntrospector = new DefaultServerIntrospector();
        }
        return serverIntrospector;
    }

    public Map<String, String> getServerMetadata(String serviceId, Server server) {
        return this.serverIntrospector(serviceId).getMetadata(server);
    }
}

