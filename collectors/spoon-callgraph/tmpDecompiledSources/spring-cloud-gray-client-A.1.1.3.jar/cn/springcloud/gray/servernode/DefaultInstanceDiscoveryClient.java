/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceStatus
 *  org.springframework.beans.BeansException
 *  org.springframework.beans.factory.InitializingBean
 *  org.springframework.cloud.client.serviceregistry.Registration
 *  org.springframework.cloud.client.serviceregistry.ServiceRegistry
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 */
package cn.springcloud.gray.servernode;

import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.servernode.InstanceDiscoveryClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DefaultInstanceDiscoveryClient
implements InstanceDiscoveryClient,
ApplicationContextAware,
InitializingBean {
    private ServiceRegistry<Registration> serviceRegistry;
    private Registration registration;
    private ApplicationContext applicationContext;

    @Override
    public void setStatus(InstanceStatus status) {
        this.serviceRegistry.setStatus(this.registration, status.name());
    }

    public void afterPropertiesSet() throws Exception {
        this.registration = (Registration)this.applicationContext.getBean(Registration.class);
        this.serviceRegistry = (ServiceRegistry)this.applicationContext.getBean(ServiceRegistry.class);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

