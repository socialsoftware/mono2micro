/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.web.tracker;

import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.TrackArgs;
import cn.springcloud.gray.web.HttpRequest;
import cn.springcloud.gray.web.tracker.HttpGrayInfoTracker;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpIPGrayInfoTracker
implements HttpGrayInfoTracker {
    private static final Logger log = LoggerFactory.getLogger(HttpIPGrayInfoTracker.class);

    public void call(GrayHttpTrackInfo trackInfo, HttpRequest request) {
        String ip = this.getIpAddr(request);
        trackInfo.setTraceIp(ip);
        log.debug("\u8bb0\u5f55\u4e0bip:{}", (Object)trackInfo.getTraceIp());
    }

    @Override
    public void call(TrackArgs<GrayHttpTrackInfo, HttpRequest> args) {
        this.call(args.getTrackInfo(), args.getRequest());
    }

    public String getIpAddr(HttpRequest request) {
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

