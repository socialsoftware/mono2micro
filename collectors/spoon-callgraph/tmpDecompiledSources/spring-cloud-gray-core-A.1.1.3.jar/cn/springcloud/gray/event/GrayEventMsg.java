/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.event;

import cn.springcloud.gray.event.EventType;
import cn.springcloud.gray.event.SourceType;
import java.io.Serializable;

public class GrayEventMsg
implements Serializable {
    private static final long serialVersionUID = -8114806214567175543L;
    private String serviceId;
    private String instanceId;
    private EventType eventType;
    private SourceType sourceType;
    private Object source;

    public static GrayEventMsgBuilder builder() {
        return new GrayEventMsgBuilder();
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public SourceType getSourceType() {
        return this.sourceType;
    }

    public Object getSource() {
        return this.source;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GrayEventMsg)) {
            return false;
        }
        GrayEventMsg other = (GrayEventMsg)o;
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
        EventType this$eventType = this.getEventType();
        EventType other$eventType = other.getEventType();
        if (this$eventType == null ? other$eventType != null : !this$eventType.equals(other$eventType)) {
            return false;
        }
        SourceType this$sourceType = this.getSourceType();
        SourceType other$sourceType = other.getSourceType();
        if (this$sourceType == null ? other$sourceType != null : !((Object)((Object)this$sourceType)).equals((Object)other$sourceType)) {
            return false;
        }
        Object this$source = this.getSource();
        Object other$source = other.getSource();
        return !(this$source == null ? other$source != null : !this$source.equals(other$source));
    }

    protected boolean canEqual(Object other) {
        return other instanceof GrayEventMsg;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $serviceId = this.getServiceId();
        result = result * 59 + ($serviceId == null ? 43 : $serviceId.hashCode());
        String $instanceId = this.getInstanceId();
        result = result * 59 + ($instanceId == null ? 43 : $instanceId.hashCode());
        EventType $eventType = this.getEventType();
        result = result * 59 + ($eventType == null ? 43 : $eventType.hashCode());
        SourceType $sourceType = this.getSourceType();
        result = result * 59 + ($sourceType == null ? 43 : ((Object)((Object)$sourceType)).hashCode());
        Object $source = this.getSource();
        result = result * 59 + ($source == null ? 43 : $source.hashCode());
        return result;
    }

    public GrayEventMsg() {
    }

    public GrayEventMsg(String serviceId, String instanceId, EventType eventType, SourceType sourceType, Object source) {
        this.serviceId = serviceId;
        this.instanceId = instanceId;
        this.eventType = eventType;
        this.sourceType = sourceType;
        this.source = source;
    }

    public String toString() {
        return "GrayEventMsg(serviceId=" + this.getServiceId() + ", instanceId=" + this.getInstanceId() + ", eventType=" + this.getEventType() + ", sourceType=" + (Object)((Object)this.getSourceType()) + ", source=" + this.getSource() + ")";
    }

    public static class GrayEventMsgBuilder {
        private String serviceId;
        private String instanceId;
        private EventType eventType;
        private SourceType sourceType;
        private Object source;

        GrayEventMsgBuilder() {
        }

        public GrayEventMsgBuilder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public GrayEventMsgBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public GrayEventMsgBuilder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public GrayEventMsgBuilder sourceType(SourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public GrayEventMsgBuilder source(Object source) {
            this.source = source;
            return this;
        }

        public GrayEventMsg build() {
            return new GrayEventMsg(this.serviceId, this.instanceId, this.eventType, this.sourceType, this.source);
        }

        public String toString() {
            return "GrayEventMsg.GrayEventMsgBuilder(serviceId=" + this.serviceId + ", instanceId=" + this.instanceId + ", eventType=" + this.eventType + ", sourceType=" + (Object)((Object)this.sourceType) + ", source=" + this.source + ")";
        }
    }

}

