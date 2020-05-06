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
import cn.springcloud.gray.request.GrayHttpRequest;
import cn.springcloud.gray.request.GrayRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpParameterGrayDecisionFactory
extends CompareGrayDecisionFactory<Config> {
    private static final Logger log = LoggerFactory.getLogger(HttpParameterGrayDecisionFactory.class);

    public HttpParameterGrayDecisionFactory() {
        super(Config.class);
    }

    @Override
    public GrayDecision apply(Config configBean) {
        return args -> {
            GrayHttpRequest grayRequest = (GrayHttpRequest)args.getGrayRequest();
            PredicateComparator<Collection<String>> predicateComparator = Comparators.getCollectionStringComparator(configBean.getCompareMode());
            if (predicateComparator == null) {
                log.warn("\u6ca1\u6709\u627e\u5230\u76f8\u5e94\u4e0ecompareMode'{}'\u5bf9\u5e94\u7684PredicateComparator", (Object)configBean.getCompareMode());
                return false;
            }
            return predicateComparator.test(grayRequest.getParameter(configBean.getName()), configBean.getValues());
        };
    }

    public static class Config
    extends CompareGrayDecisionFactory.CompareConfig {
        private String name;
        private List<String> values;

        public void setValues(List<String> values) {
            this.values = values;
            Collections.sort(values);
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public List<String> getValues() {
            return this.values;
        }
    }

}

