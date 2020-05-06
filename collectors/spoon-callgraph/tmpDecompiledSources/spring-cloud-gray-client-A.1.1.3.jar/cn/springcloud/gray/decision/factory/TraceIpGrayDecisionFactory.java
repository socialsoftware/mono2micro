/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.StringUtils
 */
package cn.springcloud.gray.decision.factory;

import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.decision.GrayDecisionInputArgs;
import cn.springcloud.gray.decision.factory.AbstractGrayDecisionFactory;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class TraceIpGrayDecisionFactory
extends AbstractGrayDecisionFactory<Config> {
    private static final Logger log = LoggerFactory.getLogger(TraceIpGrayDecisionFactory.class);

    public TraceIpGrayDecisionFactory() {
        super(Config.class);
    }

    @Override
    public GrayDecision apply(Config configBean) {
        return args -> {
            Pattern pat = Pattern.compile(configBean.getIp());
            GrayTrackInfo trackInfo = args.getGrayRequest().getGrayTrackInfo();
            if (trackInfo == null) {
                log.warn("\u6ca1\u6709\u83b7\u53d6\u5230\u7070\u5ea6\u8ffd\u8e2a\u4fe1\u606f");
                return false;
            }
            String traceIp = trackInfo.getTraceIp();
            if (StringUtils.isEmpty((Object)traceIp)) {
                log.warn("\u7070\u5ea6\u8ffd\u8e2a\u8bb0\u5f55\u7684ip\u4e3a\u7a7a");
                return false;
            }
            Matcher mat = pat.matcher(traceIp);
            return mat.find();
        };
    }

    public static class Config {
        private String ip;

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getIp() {
            return this.ip;
        }
    }

}

