/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.concurrent;

import cn.springcloud.gray.concurrent.GrayAsyncContext;
import java.util.concurrent.Callable;

public class GrayCallableContext
extends GrayAsyncContext {
    private Callable<?> target;

    public Callable<?> getTarget() {
        return this.target;
    }

    public void setTarget(Callable<?> target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GrayCallableContext)) {
            return false;
        }
        GrayCallableContext other = (GrayCallableContext)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Callable<?> this$target = this.getTarget();
        Callable<?> other$target = other.getTarget();
        return !(this$target == null ? other$target != null : !this$target.equals(other$target));
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof GrayCallableContext;
    }

    @Override
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Callable<?> $target = this.getTarget();
        result = result * 59 + ($target == null ? 43 : $target.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "GrayCallableContext(target=" + this.getTarget() + ")";
    }
}

