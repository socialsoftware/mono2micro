/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.request;

import cn.springcloud.gray.request.GrayTrackInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class GrayRequest {
    private String serviceId;
    private URI uri;
    private GrayTrackInfo grayTrackInfo;
    private Map<String, Object> attributes = new HashMap<String, Object>(32);

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return this.uri;
    }

    public void setGrayTrackInfo(GrayTrackInfo grayTrackInfo) {
        this.grayTrackInfo = grayTrackInfo;
    }

    public GrayTrackInfo getGrayTrackInfo() {
        return this.grayTrackInfo;
    }
}

