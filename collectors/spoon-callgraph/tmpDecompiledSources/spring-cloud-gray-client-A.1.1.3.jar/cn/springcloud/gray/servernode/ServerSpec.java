/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.servernode;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ServerSpec {
    private String serviceId;
    private String instanceId;
    private URI uri;
    private Map<String, Object> metadatas = new HashMap<String, Object>();

    public void setMetadata(String name, Object value) {
        this.metadatas.put(name, value);
    }

    public Object getMetadata(String name) {
        return this.metadatas.get(name);
    }

    public static ServerSpecBuilder builder() {
        return new ServerSpecBuilder();
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public URI getUri() {
        return this.uri;
    }

    public Map<String, Object> getMetadatas() {
        return this.metadatas;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public void setMetadatas(Map<String, Object> metadatas) {
        this.metadatas = metadatas;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ServerSpec)) {
            return false;
        }
        ServerSpec other = (ServerSpec)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$serviceId = this.getServiceId();
        String other$serviceId = other.getServiceId();
        if (this$serviceId == null ? other$serviceId != null : !this$serviceId.equals(other$serviceId)) {
            return false;
        }
        String this$instanceId = this.getInstanceId();
        String other$instanceId = other.getInstanceId();
        if (this$instanceId == null ? other$instanceId != null : !this$instanceId.equals(other$instanceId)) {
            return false;
        }
        URI this$uri = this.getUri();
        URI other$uri = other.getUri();
        if (this$uri == null ? other$uri != null : !((Object)this$uri).equals(other$uri)) {
            return false;
        }
        Map<String, Object> this$metadatas = this.getMetadatas();
        Map<String, Object> other$metadatas = other.getMetadatas();
        return !(this$metadatas == null ? other$metadatas != null : !((Object)this$metadatas).equals(other$metadatas));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ServerSpec;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $serviceId = this.getServiceId();
        result = result * 59 + ($serviceId == null ? 43 : $serviceId.hashCode());
        String $instanceId = this.getInstanceId();
        result = result * 59 + ($instanceId == null ? 43 : $instanceId.hashCode());
        URI $uri = this.getUri();
        result = result * 59 + ($uri == null ? 43 : ((Object)$uri).hashCode());
        Map<String, Object> $metadatas = this.getMetadatas();
        result = result * 59 + ($metadatas == null ? 43 : ((Object)$metadatas).hashCode());
        return result;
    }

    public String toString() {
        return "ServerSpec(serviceId=" + this.getServiceId() + ", instanceId=" + this.getInstanceId() + ", uri=" + this.getUri() + ", metadatas=" + this.getMetadatas() + ")";
    }

    public ServerSpec(String serviceId, String instanceId, URI uri, Map<String, Object> metadatas) {
        this.serviceId = serviceId;
        this.instanceId = instanceId;
        this.uri = uri;
        this.metadatas = metadatas;
    }

    public ServerSpec() {
    }

    public static class ServerSpecBuilder {
        private String serviceId;
        private String instanceId;
        private URI uri;
        private Map<String, Object> metadatas;

        ServerSpecBuilder() {
        }

        public ServerSpecBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public ServerSpecBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public ServerSpecBuilder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public ServerSpecBuilder metadatas(Map<String, Object> metadatas) {
            this.metadatas = metadatas;
            return this;
        }

        public ServerSpec build() {
            return new ServerSpec(this.serviceId, this.instanceId, this.uri, this.metadatas);
        }

        public String toString() {
            return "ServerSpec.ServerSpecBuilder(serviceId=" + this.serviceId + ", instanceId=" + this.instanceId + ", uri=" + this.uri + ", metadatas=" + this.metadatas + ")";
        }
    }

}

