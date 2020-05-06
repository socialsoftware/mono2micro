/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.web.HttpRequest
 *  javax.servlet.http.HttpServletRequest
 *  org.springframework.http.HttpHeaders
 */
package cn.springcloud.gray.web;

import cn.springcloud.gray.web.HttpRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public class ServletHttpRequestWrapper
implements HttpRequest {
    private HttpServletRequest servletRequest;

    public ServletHttpRequestWrapper(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public Enumeration<String> getHeaders(String name) {
        return this.servletRequest.getHeaders(name);
    }

    public Enumeration<String> getHeaderNames() {
        return this.servletRequest.getHeaderNames();
    }

    public String getHeader(String headerName) {
        return this.servletRequest.getHeader(headerName);
    }

    public String getRequestURI() {
        return this.servletRequest.getRequestURI();
    }

    public String[] getParameterValues(String name) {
        return this.servletRequest.getParameterValues(name);
    }

    public String getParameter(String name) {
        return this.servletRequest.getParameter(name);
    }

    public String getRemoteAddr() {
        return this.servletRequest.getRemoteAddr();
    }

    public String getMethod() {
        return this.servletRequest.getMethod();
    }

    public Map<String, List<String>> getHeaders() {
        Enumeration headerNames = this.servletRequest.getHeaderNames();
        HttpHeaders httpHeaders = new HttpHeaders();
        while (headerNames.hasMoreElements()) {
            String headerName = (String)headerNames.nextElement();
            Enumeration headerValues = this.servletRequest.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                httpHeaders.add(headerName, (String)headerValues.nextElement());
            }
        }
        return httpHeaders;
    }

    public Map<String, List<String>> getParameters() {
        return this.servletRequest.getParameterMap().entrySet().stream().collect(Collectors.toMap(k -> (String)k.getKey(), v -> Arrays.asList((Object[])v.getValue())));
    }
}

