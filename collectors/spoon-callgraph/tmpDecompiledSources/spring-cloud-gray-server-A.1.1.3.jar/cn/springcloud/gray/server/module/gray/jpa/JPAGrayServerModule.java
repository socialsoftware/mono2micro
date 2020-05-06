/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.event.EventType
 *  cn.springcloud.gray.event.GrayEventMsg
 *  cn.springcloud.gray.event.GrayEventMsg$GrayEventMsgBuilder
 *  cn.springcloud.gray.event.GraySourceEventPublisher
 *  cn.springcloud.gray.event.SourceType
 *  cn.springcloud.gray.model.GrayStatus
 *  cn.springcloud.gray.model.InstanceInfo
 *  cn.springcloud.gray.model.InstanceStatus
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.transaction.annotation.Transactional
 */
package cn.springcloud.gray.server.module.gray.jpa;

import cn.springcloud.gray.event.EventType;
import cn.springcloud.gray.event.GrayEventMsg;
import cn.springcloud.gray.event.GraySourceEventPublisher;
import cn.springcloud.gray.event.SourceType;
import cn.springcloud.gray.model.GrayStatus;
import cn.springcloud.gray.model.InstanceInfo;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.server.configuration.properties.GrayServerProperties;
import cn.springcloud.gray.server.discovery.ServiceDiscovery;
import cn.springcloud.gray.server.module.gray.GrayServerModule;
import cn.springcloud.gray.server.module.gray.domain.GrayDecision;
import cn.springcloud.gray.server.module.gray.domain.GrayInstance;
import cn.springcloud.gray.server.module.gray.domain.GrayPolicy;
import cn.springcloud.gray.server.module.gray.domain.GrayService;
import cn.springcloud.gray.server.module.user.ServiceManageModule;
import cn.springcloud.gray.server.module.user.domain.ServiceOwner;
import cn.springcloud.gray.server.service.GrayDecisionService;
import cn.springcloud.gray.server.service.GrayInstanceService;
import cn.springcloud.gray.server.service.GrayPolicyService;
import cn.springcloud.gray.server.service.GrayServiceService;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public class JPAGrayServerModule
implements GrayServerModule {
    private static final Logger log = LoggerFactory.getLogger(JPAGrayServerModule.class);
    private static final List<SourceType> INSTANCE_STATUS_CHECK_SOURCE_TYPES = Arrays.asList(new SourceType[]{SourceType.GRAY_POLICY, SourceType.GRAY_DECISION});
    private GrayServiceService grayServiceService;
    private GrayInstanceService grayInstanceService;
    private GrayDecisionService grayDecisionService;
    private GrayPolicyService grayPolicyService;
    private GraySourceEventPublisher grayEventPublisher;
    private GrayServerProperties grayServerProperties;
    private ServiceDiscovery serviceDiscovery;
    private ServiceManageModule serviceManageModule;

    public JPAGrayServerModule(GrayServerProperties grayServerProperties, GraySourceEventPublisher grayEventPublisher, ServiceDiscovery serviceDiscovery, GrayServiceService grayServiceService, GrayInstanceService grayInstanceService, GrayDecisionService grayDecisionService, GrayPolicyService grayPolicyService, ServiceManageModule serviceManageModule) {
        this.grayServerProperties = grayServerProperties;
        this.grayEventPublisher = grayEventPublisher;
        this.serviceDiscovery = serviceDiscovery;
        this.grayServiceService = grayServiceService;
        this.grayInstanceService = grayInstanceService;
        this.grayDecisionService = grayDecisionService;
        this.grayPolicyService = grayPolicyService;
        this.serviceManageModule = serviceManageModule;
    }

    @Override
    public List<GrayService> allGrayServices() {
        return this.grayServiceService.findAllModel();
    }

    @Override
    public List<GrayInstance> listGrayInstancesByStatus(GrayStatus grayStatus, Collection<InstanceStatus> instanceStatus) {
        return this.grayInstanceService.findAllByStatus(grayStatus, instanceStatus);
    }

    @Override
    public List<GrayInstance> listGrayInstancesByNormalInstanceStatus(Collection<InstanceStatus> instanceStatus) {
        return this.grayInstanceService.listGrayInstancesByNormalInstanceStatus(instanceStatus);
    }

    @Transactional
    @Override
    public void deleteGrayService(String serviceId) {
        List<GrayInstance> grayInstances = this.grayInstanceService.findByServiceId(serviceId);
        this.grayServiceService.deleteReactById(serviceId);
        this.serviceManageModule.deleteSericeManeges(serviceId);
        this.publishEventMsg(this.createEventMsg(SourceType.GRAY_SERVICE, EventType.DOWN, serviceId), null);
    }

    @Override
    public void updateGrayStatus(String instanceId, GrayStatus grayStatus) {
        GrayInstance instance = (GrayInstance)this.grayInstanceService.findOneModel(instanceId);
        if (instance != null && !Objects.equals((Object)instance.getGrayStatus(), (Object)grayStatus)) {
            instance.setGrayStatus(grayStatus);
            this.grayInstanceService.saveModel(instance);
            if (grayStatus == GrayStatus.OPEN) {
                this.publishUpdateIntanceEvent(instance);
            } else {
                this.publishDownIntanceEvent(instance);
            }
        }
    }

    @Transactional
    @Override
    public GrayInstance saveGrayInstance(GrayInstance instance) {
        InstanceInfo instanceInfo;
        GrayService grayService = (GrayService)this.grayServiceService.findOneModel(instance.getServiceId());
        if (grayService == null) {
            grayService = GrayService.builder().serviceId(instance.getServiceId()).serviceName(instance.getServiceId()).build();
            this.grayServiceService.saveModel(grayService);
            this.serviceManageModule.insertServiceOwner(grayService.getServiceId());
        }
        if (Objects.isNull((Object)instance.getInstanceStatus()) && !Objects.isNull(this.serviceDiscovery) && !Objects.isNull((Object)(instanceInfo = this.serviceDiscovery.getInstanceInfo(instance.getServiceId(), instance.getInstanceId())))) {
            instance.setInstanceStatus(instanceInfo.getInstanceStatus());
        }
        GrayInstance oldRecord = (GrayInstance)this.grayInstanceService.findOneModel(instance.getInstanceId());
        GrayInstance newRecord = this.grayInstanceService.saveModel(instance);
        if (this.isLockGray(newRecord) || this.grayServerProperties.getInstance().getNormalInstanceStatus().contains((Object)instance.getInstanceStatus())) {
            if (Objects.equals((Object)newRecord.getGrayStatus(), (Object)GrayStatus.OPEN)) {
                this.publishUpdateIntanceEvent(newRecord);
            } else if (oldRecord != null && Objects.equals((Object)oldRecord.getGrayStatus(), (Object)GrayStatus.OPEN)) {
                this.publishDownIntanceEvent(newRecord);
            }
        }
        return newRecord;
    }

    @Override
    public void updateInstanceStatus(String instanceId, InstanceStatus instanceStatus) {
        GrayInstance instance = (GrayInstance)this.grayInstanceService.findOneModel(instanceId);
        this.updateInstanceStatus(instance, instanceStatus);
    }

    @Override
    public void updateInstanceStatus(GrayInstance instance, InstanceStatus instanceStatus) {
        if (instance != null && !Objects.equals((Object)instance.getInstanceStatus(), (Object)instanceStatus)) {
            instance.setInstanceStatus(instanceStatus);
            this.grayInstanceService.saveModel(instance);
            if (this.isLockGray(instance)) {
                return;
            }
            if (instance.getGrayStatus() == GrayStatus.OPEN) {
                if (this.grayServerProperties.getInstance().getNormalInstanceStatus().contains((Object)instanceStatus)) {
                    this.publishUpdateIntanceEvent(instance);
                } else {
                    this.publishDownIntanceEvent(instance);
                }
            }
        }
    }

    @Transactional
    @Override
    public void deleteGrayInstance(String intanceId) {
        GrayInstance grayInstance = (GrayInstance)this.grayInstanceService.findOneModel(intanceId);
        if (grayInstance != null) {
            this.grayInstanceService.deleteReactById(intanceId);
            this.publishDownIntanceEvent(grayInstance);
        }
    }

    private boolean isLockGray(GrayInstance instance) {
        return Objects.equals(instance.getGrayLock(), 1);
    }

    @Override
    public GrayPolicy saveGrayPolicy(GrayPolicy grayPolicy) {
        GrayPolicy newRecord = this.grayPolicyService.saveModel(grayPolicy);
        this.publishEventMsg(this.createEventMsg(SourceType.GRAY_POLICY, EventType.UPDATE, (GrayInstance)this.grayInstanceService.findOneModel(newRecord.getInstanceId())), newRecord);
        return newRecord;
    }

    @Transactional
    @Override
    public void deleteGrayPolicy(Long policyId) {
        GrayPolicy policy = (GrayPolicy)this.grayPolicyService.findOneModel(policyId);
        if (policy != null) {
            this.grayPolicyService.deleteReactById(policyId);
            this.publishEventMsg(this.createEventMsg(SourceType.GRAY_POLICY, EventType.DOWN, (GrayInstance)this.grayInstanceService.findOneModel(policy.getInstanceId())), policy);
        }
    }

    @Override
    public GrayDecision saveGrayDecision(GrayDecision grayDecision) {
        GrayPolicy policy = (GrayPolicy)this.grayPolicyService.findOneModel(grayDecision.getPolicyId());
        grayDecision.setInstanceId(policy.getInstanceId());
        GrayDecision newRecord = this.grayDecisionService.saveModel(grayDecision);
        this.publishEventMsg(this.createEventMsg(SourceType.GRAY_DECISION, EventType.UPDATE, (GrayInstance)this.grayInstanceService.findOneModel(newRecord.getInstanceId())), newRecord);
        return newRecord;
    }

    @Override
    public void deleteGrayDecision(Long decisionId) {
        GrayDecision decision = (GrayDecision)this.grayDecisionService.findOneModel(decisionId);
        if (decision != null) {
            this.grayDecisionService.delete(decisionId);
            this.publishEventMsg(this.createEventMsg(SourceType.GRAY_DECISION, EventType.DOWN, (GrayInstance)this.grayInstanceService.findOneModel(decision.getInstanceId())), decision);
        }
    }

    @Override
    public GrayDecision getGrayDecision(Long id) {
        return (GrayDecision)this.grayDecisionService.findOneModel(id);
    }

    @Override
    public List<GrayDecision> listGrayDecisionsByPolicyId(Long policyId) {
        return this.grayDecisionService.findByPolicyId(policyId);
    }

    @Override
    public List<GrayInstance> listGrayInstancesByServiceId(String serviceId) {
        return this.grayInstanceService.findByServiceId(serviceId);
    }

    @Override
    public List<GrayInstance> listGrayInstancesByServiceId(String serviceId, Collection<InstanceStatus> instanceStatus) {
        return this.grayInstanceService.findByServiceId(serviceId, instanceStatus);
    }

    @Override
    public GrayInstance getGrayInstance(String id) {
        return (GrayInstance)this.grayInstanceService.findOneModel(id);
    }

    @Override
    public List<GrayService> listAllGrayServices() {
        return this.grayServiceService.findAllModel();
    }

    @Transactional
    @Override
    public GrayService saveGrayService(GrayService grayService) {
        GrayService record = this.grayServiceService.saveModel(grayService);
        if (this.serviceManageModule.getServiceOwner(grayService.getServiceId()) == null) {
            this.serviceManageModule.addServiceOwner(grayService.getServiceId());
        }
        this.publishEventMsg(this.createEventMsg(SourceType.GRAY_SERVICE, EventType.UPDATE, record.getServiceId()), record);
        return record;
    }

    @Override
    public GrayService getGrayService(String id) {
        return null;
    }

    @Override
    public List<GrayPolicy> listGrayPoliciesByInstanceId(String instanceId) {
        return this.grayPolicyService.findByInstanceId(instanceId);
    }

    @Override
    public Page<GrayService> listAllGrayServices(Pageable pageable) {
        return this.grayServiceService.listAllGrayServices(pageable);
    }

    @Override
    public List<GrayService> findGrayServices(Iterable<String> serviceIds) {
        return this.grayServiceService.findAllModel(serviceIds);
    }

    @Override
    public Page<GrayPolicy> listGrayPoliciesByInstanceId(String instanceId, Pageable pageable) {
        return this.grayPolicyService.listGrayPoliciesByInstanceId(instanceId, pageable);
    }

    @Override
    public Page<GrayInstance> listGrayInstancesByServiceId(String serviceId, Pageable pageable) {
        return this.grayInstanceService.listGrayInstancesByServiceId(serviceId, pageable);
    }

    @Override
    public Page<GrayDecision> listGrayDecisionsByPolicyId(Long policyId, Pageable pageable) {
        return this.grayDecisionService.listGrayDecisionsByPolicyId(policyId, pageable);
    }

    protected GraySourceEventPublisher getGrayEventPublisher() {
        return this.grayEventPublisher;
    }

    private void publishUpdateIntanceEvent(GrayInstance grayInstance) {
        GrayEventMsg eventMsg = this.createEventMsg(SourceType.GRAY_INSTANCE, EventType.UPDATE, grayInstance);
        if (eventMsg != null) {
            log.info("push event message -> {}", (Object)eventMsg);
            this.getGrayEventPublisher().publishEvent(eventMsg, (Object)grayInstance, 100L);
        }
    }

    private GrayEventMsg createEventMsg(SourceType sourceType, EventType eventType, GrayInstance grayInstance) {
        if (!this.isNeesPushEentMsg(sourceType, grayInstance)) {
            return null;
        }
        return GrayEventMsg.builder().serviceId(grayInstance.getServiceId()).instanceId(grayInstance.getInstanceId()).eventType(eventType).sourceType(sourceType).build();
    }

    private boolean isNeesPushEentMsg(SourceType sourceType, GrayInstance grayInstance) {
        if (INSTANCE_STATUS_CHECK_SOURCE_TYPES.contains((Object)sourceType)) {
            return Objects.equals((Object)grayInstance.getGrayStatus(), (Object)GrayStatus.OPEN) && (this.isLockGray(grayInstance) || this.grayServerProperties.getInstance().getNormalInstanceStatus().contains((Object)grayInstance.getInstanceStatus()));
        }
        return true;
    }

    private GrayEventMsg createEventMsg(SourceType sourceType, EventType eventType, String serviceId) {
        return GrayEventMsg.builder().serviceId(serviceId).eventType(eventType).sourceType(sourceType).build();
    }

    private void publishDownIntanceEvent(GrayInstance grayInstance) {
        this.publishEventMsg(this.createEventMsg(SourceType.GRAY_INSTANCE, EventType.DOWN, grayInstance), grayInstance);
    }

    public void publishEventMsg(GrayEventMsg eventMsg, Object source) {
        if (eventMsg == null) {
            return;
        }
        log.info("push event message -> {}", (Object)eventMsg);
        this.getGrayEventPublisher().asyncPublishEvent(eventMsg, source);
    }
}

