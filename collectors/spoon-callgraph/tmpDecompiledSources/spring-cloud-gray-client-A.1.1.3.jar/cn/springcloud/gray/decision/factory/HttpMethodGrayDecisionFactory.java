/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.http.HttpMethod
 */
package cn.springcloud.gray.decision.factory;

import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.decision.GrayDecisionInputArgs;
import cn.springcloud.gray.decision.compare.Comparators;
import cn.springcloud.gray.decision.compare.CompareMode;
import cn.springcloud.gray.decision.compare.PredicateComparator;
import cn.springcloud.gray.decision.factory.AbstractGrayDecisionFactory;
import cn.springcloud.gray.decision.factory.CompareGrayDecisionFactory;
import cn.springcloud.gray.request.GrayHttpRequest;
import cn.springcloud.gray.request.GrayRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class HttpMethodGrayDecisionFactory
extends AbstractGrayDecisionFactory<Config> {
    private static final Logger log = LoggerFactory.getLogger(HttpMethodGrayDecisionFactory.class);

    public HttpMethodGrayDecisionFactory() {
        super(Config.class);
    }

    @Override
    public GrayDecision apply(Config configBean) {
        return args -> {
            GrayHttpRequest grayRequest = (GrayHttpRequest)args.getGrayRequest();
            PredicateComparator<String> predicateComparator = Comparators.getStringComparator(configBean.getCompareMode());
            if (predicateComparator == null) {
                log.warn("\u6ca1\u6709\u627e\u5230\u76f8\u5e94\u4e0ecompareMode'{}'\u5bf9\u5e94\u7684PredicateComparator", (Object)configBean.getCompareMode());
                return false;
            }
            return predicateComparator.test(grayRequest.getMethod(), configBean.getMethod().name());
        };
    }

    public static class Config
    extends CompareGrayDecisionFactory.CompareConfig {
        private HttpMethod method;

        public void setMethod(HttpMethod method) {
            this.method = method;
        }

        public HttpMethod getMethod() {
            return this.method;
        }
    }

}

