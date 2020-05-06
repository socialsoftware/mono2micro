/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.GrayClientHolder
 *  cn.springcloud.gray.ServerChooser
 *  cn.springcloud.gray.choose.ListChooser
 *  com.google.common.base.Optional
 *  com.netflix.loadbalancer.AbstractServerPredicate
 *  com.netflix.loadbalancer.ILoadBalancer
 *  com.netflix.loadbalancer.Server
 *  com.netflix.loadbalancer.ZoneAvoidanceRule
 */
package cn.springcloud.gray.client.netflix.ribbon;

import cn.springcloud.gray.GrayClientHolder;
import cn.springcloud.gray.ServerChooser;
import cn.springcloud.gray.choose.ListChooser;
import com.google.common.base.Optional;
import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import java.util.List;

public class GrayChooserRule
extends ZoneAvoidanceRule {
    private ServerChooser<Server> serverChooser = GrayClientHolder.getServerChooser();

    public Server choose(Object key) {
        return (Server)this.serverChooser.chooseServer(this.getLoadBalancer().getAllServers(), servers -> {
            Optional server = this.getPredicate().chooseRoundRobinAfterFiltering(servers, key);
            if (server.isPresent()) {
                return (Server)server.get();
            }
            return null;
        });
    }
}

