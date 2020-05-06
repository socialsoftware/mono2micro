/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.servernode;

import java.util.List;

public interface ServerListProcessor<SRV> {
    public List<SRV> process(String var1, List<SRV> var2);

    public static class Default<SRV>
    implements ServerListProcessor<SRV> {
        @Override
        public List<SRV> process(String serviceId, List<SRV> servers) {
            return servers;
        }
    }

}

