/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceStatus
 *  cn.springcloud.gray.servernode.InstanceDiscoveryClient
 *  org.springframework.beans.BeansException
 *  org.springframework.beans.factory.InitializingBean
 *  org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration
 *  org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 */
package cn.springcloud.gray.client.netflix.eureka;

import cn.springcloud.gray.client.netflix.eureka.EurekaInstatnceTransformer;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.servernode.InstanceDiscoveryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class EurekaInstanceDiscoveryClient
implements ApplicationContextAware,
InitializingBean,
InstanceDiscoveryClient {
    private static final Logger log = LoggerFactory.getLogger(EurekaInstanceDiscoveryClient.class);
    private EurekaServiceRegistry eurekaServiceRegistry;
    private EurekaRegistration eurekaRegistration;
    private ApplicationContext applicationContext;

    public void setStatus(InstanceStatus status) {
        this.eurekaServiceRegistry.setStatus(this.eurekaRegistration, EurekaInstatnceTransformer.toEurekaInstanceStatus(status).name());
    }

    public void afterPropertiesSet() throws Exception {
        this.eurekaServiceRegistry = (EurekaServiceRegistry)this.applicationContext.getBean(EurekaServiceRegistry.class);
        this.eurekaRegistration = (EurekaRegistration)this.applicationContext.getBean(EurekaRegistration.class);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

