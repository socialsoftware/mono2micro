/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayTrackDefinition
 */
package cn.springcloud.gray.communication;

import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayTrackDefinition;
import java.util.List;

public interface InformationClient {
    public List<GrayInstance> allGrayInstances();

    public void addGrayInstance(GrayInstance var1);

    public GrayInstance getGrayInstance(String var1, String var2);

    public void serviceDownline(String var1);

    public List<GrayTrackDefinition> getTrackDefinitions(String var1, String var2);
}

