/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.concurrent;

import cn.springcloud.gray.concurrent.GrayAsyncContext;

public class GrayRunnableContext
extends GrayAsyncContext {
    private Runnable target;

    public Runnable getTarget() {
        return this.target;
    }

    public void setTarget(Runnable target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GrayRunnableContext)) {
            return false;
        }
        GrayRunnableContext other = (GrayRunnableContext)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Runnable this$target = this.getTarget();
        Runnable other$target = other.getTarget();
        return !(this$target == null ? other$target != null : !this$target.equals(other$target));
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof GrayRunnableContext;
    }

    @Override
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Runnable $target = this.getTarget();
        result = result * 59 + ($target == null ? 43 : $target.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "GrayRunnableContext(target=" + this.getTarget() + ")";
    }
}

