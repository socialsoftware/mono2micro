/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceInfo
 */
package cn.springcloud.gray.eureka.server.communicate;

import cn.springcloud.gray.model.InstanceInfo;

public interface GrayCommunicateClient {
    public void noticeInstanceInfo(InstanceInfo var1);
}

