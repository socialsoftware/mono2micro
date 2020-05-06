/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.decision.factory;

import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.decision.GrayDecisionInputArgs;
import cn.springcloud.gray.decision.compare.Comparators;
import cn.springcloud.gray.decision.compare.CompareMode;
import cn.springcloud.gray.decision.compare.PredicateComparator;
import cn.springcloud.gray.decision.factory.CompareGrayDecisionFactory;
import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackAttributeGrayDecisionFactory
extends CompareGrayDecisionFactory<Config> {
    private static final Logger log = LoggerFactory.getLogger(TrackAttributeGrayDecisionFactory.class);

    public TrackAttributeGrayDecisionFactory() {
        super(Config.class);
    }

    @Override
    public GrayDecision apply(Config configBean) {
        return args -> {
            GrayHttpTrackInfo grayTrackInfo = (GrayHttpTrackInfo)args.getGrayRequest().getGrayTrackInfo();
            if (grayTrackInfo == null) {
                log.warn("\u6ca1\u6709\u83b7\u53d6\u5230\u7070\u5ea6\u8ffd\u8e2a\u4fe1\u606f");
                return false;
            }
            PredicateComparator<String> predicateComparator = Comparators.getStringComparator(configBean.getCompareMode());
            if (predicateComparator == null) {
                log.warn("\u6ca1\u6709\u627e\u5230\u76f8\u5e94\u4e0ecompareMode'{}'\u5bf9\u5e94\u7684PredicateComparator", (Object)configBean.getCompareMode());
                return false;
            }
            return predicateComparator.test(grayTrackInfo.getAttribute(configBean.getName()), configBean.getValue());
        };
    }

    public static class Config
    extends CompareGrayDecisionFactory.CompareConfig {
        private String name;
        private String value;

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }
    }

}

