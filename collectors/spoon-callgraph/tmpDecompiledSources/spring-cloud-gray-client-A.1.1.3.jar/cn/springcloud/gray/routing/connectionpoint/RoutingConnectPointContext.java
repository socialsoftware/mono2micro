/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.routing.connectionpoint;

import cn.springcloud.gray.request.GrayRequest;

public class RoutingConnectPointContext {
    private static final ThreadLocal<RoutingConnectPointContext> contextLocal = new ThreadLocal();
    private GrayRequest grayRequest;
    private Throwable throwable;
    private String interceptroType;

    static void setContextLocal(RoutingConnectPointContext cxt) {
        contextLocal.set(cxt);
    }

    static void removeContextLocal() {
        contextLocal.remove();
    }

    public static RoutingConnectPointContext getContextLocal() {
        return contextLocal.get();
    }

    public static RoutingConnectPointContextBuilder builder() {
        return new RoutingConnectPointContextBuilder();
    }

    public GrayRequest getGrayRequest() {
        return this.grayRequest;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public String getInterceptroType() {
        return this.interceptroType;
    }

    public RoutingConnectPointContext(GrayRequest grayRequest, Throwable throwable, String interceptroType) {
        this.grayRequest = grayRequest;
        this.throwable = throwable;
        this.interceptroType = interceptroType;
    }

    public RoutingConnectPointContext() {
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public static class RoutingConnectPointContextBuilder {
        private GrayRequest grayRequest;
        private Throwable throwable;
        private String interceptroType;

        RoutingConnectPointContextBuilder() {
        }

        public RoutingConnectPointContextBuilder grayRequest(GrayRequest grayRequest) {
            this.grayRequest = grayRequest;
            return this;
        }

        public RoutingConnectPointContextBuilder throwable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        public RoutingConnectPointContextBuilder interceptroType(String interceptroType) {
            this.interceptroType = interceptroType;
            return this;
        }

        public RoutingConnectPointContext build() {
            return new RoutingConnectPointContext(this.grayRequest, this.throwable, this.interceptroType);
        }

        public String toString() {
            return "RoutingConnectPointContext.RoutingConnectPointContextBuilder(grayRequest=" + this.grayRequest + ", throwable=" + this.throwable + ", interceptroType=" + this.interceptroType + ")";
        }
    }

}

