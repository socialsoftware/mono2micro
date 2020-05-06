/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.cloud.client.serviceregistry.Registration
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 */
package cn.springcloud.gray.local;

import cn.springcloud.gray.local.InstanceLocalInfo;
import cn.springcloud.gray.local.InstanceLocalInfoInitiralizer;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class LazyInstanceLocalInfoInitiralizer
implements InstanceLocalInfoInitiralizer,
ApplicationContextAware {
    protected ApplicationContext applicationContext;
    protected InstanceLocalInfo instanceLocalInfo;

    @Override
    public InstanceLocalInfo getInstanceLocalInfo() {
        if (this.instanceLocalInfo == null) {
            this.instanceLocalInfo = this.createInstanceLocalInfo();
        }
        return this.instanceLocalInfo;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected InstanceLocalInfo createInstanceLocalInfo() {
        Registration registration = (Registration)this.applicationContext.getBean(Registration.class);
        return InstanceLocalInfo.builder().instanceId(this.getLocalInstanceId()).serviceId(registration.getServiceId()).host(registration.getHost()).port(registration.getPort()).build();
    }

    protected abstract String getLocalInstanceId();
}

