/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.DecisionDefinition
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.PolicyDefinition
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.collections.ListUtils
 *  org.springframework.core.OrderComparator
 */
package cn.springcloud.gray;

import cn.springcloud.gray.RequestInterceptor;
import cn.springcloud.gray.UpdateableGrayManager;
import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.decision.GrayDecisionFactoryKeeper;
import cn.springcloud.gray.decision.MultiGrayDecision;
import cn.springcloud.gray.model.DecisionDefinition;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.PolicyDefinition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

public abstract class AbstractGrayManager
implements UpdateableGrayManager {
    private static final Logger log = LoggerFactory.getLogger(AbstractGrayManager.class);
    private GrayDecisionFactoryKeeper grayDecisionFactoryKeeper;
    private Map<String, List<RequestInterceptor>> requestInterceptors = new HashMap<String, List<RequestInterceptor>>();
    private List<RequestInterceptor> communalRequestInterceptors = ListUtils.EMPTY_LIST;

    public AbstractGrayManager(GrayDecisionFactoryKeeper grayDecisionFactoryKeeper) {
        this.grayDecisionFactoryKeeper = grayDecisionFactoryKeeper;
    }

    @Override
    public List<RequestInterceptor> getRequeestInterceptors(String interceptroType) {
        List<RequestInterceptor> list = this.requestInterceptors.get(interceptroType);
        if (list == null) {
            return this.communalRequestInterceptors;
        }
        return list;
    }

    @Override
    public List<GrayDecision> getGrayDecision(GrayInstance instance) {
        List policyDefinitions = instance.getPolicyDefinitions();
        ArrayList<GrayDecision> grayDecisions = new ArrayList<GrayDecision>(policyDefinitions.size());
        for (PolicyDefinition policyDefinition : policyDefinitions) {
            GrayDecision decision;
            if (CollectionUtils.isEmpty((Collection)policyDefinition.getList()) || (decision = this.createGrayDecision(policyDefinition)) == null) continue;
            grayDecisions.add(decision);
        }
        return grayDecisions;
    }

    @Override
    public List<GrayDecision> getGrayDecision(String serviceId, String instanceId) {
        return this.getGrayDecision(this.getGrayInstance(serviceId, instanceId));
    }

    private GrayDecision createGrayDecision(PolicyDefinition policyDefinition) {
        try {
            MultiGrayDecision decision = new MultiGrayDecision(GrayDecision.allow());
            for (DecisionDefinition decisionDefinition : policyDefinition.getList()) {
                decision = decision.and(this.grayDecisionFactoryKeeper.getGrayDecision(decisionDefinition));
            }
            return decision;
        }
        catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void setRequestInterceptors(Collection<RequestInterceptor> requestInterceptors) {
        HashMap<String, List<RequestInterceptor>> requestInterceptorMap = new HashMap<String, List<RequestInterceptor>>();
        ArrayList<RequestInterceptor> all = new ArrayList<RequestInterceptor>();
        if (CollectionUtils.isNotEmpty(requestInterceptors)) {
            for (RequestInterceptor interceptor : requestInterceptors) {
                if (StringUtils.equals(interceptor.interceptroType(), "all")) {
                    all.add(interceptor);
                    continue;
                }
                ArrayList<RequestInterceptor> interceptors = (ArrayList<RequestInterceptor>)requestInterceptorMap.get(interceptor.interceptroType());
                if (interceptors == null) {
                    interceptors = new ArrayList<RequestInterceptor>();
                    requestInterceptorMap.put(interceptor.interceptroType(), interceptors);
                }
                interceptors.add(interceptor);
            }
            this.putTypeAllTo(requestInterceptorMap, all);
            this.communalRequestInterceptors = all;
        }
        this.communalRequestInterceptors = all;
        this.requestInterceptors = requestInterceptorMap;
    }

    private void putTypeAllTo(Map<String, List<RequestInterceptor>> requestInterceptorMap, List<RequestInterceptor> all) {
        if (all.isEmpty()) {
            return;
        }
        requestInterceptorMap.values().forEach(list -> {
            list.addAll(all);
            OrderComparator.sort((List)list);
        });
    }
}

