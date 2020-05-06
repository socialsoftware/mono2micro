/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package cn.springcloud.gray;

import cn.springcloud.gray.GrayClientHolder;
import cn.springcloud.gray.ServerListResult;
import cn.springcloud.gray.choose.ListChooser;
import cn.springcloud.gray.servernode.ServerSpec;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public interface ServerChooser<Server> {
    public boolean matchGrayDecisions(ServerSpec var1);

    public boolean matchGrayDecisions(Server var1);

    public ServerListResult<Server> distinguishServerList(List<Server> var1);

    public ServerListResult<Server> distinguishAndMatchGrayServerList(List<Server> var1);

    default public Server chooseServer(List<Server> servers, ListChooser<Server> chooser) {
        Server server;
        ServerListResult<Server> serverListResult = this.distinguishAndMatchGrayServerList(servers);
        if (serverListResult == null) {
            return chooser.choose(servers);
        }
        if (GrayClientHolder.getGraySwitcher().isEanbleGrayRouting() && CollectionUtils.isNotEmpty(serverListResult.getGrayServers()) && (server = chooser.choose(serverListResult.getGrayServers())) != null) {
            return server;
        }
        return chooser.choose(serverListResult.getNormalServers());
    }
}

