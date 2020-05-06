/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.exceptions.NotFoundException
 *  cn.springcloud.gray.model.InstanceInfo
 */
package cn.springcloud.gray.server.module.client;

import cn.springcloud.gray.exceptions.NotFoundException;
import cn.springcloud.gray.model.InstanceInfo;
import cn.springcloud.gray.server.discovery.ServiceDiscovery;
import cn.springcloud.gray.server.module.client.ClientRemoteModule;
import cn.springcloud.gray.server.module.gray.GrayServerModule;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

public class DefaultClientRemoteModule
implements ClientRemoteModule {
    private ServiceDiscovery serviceDiscovery;
    private GrayServerModule grayServerModule;

    public DefaultClientRemoteModule(ServiceDiscovery serviceDiscovery, GrayServerModule grayServerModule) {
        this.serviceDiscovery = serviceDiscovery;
        this.grayServerModule = grayServerModule;
    }

    @Override
    public String getClientPath(String serviceId, String instanceId) {
        InstanceInfo instanceInfo = this.serviceDiscovery.getInstanceInfo(serviceId, instanceId);
        if (instanceInfo == null) {
            throw new NotFoundException();
        }
        if (StringUtils.isEmpty(instanceInfo.getHost()) || Objects.equals(instanceInfo.getPort(), 0)) {
            throw new IllegalArgumentException();
        }
        String contextPath = this.grayServerModule.getServiceContextPath(serviceId);
        StringBuilder path = new StringBuilder("http://").append(instanceInfo.getHost()).append(":").append(instanceInfo.getPort()).append(contextPath);
        return path.toString();
    }

    @Override
    public <T> T callClient(String serviceId, String instanceId, String uri, Function<String, T> function) {
        return function.apply(this.getClientPath(serviceId, instanceId) + uri);
    }

    @Override
    public void callClient(String serviceId, String instanceId, String uri, Consumer<String> consumer) {
        consumer.accept(this.getClientPath(serviceId, instanceId) + uri);
    }
}

