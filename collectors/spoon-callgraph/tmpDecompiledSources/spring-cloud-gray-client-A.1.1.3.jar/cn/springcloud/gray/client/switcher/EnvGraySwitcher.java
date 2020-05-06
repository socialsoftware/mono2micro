/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.client.switcher;

import cn.springcloud.gray.client.config.properties.GrayProperties;
import cn.springcloud.gray.client.switcher.GraySwitcher;

public class EnvGraySwitcher
implements GraySwitcher {
    private GrayProperties grayProperties;

    public EnvGraySwitcher(GrayProperties grayProperties) {
        this.grayProperties = grayProperties;
    }

    @Override
    public boolean state() {
        return this.grayProperties.isEnabled();
    }

    @Override
    public boolean isEanbleGrayRouting() {
        return this.grayProperties.isGrayRouting();
    }
}

