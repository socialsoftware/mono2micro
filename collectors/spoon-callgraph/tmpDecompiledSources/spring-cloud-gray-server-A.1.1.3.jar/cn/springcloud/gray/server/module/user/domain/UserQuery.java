/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.module.user.domain;

public class UserQuery {
    public static final int STATUS_ALL = -1;
    private String key;
    private int status;

    public static UserQueryBuilder builder() {
        return new UserQueryBuilder();
    }

    public String getKey() {
        return this.key;
    }

    public int getStatus() {
        return this.status;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UserQuery)) {
            return false;
        }
        UserQuery other = (UserQuery)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$key = this.getKey();
        String other$key = other.getKey();
        if (this$key == null ? other$key != null : !this$key.equals(other$key)) {
            return false;
        }
        return this.getStatus() == other.getStatus();
    }

    protected boolean canEqual(Object other) {
        return other instanceof UserQuery;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $key = this.getKey();
        result = result * 59 + ($key == null ? 43 : $key.hashCode());
        result = result * 59 + this.getStatus();
        return result;
    }

    public String toString() {
        return "UserQuery(key=" + this.getKey() + ", status=" + this.getStatus() + ")";
    }

    public UserQuery() {
    }

    public UserQuery(String key, int status) {
        this.key = key;
        this.status = status;
    }

    public static class UserQueryBuilder {
        private String key;
        private int status;

        UserQueryBuilder() {
        }

        public UserQueryBuilder key(String key) {
            this.key = key;
            return this;
        }

        public UserQueryBuilder status(int status) {
            this.status = status;
            return this;
        }

        public UserQuery build() {
            return new UserQuery(this.key, this.status);
        }

        public String toString() {
            return "UserQuery.UserQueryBuilder(key=" + this.key + ", status=" + this.status + ")";
        }
    }

}

