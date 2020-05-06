/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.MapUtils
 *  org.springframework.http.HttpHeaders
 *  org.springframework.util.LinkedMultiValueMap
 *  org.springframework.util.MultiValueMap
 */
package cn.springcloud.gray.request;

import cn.springcloud.gray.request.GrayTrackInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class GrayHttpTrackInfo
extends GrayTrackInfo {
    public static final String ATTRIBUTE_HTTP_METHOD = "method";
    public static final String ATTRIBUTE_HTTP_URI = "uri";
    public static final String GRAY_TRACK_HEADER_PREFIX = "_g_t_header";
    public static final String GRAY_TRACK_METHOD = "_g_t_method";
    public static final String GRAY_TRACK_PARAMETER_PREFIX = "_g_t_param";
    public static final String GRAY_TRACK_URI = "_g_t_uri";
    private HttpHeaders headers = new HttpHeaders();
    private MultiValueMap<String, String> parameters = new LinkedMultiValueMap();

    public void addHeader(String name, String value) {
        this.headers.add(name.toLowerCase(), value);
    }

    public void setHeader(String name, List<String> values) {
        this.headers.put(name.toLowerCase(), values);
    }

    public List<String> getHeader(String name) {
        return this.headers.get((Object)name.toLowerCase());
    }

    public void addParameter(String name, String value) {
        this.parameters.add((Object)name.toLowerCase(), (Object)value);
    }

    public void setParameters(String name, List<String> value) {
        this.parameters.put((Object)name.toLowerCase(), value);
    }

    public List<String> getParameter(String name) {
        return (List)this.parameters.get((Object)name.toLowerCase());
    }

    public Set<String> headerNames() {
        return this.headers.keySet();
    }

    public Set<String> parameterNames() {
        return this.parameters.keySet();
    }

    public Map<String, List<String>> getHeaders() {
        return MapUtils.unmodifiableMap((Map)this.headers);
    }

    public Map<String, List<String>> getParameters() {
        return MapUtils.unmodifiableMap(this.parameters);
    }

    public void setUri(String url) {
        this.setAttribute(ATTRIBUTE_HTTP_URI, url);
    }

    public String getUri() {
        return StringUtils.defaultString(this.getAttribute(ATTRIBUTE_HTTP_URI));
    }

    public void setMethod(String method) {
        this.setAttribute(ATTRIBUTE_HTTP_METHOD, method);
    }

    public String getMethod() {
        return StringUtils.defaultString(this.getAttribute(ATTRIBUTE_HTTP_METHOD));
    }
}

