/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package cn.springcloud.gray.utils;

import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebUtils {
    private static final Logger log = LoggerFactory.getLogger(WebUtils.class);

    public static Map<String, List<String>> getQueryParams(String urlQuery) {
        HashMap<String, List<String>> qp = new HashMap<String, List<String>>();
        if (urlQuery == null) {
            return qp;
        }
        StringTokenizer st = new StringTokenizer(urlQuery, "&");
        while (st.hasMoreTokens()) {
            String name;
            String value;
            List<String> valueList;
            String s = st.nextToken();
            int i = s.indexOf("=");
            if (i > 0 && s.length() >= i + 1) {
                name = s.substring(0, i);
                value = s.substring(i + 1);
                try {
                    name = URLDecoder.decode(name, "UTF-8");
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    value = URLDecoder.decode(value, "UTF-8");
                }
                catch (Exception exception) {
                    // empty catch block
                }
                valueList = (List)qp.get(name);
                if (valueList == null) {
                    valueList = new LinkedList();
                    qp.put(name, valueList);
                }
                valueList.add(value);
                continue;
            }
            if (i != -1) continue;
            name = s;
            value = "";
            try {
                name = URLDecoder.decode(name, "UTF-8");
            }
            catch (Exception valueList2) {
                // empty catch block
            }
            valueList = (LinkedList<String>)qp.get(name);
            if (valueList == null) {
                valueList = new LinkedList<String>();
                qp.put(name, valueList);
            }
            valueList.add(value);
        }
        return qp;
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if ((ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) && (ip = request.getRemoteAddr()).equals("127.0.0.1")) {
            InetAddress inet = null;
            try {
                inet = InetAddress.getLocalHost();
                ip = inet.getHostAddress();
            }
            catch (UnknownHostException e) {
                log.error("[IpHelper-getIpAddr] IpHelper error.", e);
            }
        }
        if (ip != null && ip.length() > 15 && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }
}

