/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.routing.connectionpoint;

import cn.springcloud.gray.routing.connectionpoint.RoutingConnectPointContext;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public interface RoutingConnectionPoint {
    default public <T> T execute(RoutingConnectPointContext connectPointContext, Supplier<T> supplier) {
        try {
            this.executeConnectPoint(connectPointContext);
            T t = supplier.get();
            return t;
        }
        catch (Exception e) {
            connectPointContext.setThrowable(e);
            throw new RuntimeException(e);
        }
        finally {
            this.shutdownconnectPoint(connectPointContext);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    default public <T> T execute(RoutingConnectPointContext connectPointContext, Supplier<T> supplier, Consumer<RoutingConnectPointContext> finalConsumer) {
        try {
            T t = this.execute(connectPointContext, supplier);
            return t;
        }
        finally {
            if (!Objects.isNull(finalConsumer)) {
                finalConsumer.accept(connectPointContext);
            }
        }
    }

    public void executeConnectPoint(RoutingConnectPointContext var1);

    public void shutdownconnectPoint(RoutingConnectPointContext var1);

    public static interface Supplier<T> {
        public T get() throws IOException;
    }

}

