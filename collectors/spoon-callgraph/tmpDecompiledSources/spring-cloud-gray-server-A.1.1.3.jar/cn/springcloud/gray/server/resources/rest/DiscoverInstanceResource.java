/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.api.ApiRes
 *  cn.springcloud.gray.api.ApiRes$ApiResBuilder
 *  cn.springcloud.gray.model.GrayStatus
 *  cn.springcloud.gray.model.InstanceInfo
 *  cn.springcloud.gray.model.InstanceStatus
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestMethod
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.client.RestTemplate
 */
package cn.springcloud.gray.server.resources.rest;

import cn.springcloud.gray.api.ApiRes;
import cn.springcloud.gray.model.GrayStatus;
import cn.springcloud.gray.model.InstanceInfo;
import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.server.discovery.ServiceDiscovery;
import cn.springcloud.gray.server.module.client.ClientRemoteModule;
import cn.springcloud.gray.server.module.gray.GrayServerModule;
import cn.springcloud.gray.server.module.gray.domain.GrayInstance;
import cn.springcloud.gray.server.module.user.ServiceManageModule;
import cn.springcloud.gray.server.resources.domain.fo.RemoteInstanceStatusUpdateFO;
import cn.springcloud.gray.server.utils.ApiResHelper;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value={"/gray/discover"})
public class DiscoverInstanceResource {
    @Autowired
    private GrayServerModule grayServerModule;
    @Autowired
    private ServiceDiscovery serviceDiscovery;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ServiceManageModule serviceManageModule;
    @Autowired
    private ClientRemoteModule clientRemoteModule;

    @RequestMapping(value={"/instanceInfo"}, method={RequestMethod.POST})
    public ResponseEntity<Void> instanceInfo(@RequestBody InstanceInfo instanceInfo) {
        if (StringUtils.isNotEmpty(instanceInfo.getInstanceId()) && instanceInfo.getInstanceStatus() != null) {
            this.grayServerModule.updateInstanceStatus(instanceInfo.getInstanceId(), instanceInfo.getInstanceStatus());
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value={"/instanceInfo/setInstanceStatus"}, method={RequestMethod.PUT})
    public ResponseEntity<ApiRes<Void>> setInstanceStatus(@RequestBody RemoteInstanceStatusUpdateFO instanceStatusUpdateFO) {
        if (!this.serviceManageModule.hasServiceAuthority(instanceStatusUpdateFO.getServiceId())) {
            return ResponseEntity.status((HttpStatus)HttpStatus.FORBIDDEN).body(ApiResHelper.notAuthority());
        }
        this.clientRemoteModule.callClient(instanceStatusUpdateFO.getServiceId(), instanceStatusUpdateFO.getInstanceId(), "/gray/discovery/instance/setStatus?status={status}", url -> this.restTemplate.put(url, null, new Object[]{instanceStatusUpdateFO.getInstanceStatus().name()}));
        return ResponseEntity.ok((Object)ApiRes.builder().code("0").build());
    }

    @RequestMapping(value={"/instances"}, method={RequestMethod.GET}, params={"serviceId"})
    public ApiRes<List<GrayInstance>> instances(@RequestParam(value="serviceId") String serviceId) {
        List<InstanceInfo> instanceInfos = this.serviceDiscovery.listInstanceInfos(serviceId);
        List grayInstances = instanceInfos.stream().map(info -> {
            GrayInstance instance = new GrayInstance();
            instance.setServiceId(serviceId);
            instance.setInstanceId(info.getInstanceId());
            instance.setHost(info.getHost());
            instance.setPort(info.getPort());
            instance.setInstanceStatus(info.getInstanceStatus());
            instance.setGrayStatus(GrayStatus.CLOSE);
            GrayInstance grayInstance = this.grayServerModule.getGrayInstance(instance.getInstanceId());
            if (grayInstance != null) {
                instance.setGrayStatus(grayInstance.getGrayStatus());
            }
            return instance;
        }).collect(Collectors.toList());
        return ApiResHelper.successData(grayInstances);
    }
}

