/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.MapUtils
 */
package cn.springcloud.gray.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

public class GrayTrackInfo {
    public static final String GRAY_TRACK_SEPARATE = "__";
    public static final String GRAY_TRACK_PREFIX = "_g_t_";
    public static final String ATTRIBUTE_TRACE_IP = "track_ip";
    public static final String GRAY_TRACK_TRACE_IP = "_g_t_track_ip";
    public static final String GRAY_TRACK_ATTRIBUTE_PREFIX = "_g_t_attr";
    private Map<String, String> attributes = new HashMap<String, String>(32);

    public String getAttribute(String name) {
        return this.attributes.get(name.toLowerCase());
    }

    public void setAttribute(String name, String value) {
        this.attributes.put(name.toLowerCase(), value);
    }

    public Map<String, String> getAttributes() {
        return MapUtils.unmodifiableMap(this.attributes);
    }

    public static String generateGrayTrackName(String ... keys) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < keys.length; ++i) {
            str.append(keys[i]);
            if (Objects.equals(i + 1, keys.length)) continue;
            str.append(GRAY_TRACK_SEPARATE);
        }
        return str.toString();
    }

    public void setTraceIp(String ip) {
        this.setAttribute(ATTRIBUTE_TRACE_IP, ip);
    }

    public String getTraceIp() {
        return StringUtils.defaultString(this.getAttribute(ATTRIBUTE_TRACE_IP));
    }
}

