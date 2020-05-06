/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.DecisionDefinition
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.PolicyDefinition
 */
package cn.springcloud.gray;

import cn.springcloud.gray.CommunicableGrayManager;
import cn.springcloud.gray.GrayManager;
import cn.springcloud.gray.RequestInterceptor;
import cn.springcloud.gray.UpdateableGrayManager;
import cn.springcloud.gray.communication.InformationClient;
import cn.springcloud.gray.decision.GrayDecision;
import cn.springcloud.gray.model.DecisionDefinition;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayService;
import cn.springcloud.gray.model.PolicyDefinition;
import java.util.Collection;
import java.util.List;

public abstract class GrayManagerDelegater
implements UpdateableGrayManager,
CommunicableGrayManager {
    protected GrayManager delegate;

    public GrayManagerDelegater(GrayManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasGray(String serviceId) {
        return this.delegate.hasGray(serviceId);
    }

    @Override
    public Collection<GrayService> allGrayServices() {
        return this.delegate.allGrayServices();
    }

    @Override
    public GrayService getGrayService(String serviceId) {
        return this.delegate.getGrayService(serviceId);
    }

    @Override
    public GrayInstance getGrayInstance(String serviceId, String instanceId) {
        return this.delegate.getGrayInstance(serviceId, instanceId);
    }

    @Override
    public List<GrayDecision> getGrayDecision(GrayInstance instance) {
        return this.delegate.getGrayDecision(instance);
    }

    @Override
    public List<GrayDecision> getGrayDecision(String serviceId, String instanceId) {
        return this.delegate.getGrayDecision(serviceId, instanceId);
    }

    @Override
    public void updateGrayInstance(GrayInstance instance) {
        this.delegate.updateGrayInstance(instance);
    }

    @Override
    public void closeGray(GrayInstance instance) {
        this.delegate.closeGray(instance);
    }

    @Override
    public void closeGray(String serviceId, String instanceId) {
        this.delegate.closeGray(serviceId, instanceId);
    }

    @Override
    public List<RequestInterceptor> getRequeestInterceptors(String interceptroType) {
        return this.delegate.getRequeestInterceptors(interceptroType);
    }

    @Override
    public void setup() {
        this.delegate.setup();
    }

    @Override
    public void shutdown() {
        this.delegate.shutdown();
    }

    @Override
    public void setGrayServices(Object grayServices) {
        if (this.delegate instanceof UpdateableGrayManager) {
            ((UpdateableGrayManager)this.delegate).setGrayServices(grayServices);
        }
    }

    @Override
    public void setRequestInterceptors(Collection<RequestInterceptor> requestInterceptors) {
        if (this.delegate instanceof UpdateableGrayManager) {
            ((UpdateableGrayManager)this.delegate).setRequestInterceptors(requestInterceptors);
        }
    }

    @Override
    public InformationClient getGrayInformationClient() {
        if (this.delegate instanceof CommunicableGrayManager) {
            return ((CommunicableGrayManager)this.delegate).getGrayInformationClient();
        }
        throw new UnsupportedOperationException("delegate\u4e0d\u662fCommunicableGrayManager\u7684\u5b9e\u73b0\u7c7b");
    }

    @Override
    public void removeGrayService(String serviceId) {
        if (this.delegate instanceof UpdateableGrayManager) {
            ((UpdateableGrayManager)this.delegate).removeGrayService(serviceId);
        }
    }

    @Override
    public void removePolicyDefinition(String serviceId, String instanceId, String policyId) {
        if (this.delegate instanceof UpdateableGrayManager) {
            ((UpdateableGrayManager)this.delegate).removePolicyDefinition(serviceId, instanceId, policyId);
        }
    }

    @Override
    public void updatePolicyDefinition(String serviceId, String instanceId, PolicyDefinition policyDefinition) {
        if (this.delegate instanceof UpdateableGrayManager) {
            ((UpdateableGrayManager)this.delegate).updatePolicyDefinition(serviceId, instanceId, policyDefinition);
        }
    }

    @Override
    public void removeDecisionDefinition(String serviceId, String instanceId, String policyId, String decisionId) {
        if (this.delegate instanceof UpdateableGrayManager) {
            ((UpdateableGrayManager)this.delegate).removeDecisionDefinition(serviceId, instanceId, policyId, decisionId);
        }
    }

    @Override
    public void updateDecisionDefinition(String serviceId, String instanceId, String policyId, DecisionDefinition decisionDefinition) {
        if (this.delegate instanceof UpdateableGrayManager) {
            ((UpdateableGrayManager)this.delegate).updateDecisionDefinition(serviceId, instanceId, policyId, decisionDefinition);
        }
    }
}

