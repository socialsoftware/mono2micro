/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.api.ApiRes
 *  cn.springcloud.gray.api.ApiRes$ApiResBuilder
 *  io.swagger.annotations.ApiParam
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.domain.Sort
 *  org.springframework.data.domain.Sort$Direction
 *  org.springframework.data.web.PageableDefault
 *  org.springframework.http.HttpHeaders
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.ResponseEntity
 *  org.springframework.util.MultiValueMap
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestMethod
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package cn.springcloud.gray.server.resources.rest;

import cn.springcloud.gray.api.ApiRes;
import cn.springcloud.gray.server.module.gray.GrayModelType;
import cn.springcloud.gray.server.module.gray.GrayServerModule;
import cn.springcloud.gray.server.module.gray.GrayServiceIdFinder;
import cn.springcloud.gray.server.module.gray.domain.GrayInstance;
import cn.springcloud.gray.server.module.user.ServiceManageModule;
import cn.springcloud.gray.server.module.user.UserModule;
import cn.springcloud.gray.server.utils.ApiResHelper;
import cn.springcloud.gray.server.utils.PaginationUtils;
import io.swagger.annotations.ApiParam;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/gray/instance"})
public class GrayInstanceResource {
    @Autowired
    private GrayServerModule grayServerModule;
    @Autowired
    private UserModule userModule;
    @Autowired
    private ServiceManageModule serviceManageModule;
    @Autowired
    private GrayServiceIdFinder grayServiceIdFinder;

    @RequestMapping(value={"/list"}, method={RequestMethod.GET}, params={"serviceId"})
    public ApiRes<List<GrayInstance>> listByServiceId(@RequestParam(value="serviceId") String serviceId) {
        return ApiRes.builder().code("0").data(this.grayServerModule.listGrayInstancesByServiceId(serviceId)).build();
    }

    @GetMapping(value={"/page"})
    public ResponseEntity<ApiRes<List<GrayInstance>>> page(@RequestParam(value="serviceId") String serviceId, @ApiParam @PageableDefault(direction=Sort.Direction.DESC) Pageable pageable) {
        Page<GrayInstance> page = this.grayServerModule.listGrayInstancesByServiceId(serviceId, pageable);
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(page);
        ApiRes res = ApiRes.builder().code("0").data((Object)page.getContent()).build();
        return new ResponseEntity((Object)res, (MultiValueMap)headers, HttpStatus.OK);
    }

    @RequestMapping(value={"/"}, method={RequestMethod.GET})
    public ApiRes<GrayInstance> info(@RequestParam(value="id") String id) {
        return ApiRes.builder().code("0").data((Object)this.grayServerModule.getGrayInstance(id)).build();
    }

    @RequestMapping(value={"/"}, method={RequestMethod.DELETE})
    public ApiRes<Void> delete(@RequestParam(value="id") String id) {
        if (!this.serviceManageModule.hasServiceAuthority(this.grayServiceIdFinder.getServiceId(GrayModelType.INSTANCE, id))) {
            return ApiResHelper.notAuthority();
        }
        this.grayServerModule.deleteGrayInstance(id);
        return ApiRes.builder().code("0").build();
    }

    @RequestMapping(value={"/"}, method={RequestMethod.POST})
    public ApiRes<Void> save(@RequestBody GrayInstance grayInstance) {
        if (StringUtils.isNotEmpty(this.userModule.getCurrentUserId()) && !this.serviceManageModule.hasServiceAuthority(grayInstance.getServiceId())) {
            return ApiResHelper.notAuthority();
        }
        grayInstance.setOperator(this.userModule.getCurrentUserId());
        grayInstance.setOperateTime(new Date());
        this.grayServerModule.saveGrayInstance(grayInstance);
        return ApiRes.builder().code("0").build();
    }

    @RequestMapping(value={"/switchStatus"}, method={RequestMethod.PUT})
    public ApiRes<Void> switchGrayStatus(@RequestParam(value="id") String instanceId, @ApiParam(value="\u7070\u5ea6\u5f00\u5173{0: close, 1: open}", defaultValue="0") @RequestParam(value="switch") int onoff) {
        if (StringUtils.isNotEmpty(this.userModule.getCurrentUserId()) && !this.serviceManageModule.hasServiceAuthority(this.grayServiceIdFinder.getServiceId(GrayModelType.INSTANCE, instanceId))) {
            return ApiResHelper.notAuthority();
        }
        switch (onoff) {
            case 1: {
                this.grayServerModule.openGray(instanceId);
                return ApiRes.builder().code("0").build();
            }
            case 0: {
                this.grayServerModule.closeGray(instanceId);
                return ApiRes.builder().code("0").build();
            }
        }
        throw new UnsupportedOperationException("\u4e0d\u652f\u6301\u7684\u5f00\u5173\u7c7b\u578b");
    }
}

