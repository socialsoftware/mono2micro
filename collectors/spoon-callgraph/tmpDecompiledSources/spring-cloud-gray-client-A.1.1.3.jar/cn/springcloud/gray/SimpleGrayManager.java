/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.DecisionDefinition
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.PolicyDefinition
 */
package cn.springcloud.gray;

import cn.springcloud.gray.AbstractGrayManager;
import cn.springcloud.gray.GrayClientHolder;
import cn.springcloud.gray.decision.GrayDecisionFactoryKeeper;
import cn.springcloud.gray.local.InstanceLocalInfo;
import cn.springcloud.gray.model.DecisionDefinition;
import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayService;
import cn.springcloud.gray.model.PolicyDefinition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleGrayManager
extends AbstractGrayManager {
    private static final Logger log = LoggerFactory.getLogger(SimpleGrayManager.class);
    protected Map<String, GrayService> grayServices = new ConcurrentHashMap<String, GrayService>();
    protected Lock lock = new ReentrantLock();

    public SimpleGrayManager(GrayDecisionFactoryKeeper grayDecisionFactoryKeeper) {
        super(grayDecisionFactoryKeeper);
    }

    @Override
    public boolean hasGray(String serviceId) {
        GrayService grayService = this.grayServices.get(serviceId);
        return grayService != null && !grayService.getGrayInstances().isEmpty();
    }

    @Override
    public Collection<GrayService> allGrayServices() {
        return Collections.unmodifiableCollection(this.grayServices.values());
    }

    @Override
    public GrayService getGrayService(String serviceId) {
        return this.grayServices.get(serviceId);
    }

    @Override
    public void removeGrayService(String serviceId) {
        this.lock.lock();
        try {
            this.grayServices.remove(serviceId);
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removePolicyDefinition(String serviceId, String instanceId, String policyId) {
        this.lock.lock();
        try {
            GrayInstance grayInstance = this.getGrayInstance(serviceId, instanceId);
            if (grayInstance == null) {
                log.error("removePolicyDefinition('{}', '{}', '{}') is not find gray instance", serviceId, instanceId, policyId);
                return;
            }
            PolicyDefinition record = this.getPolicyDefinition(grayInstance.getPolicyDefinitions(), policyId);
            if (record != null) {
                grayInstance.getPolicyDefinitions().remove((Object)record);
            }
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void updatePolicyDefinition(String serviceId, String instanceId, PolicyDefinition policyDefinition) {
        this.lock.lock();
        try {
            GrayInstance grayInstance = this.getGrayInstance(serviceId, instanceId);
            if (grayInstance == null) {
                log.error("updatePolicyDefinition('{}', '{}', '{}') is not find gray instance", new Object[]{serviceId, instanceId, policyDefinition});
                return;
            }
            PolicyDefinition record = this.getPolicyDefinition(grayInstance.getPolicyDefinitions(), policyDefinition.getPolicyId());
            if (record == null) {
                record = new PolicyDefinition();
                record.setPolicyId(policyDefinition.getPolicyId());
                grayInstance.getPolicyDefinitions().add(policyDefinition);
            }
            record.setList(new ArrayList(policyDefinition.getList()));
            record.setAlias(policyDefinition.getAlias());
        }
        finally {
            this.lock.unlock();
        }
    }

    private PolicyDefinition getPolicyDefinition(String serviceId, String instanceId, String policyId) {
        GrayInstance grayInstance = this.getGrayInstance(serviceId, instanceId);
        if (grayInstance == null) {
            log.error("getPolicyDefinition('{}', '{}', '{}') is not find gray instance", serviceId, instanceId, policyId);
            return null;
        }
        return this.getPolicyDefinition(grayInstance.getPolicyDefinitions(), policyId);
    }

    @Override
    public void removeDecisionDefinition(String serviceId, String instanceId, String policyId, String decesionId) {
        PolicyDefinition policyDefinition = this.getPolicyDefinition(serviceId, instanceId, policyId);
        if (policyDefinition == null) {
            return;
        }
        DecisionDefinition decisionDefinition = this.getDecisionDefinition(policyDefinition.getList(), decesionId);
        if (decisionDefinition != null) {
            policyDefinition.getList().remove((Object)decisionDefinition);
        }
    }

    @Override
    public void updateDecisionDefinition(String serviceId, String instanceId, String policyId, DecisionDefinition decisionDefinition) {
        PolicyDefinition policyDefinition = this.getPolicyDefinition(serviceId, instanceId, policyId);
        if (policyDefinition == null) {
            return;
        }
        DecisionDefinition definition = this.getDecisionDefinition(policyDefinition.getList(), decisionDefinition.getId());
        if (definition != null) {
            definition.setName(decisionDefinition.getName());
            definition.setInfos(decisionDefinition.getInfos());
        } else {
            policyDefinition.getList().add(decisionDefinition);
        }
    }

    @Override
    public GrayInstance getGrayInstance(String serviceId, String instanceId) {
        GrayService service = this.getGrayService(serviceId);
        return service != null ? service.getGrayInstance(instanceId) : null;
    }

    @Override
    public void updateGrayInstance(GrayInstance instance) {
        if (instance == null) {
            return;
        }
        this.lock.lock();
        try {
            if (!instance.isGray()) {
                this.closeGray(instance);
                return;
            }
            this.updateGrayInstance(this.grayServices, instance);
        }
        finally {
            this.lock.unlock();
        }
    }

    protected void updateGrayInstance(Map<String, GrayService> grayServices, GrayInstance instance) {
        InstanceLocalInfo instanceLocalInfo = GrayClientHolder.getInstanceLocalInfo();
        if (instanceLocalInfo != null && StringUtils.equals(instanceLocalInfo.getServiceId(), instance.getServiceId())) {
            return;
        }
        GrayService service = grayServices.get(instance.getServiceId());
        if (service == null && service == null) {
            service = new GrayService();
            service.setServiceId(instance.getServiceId());
            grayServices.put(service.getServiceId(), service);
        }
        log.debug("\u6dfb\u52a0\u7070\u5ea6\u5b9e\u4f8b, serviceId:{}, instanceId:{}", (Object)instance.getServiceId(), (Object)instance.getInstanceId());
        service.setGrayInstance(instance);
    }

    @Override
    public void closeGray(GrayInstance instance) {
        this.closeGray(instance.getServiceId(), instance.getInstanceId());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void closeGray(String serviceId, String instanceId) {
        GrayService service = this.getGrayService(serviceId);
        if (service == null) {
            log.debug("\u6ca1\u6709\u627e\u5230\u7070\u5ea6\u670d\u52a1:{}, \u6240\u4ee5\u65e0\u9700\u5220\u9664\u5b9e\u4f8b:{} \u7684\u7070\u5ea6\u72b6\u6001", (Object)serviceId, (Object)instanceId);
            return;
        }
        log.debug("\u5173\u95ed\u5b9e\u4f8b\u7684\u5728\u7070\u5ea6\u72b6\u6001, serviceId:{}, instanceId:{}", (Object)serviceId, (Object)instanceId);
        this.lock.lock();
        try {
            service.removeGrayInstance(instanceId);
        }
        finally {
            this.lock.unlock();
        }
    }

    @Override
    public void setup() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void setGrayServices(Object grayServices) {
        if (!(grayServices instanceof Map)) {
            throw new UnsupportedOperationException("setGrayServices(grayServices) \u65e0\u6cd5\u652f\u6301\u7684\u53c2\u6570\u7c7b\u578b");
        }
        this.grayServices = (Map)grayServices;
    }

    private PolicyDefinition getPolicyDefinition(List<PolicyDefinition> policyDefinitions, String policyId) {
        for (PolicyDefinition policyDefinition : policyDefinitions) {
            if (!StringUtils.equals(policyDefinition.getPolicyId(), policyId)) continue;
            return policyDefinition;
        }
        return null;
    }

    private DecisionDefinition getDecisionDefinition(List<DecisionDefinition> decisionDefinitions, String decisionId) {
        for (DecisionDefinition decisionDefinition : decisionDefinitions) {
            if (!StringUtils.equals(decisionDefinition.getId(), decisionId)) continue;
            return decisionDefinition;
        }
        return null;
    }
}

