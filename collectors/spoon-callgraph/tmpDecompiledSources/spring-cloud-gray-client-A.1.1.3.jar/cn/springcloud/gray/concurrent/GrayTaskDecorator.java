/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.task.TaskDecorator
 */
package cn.springcloud.gray.concurrent;

import cn.springcloud.gray.concurrent.GrayConcurrentHelper;
import org.springframework.core.task.TaskDecorator;

public class GrayTaskDecorator
implements TaskDecorator {
    public Runnable decorate(Runnable runnable) {
        return GrayConcurrentHelper.createDelegateRunnable(runnable);
    }
}

