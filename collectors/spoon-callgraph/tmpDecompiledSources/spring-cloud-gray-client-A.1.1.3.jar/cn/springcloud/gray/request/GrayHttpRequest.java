/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.MapUtils
 *  org.springframework.util.CollectionUtils
 *  org.springframework.util.LinkedMultiValueMap
 */
package cn.springcloud.gray.request;

import cn.springcloud.gray.request.GrayRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.commons.collections.MapUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;

public class GrayHttpRequest
extends GrayRequest {
    private Map<String, List<String>> headers = new LinkedMultiValueMap();
    private String method;
    private Map<String, List<String>> parameters = new LinkedMultiValueMap();
    private byte[] body;

    public void addHeaders(Map<String, ? extends Collection<String>> headers) {
        if (MapUtils.isEmpty(headers)) {
            return;
        }
        headers.forEach((k, v) -> this.headers.put((String)k, new ArrayList(v)));
    }

    public void addParameters(Map<String, ? extends Collection<String>> parameters) {
        if (CollectionUtils.isEmpty(parameters)) {
            return;
        }
        parameters.forEach((k, v) -> this.parameters.put((String)k, new ArrayList(v)));
    }

    public void addHeader(String name, String value) {
        List values = this.headers.computeIfAbsent(name, k -> new LinkedList());
        values.add(value);
    }

    public List<String> getHeader(String name) {
        return this.headers.get(name.toLowerCase());
    }

    public List<String> getParameter(String name) {
        return this.parameters.get(name.toLowerCase());
    }

    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    public String getMethod() {
        return this.method;
    }

    public Map<String, List<String>> getParameters() {
        return this.parameters;
    }

    public byte[] getBody() {
        return this.body;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setParameters(Map<String, List<String>> parameters) {
        this.parameters = parameters;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}

