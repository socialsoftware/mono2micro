/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.concurrent;

import cn.springcloud.gray.concurrent.GrayConcurrentHelper;
import java.util.concurrent.Executor;

public class GrayExecutor
implements Executor {
    private Executor delegater;

    public GrayExecutor(Executor delegater) {
        this.delegater = delegater;
    }

    @Override
    public void execute(Runnable command) {
        this.delegater.execute(GrayConcurrentHelper.createDelegateRunnable(command));
    }

    public Executor getDelegater() {
        return this.delegater;
    }
}

