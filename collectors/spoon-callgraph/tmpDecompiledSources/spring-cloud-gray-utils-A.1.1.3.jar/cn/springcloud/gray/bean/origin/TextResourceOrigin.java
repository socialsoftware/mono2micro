/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.io.Resource
 *  org.springframework.util.ObjectUtils
 */
package cn.springcloud.gray.bean.origin;

import cn.springcloud.gray.bean.origin.Origin;
import org.springframework.core.io.Resource;
import org.springframework.util.ObjectUtils;

public class TextResourceOrigin
implements Origin {
    private final Resource resource;
    private final Location location;

    public TextResourceOrigin(Resource resource, Location location) {
        this.resource = resource;
        this.location = location;
    }

    public Resource getResource() {
        return this.resource;
    }

    public Location getLocation() {
        return this.location;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof TextResourceOrigin) {
            TextResourceOrigin other = (TextResourceOrigin)obj;
            boolean result = true;
            result = result && ObjectUtils.nullSafeEquals((Object)this.resource, (Object)other.resource);
            result = result && ObjectUtils.nullSafeEquals((Object)this.location, (Object)other.location);
            return result;
        }
        return super.equals(obj);
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + ObjectUtils.nullSafeHashCode((Object)this.resource);
        result = 31 * result + ObjectUtils.nullSafeHashCode((Object)this.location);
        return result;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.resource != null ? this.resource.getDescription() : "unknown resource [?]");
        if (this.location != null) {
            result.append(":").append(this.location);
        }
        return result.toString();
    }

    public static final class Location {
        private final int line;
        private final int column;

        public Location(int line, int column) {
            this.line = line;
            this.column = column;
        }

        public int getLine() {
            return this.line;
        }

        public int getColumn() {
            return this.column;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            Location other = (Location)obj;
            boolean result = true;
            result = result && this.line == other.line;
            result = result && this.column == other.column;
            return result;
        }

        public int hashCode() {
            return 31 * this.line + this.column;
        }

        public String toString() {
            return this.line + 1 + ":" + (this.column + 1);
        }
    }

}

