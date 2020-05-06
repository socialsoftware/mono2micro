/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.web;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public interface HttpRequest {
    public Map<String, List<String>> getHeaders();

    public Enumeration<String> getHeaders(String var1);

    public Enumeration<String> getHeaderNames();

    public String getHeader(String var1);

    public String getRequestURI();

    public Map<String, List<String>> getParameters();

    public String[] getParameterValues(String var1);

    public String getParameter(String var1);

    public String getRemoteAddr();

    public String getMethod();
}

