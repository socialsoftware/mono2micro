/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.module.client;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ClientRemoteModule {
    public String getClientPath(String var1, String var2);

    public <T> T callClient(String var1, String var2, String var3, Function<String, T> var4);

    public void callClient(String var1, String var2, String var3, Consumer<String> var4);
}

