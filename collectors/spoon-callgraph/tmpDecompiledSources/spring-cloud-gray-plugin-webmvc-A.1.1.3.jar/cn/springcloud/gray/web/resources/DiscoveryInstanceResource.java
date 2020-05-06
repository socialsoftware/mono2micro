/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.InstanceStatus
 *  cn.springcloud.gray.servernode.InstanceDiscoveryClient
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestMethod
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package cn.springcloud.gray.web.resources;

import cn.springcloud.gray.model.InstanceStatus;
import cn.springcloud.gray.servernode.InstanceDiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/gray/discovery/instance"})
public class DiscoveryInstanceResource {
    @Autowired
    private InstanceDiscoveryClient instanceDiscoveryClient;

    @RequestMapping(value={"/setStatus"}, method={RequestMethod.PUT})
    public void setStatus(@RequestParam(value="status") InstanceStatus status) {
        this.instanceDiscoveryClient.setStatus(status);
    }
}

