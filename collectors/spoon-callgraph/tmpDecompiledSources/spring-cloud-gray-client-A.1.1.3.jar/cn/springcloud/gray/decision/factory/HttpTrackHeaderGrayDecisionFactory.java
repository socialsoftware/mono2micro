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
import cn.springcloud.gray.decision.factory.HttpHeaderGrayDecisionFactory;
import cn.springcloud.gray.request.GrayHttpTrackInfo;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.GrayTrackInfo;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpTrackHeaderGrayDecisionFactory
extends CompareGrayDecisionFactory<HttpHeaderGrayDecisionFactory.Config> {
    private static final Logger log = LoggerFactory.getLogger(HttpTrackHeaderGrayDecisionFactory.class);

    public HttpTrackHeaderGrayDecisionFactory() {
        super(HttpHeaderGrayDecisionFactory.Config.class);
    }

    @Override
    public GrayDecision apply(HttpHeaderGrayDecisionFactory.Config configBean) {
        return args -> {
            GrayHttpTrackInfo grayTrackInfo = (GrayHttpTrackInfo)args.getGrayRequest().getGrayTrackInfo();
            if (grayTrackInfo == null) {
                log.warn("\u6ca1\u6709\u83b7\u53d6\u5230\u7070\u5ea6\u8ffd\u8e2a\u4fe1\u606f");
                return false;
            }
            PredicateComparator<Collection<String>> predicateComparator = Comparators.getCollectionStringComparator(configBean.getCompareMode());
            if (predicateComparator == null) {
                log.warn("\u6ca1\u6709\u627e\u5230\u76f8\u5e94\u4e0ecompareMode'{}'\u5bf9\u5e94\u7684PredicateComparator", (Object)configBean.getCompareMode());
                return false;
            }
            return predicateComparator.test(grayTrackInfo.getHeader(configBean.getHeader()), configBean.getValues());
        };
    }
}

