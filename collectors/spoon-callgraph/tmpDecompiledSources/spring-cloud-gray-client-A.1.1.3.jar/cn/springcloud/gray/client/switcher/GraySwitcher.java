/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.client.switcher;

public interface GraySwitcher {
    public boolean state();

    public boolean isEanbleGrayRouting();

    public static class DefaultGraySwitcher
    implements GraySwitcher {
        @Override
        public boolean state() {
            return true;
        }

        @Override
        public boolean isEanbleGrayRouting() {
            return true;
        }
    }

}

