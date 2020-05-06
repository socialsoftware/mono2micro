/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.decision;

import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.servernode.ServerSpec;

public class GrayDecisionInputArgs {
    private ServerSpec server;
    private GrayRequest grayRequest;

    public static GrayDecisionInputArgsBuilder builder() {
        return new GrayDecisionInputArgsBuilder();
    }

    public ServerSpec getServer() {
        return this.server;
    }

    public GrayRequest getGrayRequest() {
        return this.grayRequest;
    }

    public GrayDecisionInputArgs() {
    }

    public GrayDecisionInputArgs(ServerSpec server, GrayRequest grayRequest) {
        this.server = server;
        this.grayRequest = grayRequest;
    }

    public static class GrayDecisionInputArgsBuilder {
        private ServerSpec server;
        private GrayRequest grayRequest;

        GrayDecisionInputArgsBuilder() {
        }

        public GrayDecisionInputArgsBuilder server(ServerSpec server) {
            this.server = server;
            return this;
        }

        public GrayDecisionInputArgsBuilder grayRequest(GrayRequest grayRequest) {
            this.grayRequest = grayRequest;
            return this;
        }

        public GrayDecisionInputArgs build() {
            return new GrayDecisionInputArgs(this.server, this.grayRequest);
        }

        public String toString() {
            return "GrayDecisionInputArgs.GrayDecisionInputArgsBuilder(server=" + this.server + ", grayRequest=" + this.grayRequest + ")";
        }
    }

}

