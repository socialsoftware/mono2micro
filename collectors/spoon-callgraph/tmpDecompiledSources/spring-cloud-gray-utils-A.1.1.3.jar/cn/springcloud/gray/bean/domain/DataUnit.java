/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.domain;

import cn.springcloud.gray.bean.domain.DataSize;
import java.util.Objects;

public enum DataUnit {
    BYTES("B", DataSize.ofBytes(1L)),
    KILOBYTES("KB", DataSize.ofKilobytes(1L)),
    MEGABYTES("MB", DataSize.ofMegabytes(1L)),
    GIGABYTES("GB", DataSize.ofGigabytes(1L)),
    TERABYTES("TB", DataSize.ofTerabytes(1L));
    
    private final String suffix;
    private final DataSize size;

    private DataUnit(String suffix, DataSize size) {
        this.suffix = suffix;
        this.size = size;
    }

    DataSize size() {
        return this.size;
    }

    public static DataUnit fromSuffix(String suffix) {
        for (DataUnit candidate : DataUnit.values()) {
            if (!Objects.equals(candidate.suffix, suffix)) continue;
            return candidate;
        }
        throw new IllegalArgumentException("Unknown unit '" + suffix + "'");
    }
}

