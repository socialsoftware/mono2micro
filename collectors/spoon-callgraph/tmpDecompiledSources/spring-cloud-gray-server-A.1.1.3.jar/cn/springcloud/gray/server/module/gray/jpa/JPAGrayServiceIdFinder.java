/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.module.gray.jpa;

import cn.springcloud.gray.server.module.gray.GrayModelType;
import cn.springcloud.gray.server.module.gray.GrayServiceIdFinder;
import cn.springcloud.gray.server.module.gray.domain.GrayDecision;
import cn.springcloud.gray.server.module.gray.domain.GrayInstance;
import cn.springcloud.gray.server.module.gray.domain.GrayPolicy;
import cn.springcloud.gray.server.module.gray.domain.GrayTrack;
import cn.springcloud.gray.server.service.GrayDecisionService;
import cn.springcloud.gray.server.service.GrayInstanceService;
import cn.springcloud.gray.server.service.GrayPolicyService;
import cn.springcloud.gray.server.service.GrayTrackService;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class JPAGrayServiceIdFinder
implements GrayServiceIdFinder {
    private GrayInstanceService grayInstanceService;
    private GrayTrackService grayTrackService;
    private GrayPolicyService grayPolicyService;
    private GrayDecisionService grayDecisionService;
    private Map<GrayModelType, Function<Object, String>> getFunctions = new HashMap<GrayModelType, Function<Object, String>>();

    public JPAGrayServiceIdFinder(GrayInstanceService grayInstanceService, GrayPolicyService grayPolicyService, GrayDecisionService grayDecisionService, GrayTrackService grayTrackService) {
        this.grayInstanceService = grayInstanceService;
        this.grayTrackService = grayTrackService;
        this.grayPolicyService = grayPolicyService;
        this.grayDecisionService = grayDecisionService;
        this.init();
    }

    public void init() {
        this.getFunctions.put(GrayModelType.TRACK, this::findByTrack);
        this.getFunctions.put(GrayModelType.INSTANCE, this::findByInstance);
        this.getFunctions.put(GrayModelType.POLICY, this::findByPolicy);
        this.getFunctions.put(GrayModelType.DECISION, this::findByDecision);
    }

    @Override
    public String getServiceId(GrayModelType grayModelType, Object id) {
        Function<Object, String> function = this.getFunctions.get((Object)grayModelType);
        if (function == null) {
            return "-1";
        }
        return StringUtils.defaultIfEmpty((CharSequence)function.apply(id), "-1");
    }

    private String findByTrack(Object id) {
        GrayTrack grayTrack = (GrayTrack)this.grayTrackService.findOneModel((Long)id);
        return grayTrack == null ? null : grayTrack.getServiceId();
    }

    private String findByInstance(Object id) {
        GrayInstance grayInstance = (GrayInstance)this.grayInstanceService.findOneModel((String)id);
        return grayInstance == null ? null : grayInstance.getServiceId();
    }

    private String findByPolicy(Object id) {
        GrayPolicy grayPolicy = (GrayPolicy)this.grayPolicyService.findOneModel((Long)id);
        if (grayPolicy != null) {
            return this.findByInstance(grayPolicy.getInstanceId());
        }
        return null;
    }

    private String findByDecision(Object id) {
        GrayDecision grayDecision = (GrayDecision)this.grayDecisionService.findOneModel((Long)id);
        if (grayDecision != null) {
            return this.findByInstance(grayDecision.getInstanceId());
        }
        return null;
    }
}

