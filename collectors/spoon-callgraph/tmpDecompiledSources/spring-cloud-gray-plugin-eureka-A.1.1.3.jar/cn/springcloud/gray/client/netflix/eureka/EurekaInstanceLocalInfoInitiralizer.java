/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.local.InstanceLocalInfo
 *  cn.springcloud.gray.local.InstanceLocalInfo$InstanceLocalInfoBuilder
 *  cn.springcloud.gray.local.InstanceLocalInfoInitiralizer
 *  com.netflix.appinfo.EurekaInstanceConfig
 *  org.springframework.beans.BeansException
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 */
package cn.springcloud.gray.client.netflix.eureka;

import cn.springcloud.gray.local.InstanceLocalInfo;
import cn.springcloud.gray.local.InstanceLocalInfoInitiralizer;
import com.netflix.appinfo.EurekaInstanceConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class EurekaInstanceLocalInfoInitiralizer
implements InstanceLocalInfoInitiralizer,
ApplicationContextAware {
    private ApplicationContext applicationContext;
    private InstanceLocalInfo instanceLocalInfo;

    public InstanceLocalInfo getInstanceLocalInfo() {
        if (this.instanceLocalInfo == null) {
            EurekaInstanceConfig eurekaInstanceConfig = (EurekaInstanceConfig)this.applicationContext.getBean(EurekaInstanceConfig.class);
            String instanceId = eurekaInstanceConfig.getInstanceId();
            int port = eurekaInstanceConfig.getNonSecurePort();
            if (eurekaInstanceConfig.getSecurePortEnabled()) {
                port = eurekaInstanceConfig.getSecurePort();
            }
            this.instanceLocalInfo = InstanceLocalInfo.builder().instanceId(instanceId).serviceId(eurekaInstanceConfig.getAppname()).host(eurekaInstanceConfig.getHostName(false)).port(port).build();
        }
        return this.instanceLocalInfo;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

