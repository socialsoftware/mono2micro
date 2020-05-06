/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.DecisionDefinition
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayTrackDefinition
 *  cn.springcloud.gray.model.PolicyDefinition
 */
package cn.springcloud.gray.server.module.gray;

import cn.springcloud.gray.model.DecisionDefinition;
import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.model.PolicyDefinition;
import cn.springcloud.gray.server.module.gray.GrayServerModule;
import cn.springcloud.gray.server.module.gray.domain.GrayDecision;
import cn.springcloud.gray.server.module.gray.domain.GrayInstance;
import cn.springcloud.gray.server.module.gray.domain.GrayPolicy;
import cn.springcloud.gray.server.module.gray.domain.GrayTrack;
import java.io.IOException;
import java.util.List;

public interface GrayModule {
    public GrayServerModule getGrayServerModule();

    public GrayTrackDefinition ofGrayTrack(GrayTrack var1);

    public List<cn.springcloud.gray.model.GrayInstance> allOpenInstances();

    public cn.springcloud.gray.model.GrayInstance getGrayInstance(String var1, String var2);

    public List<GrayTrackDefinition> getTrackDefinitions(String var1, String var2);

    public cn.springcloud.gray.model.GrayInstance ofGrayInstance(GrayInstance var1);

    public PolicyDefinition ofGrayPolicy(GrayPolicy var1);

    public DecisionDefinition ofGrayDecision(GrayDecision var1) throws IOException;
}

