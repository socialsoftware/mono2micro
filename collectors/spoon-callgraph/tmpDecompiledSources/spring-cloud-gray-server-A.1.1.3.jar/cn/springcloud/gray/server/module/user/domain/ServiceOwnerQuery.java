/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.module.user.domain;

public class ServiceOwnerQuery {
    public static final int QUERY_ITEM_ALL = 0;
    public static final int QUERY_ITEM_BINDED = 1;
    public static final int QUERY_ITEM_UNBINDED = 2;
    private String serviceId;
    private int queryItem;

    public static ServiceOwnerQueryBuilder builder() {
        return new ServiceOwnerQueryBuilder();
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public int getQueryItem() {
        return this.queryItem;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setQueryItem(int queryItem) {
        this.queryItem = queryItem;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ServiceOwnerQuery)) {
            return false;
        }
        ServiceOwnerQuery other = (ServiceOwnerQuery)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$serviceId = this.getServiceId();
        String other$serviceId = other.getServiceId();
        if (this$serviceId == null ? other$serviceId != null : !this$serviceId.equals(other$serviceId)) {
            return false;
        }
        return this.getQueryItem() == other.getQueryItem();
    }

    protected boolean canEqual(Object other) {
        return other instanceof ServiceOwnerQuery;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $serviceId = this.getServiceId();
        result = result * 59 + ($serviceId == null ? 43 : $serviceId.hashCode());
        result = result * 59 + this.getQueryItem();
        return result;
    }

    public String toString() {
        return "ServiceOwnerQuery(serviceId=" + this.getServiceId() + ", queryItem=" + this.getQueryItem() + ")";
    }

    public ServiceOwnerQuery(String serviceId, int queryItem) {
        this.serviceId = serviceId;
        this.queryItem = queryItem;
    }

    public ServiceOwnerQuery() {
    }

    public static class ServiceOwnerQueryBuilder {
        private String serviceId;
        private int queryItem;

        ServiceOwnerQueryBuilder() {
        }

        public ServiceOwnerQueryBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public ServiceOwnerQueryBuilder queryItem(int queryItem) {
            this.queryItem = queryItem;
            return this;
        }

        public ServiceOwnerQuery build() {
            return new ServiceOwnerQuery(this.serviceId, this.queryItem);
        }

        public String toString() {
            return "ServiceOwnerQuery.ServiceOwnerQueryBuilder(serviceId=" + this.serviceId + ", queryItem=" + this.queryItem + ")";
        }
    }

}

