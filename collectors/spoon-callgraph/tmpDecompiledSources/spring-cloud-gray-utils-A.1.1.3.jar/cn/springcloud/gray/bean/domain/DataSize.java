/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.Assert
 *  org.springframework.util.StringUtils
 */
package cn.springcloud.gray.bean.domain;

import cn.springcloud.gray.bean.domain.DataUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public final class DataSize
implements Comparable<DataSize> {
    private static final Pattern PATTERN = Pattern.compile("^([+\\-]?\\d+)([a-zA-Z]{0,2})$");
    private static long BYTES_PER_KB = 1024L;
    private static long BYTES_PER_MB = BYTES_PER_KB * 1024L;
    private static long BYTES_PER_GB = BYTES_PER_MB * 1024L;
    private static long BYTES_PER_TB = BYTES_PER_GB * 1024L;
    private final long bytes;

    private DataSize(long bytes) {
        this.bytes = bytes;
    }

    public static DataSize ofBytes(long bytes) {
        return new DataSize(bytes);
    }

    public static DataSize ofKilobytes(long kilobytes) {
        return new DataSize(Math.multiplyExact(kilobytes, BYTES_PER_KB));
    }

    public static DataSize ofMegabytes(long megabytes) {
        return new DataSize(Math.multiplyExact(megabytes, BYTES_PER_MB));
    }

    public static DataSize ofGigabytes(long gigabytes) {
        return new DataSize(Math.multiplyExact(gigabytes, BYTES_PER_GB));
    }

    public static DataSize ofTerabytes(long terabytes) {
        return new DataSize(Math.multiplyExact(terabytes, BYTES_PER_TB));
    }

    public static DataSize of(long amount, DataUnit unit) {
        Assert.notNull((Object)((Object)unit), (String)"Unit must not be null");
        return new DataSize(Math.multiplyExact(amount, unit.size().toBytes()));
    }

    public static DataSize parse(CharSequence text) {
        return DataSize.parse(text, null);
    }

    public static DataSize parse(CharSequence text, DataUnit defaultUnit) {
        Assert.notNull((Object)text, (String)"Text must not be null");
        try {
            Matcher matcher = PATTERN.matcher(text);
            Assert.state((boolean)matcher.matches(), (String)"Does not match data size pattern");
            DataUnit unit = DataSize.determineDataUnit(matcher.group(2), defaultUnit);
            long amount = Long.parseLong(matcher.group(1));
            return DataSize.of(amount, unit);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException("'" + text + "' is not a valid data size", ex);
        }
    }

    private static DataUnit determineDataUnit(String suffix, DataUnit defaultUnit) {
        DataUnit defaultUnitToUse = defaultUnit != null ? defaultUnit : DataUnit.BYTES;
        return StringUtils.hasLength((String)suffix) ? DataUnit.fromSuffix(suffix) : defaultUnitToUse;
    }

    public boolean isNegative() {
        return this.bytes < 0L;
    }

    public long toBytes() {
        return this.bytes;
    }

    public long toKilobytes() {
        return this.bytes / BYTES_PER_KB;
    }

    public long toMegabytes() {
        return this.bytes / BYTES_PER_MB;
    }

    public long toGigabytes() {
        return this.bytes / BYTES_PER_GB;
    }

    public long toTerabytes() {
        return this.bytes / BYTES_PER_TB;
    }

    @Override
    public int compareTo(DataSize other) {
        return Long.compare(this.bytes, other.bytes);
    }

    public String toString() {
        return String.format("%dB", this.bytes);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        DataSize otherSize = (DataSize)other;
        return this.bytes == otherSize.bytes;
    }

    public int hashCode() {
        return Long.hashCode(this.bytes);
    }
}

