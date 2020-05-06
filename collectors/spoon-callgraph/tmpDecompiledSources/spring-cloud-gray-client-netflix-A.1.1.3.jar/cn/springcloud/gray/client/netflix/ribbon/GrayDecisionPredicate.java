/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.GrayManager
 *  cn.springcloud.gray.decision.GrayDecision
 *  cn.springcloud.gray.decision.GrayDecisionInputArgs
 *  cn.springcloud.gray.decision.GrayDecisionInputArgs$GrayDecisionInputArgsBuilder
 *  cn.springcloud.gray.request.GrayRequest
 *  cn.springcloud.gray.request.RequestLocalStorage
 *  cn.springcloud.gray.servernode.ServerExplainer
 *  cn.springcloud.gray.servernode.ServerSpec
 *  com.netflix.loadbalancer.AbstractServerPredicate
 *  com.netflix.loadbalancer.IRule
 *  com.netflix.loadbalancer.PredicateKey
 *  com.netflix.loadbalancer.Server
 *  com.netflix.loadbalancer.Server$MetaInfo
 */
package cn.springcloud.gray.client.netflix.ribbon;

import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.client.netflix.ribbon.GrayLoadBalanceRule;
import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.decision.GrayDecisionInputArgs;
import cn.springcloud.gray.request.GrayRequest;
import cn.springcloud.gray.request.RequestLocalStorage;
import cn.springcloud.gray.servernode.ServerExplainer;
import cn.springcloud.gray.servernode.ServerSpec;
import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PredicateKey;
import com.netflix.loadbalancer.Server;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrayDecisionPredicate
extends AbstractServerPredicate {
    private static final Logger log = LoggerFactory.getLogger(GrayDecisionPredicate.class);

    public GrayDecisionPredicate(GrayLoadBalanceRule rule) {
        super((IRule)rule);
    }

    public boolean apply(PredicateKey input) {
        GrayLoadBalanceRule grayRule = this.getIRule();
        GrayRequest grayRequest = grayRule.getRequestLocalStorage().getGrayRequest();
        Server server = input.getServer();
        String serviceId = grayRequest.getServiceId();
        String instanceId = server.getMetaInfo().getInstanceId();
        try {
            ServerSpec serverSpec = grayRule.getServerExplainer().apply((Object)server);
            GrayDecisionInputArgs decisionInputArgs = GrayDecisionInputArgs.builder().grayRequest(grayRequest).server(serverSpec).build();
            List grayDecisions = grayRule.getGrayManager().getGrayDecision(serviceId, instanceId);
            for (GrayDecision grayDecision : grayDecisions) {
                if (!grayDecision.test(decisionInputArgs)) continue;
                return true;
            }
        }
        catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    protected GrayLoadBalanceRule getIRule() {
        return (GrayLoadBalanceRule)this.rule;
    }
}

