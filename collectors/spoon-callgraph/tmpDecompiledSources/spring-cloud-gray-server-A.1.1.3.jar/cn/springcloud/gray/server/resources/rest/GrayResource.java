/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.model.GrayInstance
 *  cn.springcloud.gray.model.GrayTrackDefinition
 *  io.swagger.annotations.Api
 *  io.swagger.annotations.ApiOperation
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestMethod
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package cn.springcloud.gray.server.resources.rest;

import cn.springcloud.gray.model.GrayInstance;
import cn.springcloud.gray.model.GrayTrackDefinition;
import cn.springcloud.gray.server.module.gray.GrayModule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(value="gray-client\u8c03\u7528\u7684\u63a5\u53e3")
@RestController
@RequestMapping(value={"/gray"})
public class GrayResource {
    @Autowired
    private GrayModule grayModule;

    @ApiOperation(value="\u8fd4\u56de\u6240\u6709\u5df2\u7ecf\u6253\u5f00\u7070\u5ea6\u72b6\u6001\u7684\u5b9e\u4f8b\u4fe1\u606f\uff08\u5305\u542b\u51b3\u7b56\u4fe1\u606f\uff09")
    @RequestMapping(value={"/instances/enable"}, method={RequestMethod.GET})
    public List<GrayInstance> allOpens() {
        return this.grayModule.allOpenInstances();
    }

    @ApiOperation(value="\u8fd4\u56de\u6307\u5b9a\u5b9e\u4f8b\u7684\u4fe1\u606f\uff08\u5305\u542b\u51b3\u7b56\u4fe1\u606f\uff09")
    @RequestMapping(value={"/instance"}, method={RequestMethod.GET})
    public GrayInstance instance(@RequestParam(value="serviceId") String serviceId, @RequestParam(value="instanceId") String instanceId) {
        return this.grayModule.getGrayInstance(serviceId, instanceId);
    }

    @ApiOperation(value="\u8fd4\u56de\u6307\u5b9a\u5b9e\u4f8b\u7684\u7070\u5ea6\u8ffd\u8e2a\u4fe1\u606f\uff08\u5305\u542b\u51b3\u7b56\u4fe1\u606f\uff09")
    @RequestMapping(value={"/trackDefinitions"}, method={RequestMethod.GET})
    public List<GrayTrackDefinition> trackDefinitions(@RequestParam(value="serviceId") String serviceId, @RequestParam(value="instanceId") String instanceId) {
        return this.grayModule.getTrackDefinitions(serviceId, instanceId);
    }
}

