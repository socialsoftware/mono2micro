/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.DecisionDefinition
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayStatus
 *  cn.springcloud.gray.model.GrayTrackDefinition
 *  cn.springcloud.gray.model.InstanceStatus
 *  cn.springcloud.gray.model.PolicyDefinition
 *  org.springframework.util.StringUtils
 */
package cn.springcloud.gray.server.module.gray;

import cn.springcloud.gray.model.DecisionDefinition;
import cn.springcloud.gray.model.GrayStatus;
import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.model.PolicyDefinition;
import cn.springcloud.gray.server.configuration.properties.GrayServerProperties;
import cn.springcloud.gray.server.module.gray.GrayModule;
import cn.springcloud.gray.server.module.gray.GrayServerModule;
import cn.springcloud.gray.server.module.gray.GrayServerTrackModule;
import cn.springcloud.gray.server.module.gray.domain.GrayDecision;
import cn.springcloud.gray.server.module.gray.domain.GrayInstance;
import cn.springcloud.gray.server.module.gray.domain.GrayPolicy;
import cn.springcloud.gray.server.module.gray.domain.GrayTrack;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class SimpleGrayModule
implements GrayModule {
    private static final Logger log = LoggerFactory.getLogger(SimpleGrayModule.class);
    private GrayServerProperties grayServerProperties;
    private GrayServerModule grayServerModule;
    private GrayServerTrackModule grayServerTrackModule;
    private ObjectMapper objectMapper;

    public SimpleGrayModule(GrayServerProperties grayServerProperties, GrayServerModule grayServerModule, GrayServerTrackModule grayServerTrackModule, ObjectMapper objectMapper) {
        this.grayServerProperties = grayServerProperties;
        this.grayServerModule = grayServerModule;
        this.grayServerTrackModule = grayServerTrackModule;
        this.objectMapper = objectMapper;
    }

    @Override
    public GrayServerModule getGrayServerModule() {
        return this.grayServerModule;
    }

    @Override
    public cn.springcloud.gray.model.GrayInstance getGrayInstance(String serviceId, String instanceId) {
        GrayInstance grayInstance = this.grayServerModule.getGrayInstance(instanceId);
        if (grayInstance == null) {
            return null;
        }
        return this.ofGrayInstanceInfo(grayInstance);
    }

    @Override
    public List<GrayTrackDefinition> getTrackDefinitions(String serviceId, String instanceId) {
        List<GrayTrack> grayTracks = this.grayServerTrackModule.listGrayTracksEmptyInstanceByServiceId(serviceId);
        ArrayList<GrayTrackDefinition> trackDefinitions = new ArrayList<GrayTrackDefinition>(grayTracks.size());
        this.addGrayTrackDefinitions(trackDefinitions, grayTracks);
        this.addGrayTrackDefinitions(trackDefinitions, this.grayServerTrackModule.listGrayTracksByInstanceId(instanceId));
        return trackDefinitions;
    }

    private void addGrayTrackDefinitions(List<GrayTrackDefinition> trackDefinitions, List<GrayTrack> grayTracks) {
        grayTracks.forEach(track -> trackDefinitions.add(this.ofGrayTrack((GrayTrack)track)));
    }

    @Override
    public GrayTrackDefinition ofGrayTrack(GrayTrack grayTrack) {
        GrayTrackDefinition definition = new GrayTrackDefinition();
        definition.setName(grayTrack.getName());
        definition.setValue(grayTrack.getInfos());
        return definition;
    }

    @Override
    public List<cn.springcloud.gray.model.GrayInstance> allOpenInstances() {
        List<GrayInstance> instances = this.grayServerModule.listGrayInstancesByNormalInstanceStatus(this.grayServerProperties.getInstance().getNormalInstanceStatus());
        ArrayList<cn.springcloud.gray.model.GrayInstance> grayInstances = new ArrayList<cn.springcloud.gray.model.GrayInstance>(instances.size());
        instances.forEach(instance -> {
            if (Objects.equals((Object)instance.getGrayStatus(), (Object)GrayStatus.CLOSE)) {
                return;
            }
            cn.springcloud.gray.model.GrayInstance grayInstance = this.ofGrayInstanceInfo((GrayInstance)instance);
            grayInstances.add(grayInstance);
        });
        return grayInstances;
    }

    private cn.springcloud.gray.model.GrayInstance ofGrayInstanceInfo(GrayInstance instance) {
        cn.springcloud.gray.model.GrayInstance grayInstance = this.ofGrayInstance(instance);
        if (grayInstance.isGray()) {
            grayInstance.setPolicyDefinitions(this.ofGrayPoliciesByInstanceId(instance.getInstanceId()));
        }
        return grayInstance;
    }

    @Override
    public cn.springcloud.gray.model.GrayInstance ofGrayInstance(GrayInstance instance) {
        cn.springcloud.gray.model.GrayInstance grayInstance = new cn.springcloud.gray.model.GrayInstance();
        grayInstance.setPort(instance.getPort());
        grayInstance.setServiceId(instance.getServiceId());
        grayInstance.setInstanceId(instance.getInstanceId());
        grayInstance.setGrayStatus(instance.getGrayStatus());
        grayInstance.setHost(instance.getHost());
        return grayInstance;
    }

    private List<PolicyDefinition> ofGrayPoliciesByInstanceId(String instanceId) {
        List<GrayPolicy> grayPolicies = this.grayServerModule.listGrayPoliciesByInstanceId(instanceId);
        ArrayList<PolicyDefinition> policyDefinitions = new ArrayList<PolicyDefinition>(grayPolicies.size());
        grayPolicies.forEach(grayPolicy -> {
            PolicyDefinition policyDefinition = this.ofGrayPolicy((GrayPolicy)grayPolicy);
            policyDefinition.setList(this.ofGrayDecisionByPolicyId(grayPolicy.getId()));
            policyDefinitions.add(policyDefinition);
        });
        return policyDefinitions;
    }

    @Override
    public PolicyDefinition ofGrayPolicy(GrayPolicy grayPolicy) {
        PolicyDefinition policyDefinition = new PolicyDefinition();
        policyDefinition.setAlias(grayPolicy.getAlias());
        policyDefinition.setPolicyId(String.valueOf(grayPolicy.getId()));
        return policyDefinition;
    }

    private List<DecisionDefinition> ofGrayDecisionByPolicyId(Long policyId) {
        List<GrayDecision> grayDecisions = this.grayServerModule.listGrayDecisionsByPolicyId(policyId);
        ArrayList<DecisionDefinition> decisionDefinitions = new ArrayList<DecisionDefinition>(grayDecisions.size());
        grayDecisions.forEach(grayDecision -> {
            try {
                DecisionDefinition definition = this.ofGrayDecision((GrayDecision)grayDecision);
                if (definition != null) {
                    decisionDefinitions.add(definition);
                }
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        return decisionDefinitions;
    }

    @Override
    public DecisionDefinition ofGrayDecision(GrayDecision grayDecision) throws IOException {
        DecisionDefinition decisionDefinition = new DecisionDefinition();
        decisionDefinition.setId(String.valueOf(grayDecision.getId()));
        decisionDefinition.setName(grayDecision.getName());
        if (StringUtils.isEmpty((Object)grayDecision.getInfos())) {
            return null;
        }
        decisionDefinition.setInfos((Map)this.objectMapper.readValue(grayDecision.getInfos(), (TypeReference)new TypeReference<Map<String, String>>(){}));
        return decisionDefinition;
    }

}

