/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.ObjectUtils
 */
package cn.springcloud.gray.bean.origin;

import cn.springcloud.gray.bean.origin.Origin;
import cn.springcloud.gray.bean.origin.OriginProvider;
import org.springframework.util.ObjectUtils;

public class OriginTrackedValue
implements OriginProvider {
    private final Object value;
    private final Origin origin;

    private OriginTrackedValue(Object value, Origin origin) {
        this.value = value;
        this.origin = origin;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public Origin getOrigin() {
        return this.origin;
    }

    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return ObjectUtils.nullSafeEquals((Object)this.value, (Object)((OriginTrackedValue)obj).value);
    }

    public int hashCode() {
        return ObjectUtils.nullSafeHashCode((Object)this.value);
    }

    public String toString() {
        return this.value != null ? this.value.toString() : null;
    }

    public static OriginTrackedValue of(Object value) {
        return OriginTrackedValue.of(value, null);
    }

    public static OriginTrackedValue of(Object value, Origin origin) {
        if (value == null) {
            return null;
        }
        if (value instanceof CharSequence) {
            return new OriginTrackedCharSequence((CharSequence)value, origin);
        }
        return new OriginTrackedValue(value, origin);
    }

    private static class OriginTrackedCharSequence
    extends OriginTrackedValue
    implements CharSequence {
        OriginTrackedCharSequence(CharSequence value, Origin origin) {
            super(value, origin);
        }

        @Override
        public int length() {
            return this.getValue().length();
        }

        @Override
        public char charAt(int index) {
            return this.getValue().charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return this.getValue().subSequence(start, end);
        }

        @Override
        public CharSequence getValue() {
            return (CharSequence)super.getValue();
        }
    }

}

