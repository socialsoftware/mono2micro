/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.request;

public interface LocalStorageLifeCycle {
    public void initContext();

    public void closeContext();

    public static class NoOpLocalStorageLifeCycle
    implements LocalStorageLifeCycle {
        @Override
        public void initContext() {
        }

        @Override
        public void closeContext() {
        }
    }

}

