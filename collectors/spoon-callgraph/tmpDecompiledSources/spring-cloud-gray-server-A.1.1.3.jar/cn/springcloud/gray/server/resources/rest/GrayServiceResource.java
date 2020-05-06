/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.api.ApiRes
 *  cn.springcloud.gray.api.ApiRes$ApiResBuilder
 *  io.swagger.annotations.Api
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
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestMethod
 *  org.springframework.web.bind.annotation.RestController
 */
package cn.springcloud.gray.server.resources.rest;

import cn.springcloud.gray.api.ApiRes;
import cn.springcloud.gray.server.module.gray.GrayServerModule;
import cn.springcloud.gray.server.module.gray.domain.GrayService;
import cn.springcloud.gray.server.module.user.ServiceManageModule;
import cn.springcloud.gray.server.module.user.UserModule;
import cn.springcloud.gray.server.utils.ApiResHelper;
import cn.springcloud.gray.server.utils.PaginationUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import java.util.Date;
import java.util.List;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(value={"/gray/service"})
public class GrayServiceResource {
    @Autowired
    private GrayServerModule grayServerModule;
    @Autowired
    private ServiceManageModule serviceManageModule;
    @Autowired
    private UserModule userModule;

    @RequestMapping(value={"/list"}, method={RequestMethod.GET})
    public ApiRes<List<GrayService>> list() {
        return ApiRes.builder().code("0").data(this.grayServerModule.listAllGrayServices()).build();
    }

    @GetMapping(value={"/page"})
    public ResponseEntity<ApiRes<List<GrayService>>> list(@ApiParam @PageableDefault(direction=Sort.Direction.DESC) Pageable pageable) {
        Page<String> servicePage = this.serviceManageModule.listAllServiceIds(this.userModule.getCurrentUserId(), pageable);
        List<GrayService> grayServices = this.grayServerModule.findGrayServices(servicePage.getContent());
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(servicePage);
        ApiRes data = ApiRes.builder().code("0").data(grayServices).build();
        return new ResponseEntity((Object)data, (MultiValueMap)headers, HttpStatus.OK);
    }

    @RequestMapping(value={"/{id}"}, method={RequestMethod.GET})
    public ApiRes<GrayService> info(@PathVariable(value="id") String id) {
        return ApiRes.builder().code("0").data((Object)this.grayServerModule.getGrayService(id)).build();
    }

    @RequestMapping(value={"/{id}"}, method={RequestMethod.DELETE})
    public ApiRes<Void> delete(@PathVariable(value="id") String id) {
        if (!this.serviceManageModule.hasServiceAuthority(id) || !this.serviceManageModule.isServiceOwner(id, this.userModule.getCurrentUserId())) {
            return ApiResHelper.notAuthority();
        }
        this.grayServerModule.deleteGrayService(id);
        return ApiRes.builder().code("0").build();
    }

    @RequestMapping(value={"/"}, method={RequestMethod.POST})
    public ApiRes<Void> save(@RequestBody GrayService grayService) {
        if (this.grayServerModule.getGrayService(grayService.getServiceId()) != null && !this.serviceManageModule.hasServiceAuthority(grayService.getServiceId())) {
            return ApiResHelper.notAuthority();
        }
        grayService.setOperator(this.userModule.getCurrentUserId());
        grayService.setOperateTime(new Date());
        this.grayServerModule.saveGrayService(grayService);
        return ApiRes.builder().code("0").build();
    }
}

