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
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package cn.springcloud.gray.server.resources.rest;

import cn.springcloud.gray.api.ApiRes;
import cn.springcloud.gray.server.module.user.ServiceManageModule;
import cn.springcloud.gray.server.module.user.UserModule;
import cn.springcloud.gray.server.module.user.domain.UserInfo;
import cn.springcloud.gray.server.module.user.domain.UserServiceAuthority;
import cn.springcloud.gray.server.resources.domain.fo.ServiceAuthorityFO;
import cn.springcloud.gray.server.utils.ApiResHelper;
import cn.springcloud.gray.server.utils.PaginationUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(value={"/gray/service/authority"})
public class ServiceAuthorityResource {
    @Autowired
    private ServiceManageModule serviceManageModule;
    @Autowired
    private UserModule userModule;

    @GetMapping(value={"/page"})
    public ResponseEntity<ApiRes<List<UserServiceAuthority>>> list(@RequestParam(value="serviceId") String serviceId, @ApiParam @PageableDefault(sort={"id"}, direction=Sort.Direction.DESC) Pageable pageable) {
        Page<UserServiceAuthority> serviceAuthorityPage = this.serviceManageModule.listServiceAuthorities(serviceId, pageable);
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(serviceAuthorityPage);
        ApiRes data = ApiRes.builder().code("0").data((Object)serviceAuthorityPage.getContent()).build();
        return new ResponseEntity((Object)data, (MultiValueMap)headers, HttpStatus.OK);
    }

    @RequestMapping(value={"/{id}"}, method={RequestMethod.DELETE})
    public ApiRes<Void> delete(@PathVariable(value="id") Long id) {
        UserServiceAuthority serviceAuthority = this.serviceManageModule.getServiceAuthority(id);
        if (serviceAuthority == null) {
            return ApiResHelper.notFound();
        }
        String serviceId = serviceAuthority.getServiceId();
        if (!this.serviceManageModule.hasServiceAuthority(serviceId)) {
            return ApiResHelper.notAuthority();
        }
        if (this.serviceManageModule.isServiceOwner(serviceId, serviceAuthority.getUserId())) {
            return ApiRes.builder().code("403").message("service owner is can not delete").build();
        }
        this.serviceManageModule.deleteServiceAuthority(id);
        return ApiRes.builder().code("0").build();
    }

    @RequestMapping(value={"/"}, method={RequestMethod.POST})
    public ApiRes<UserServiceAuthority> save(@RequestBody ServiceAuthorityFO serviceAuthorityFO) {
        if (!this.serviceManageModule.hasServiceAuthority(serviceAuthorityFO.getServiceId())) {
            return ApiResHelper.notAuthority();
        }
        if (this.userModule.getUserInfo(serviceAuthorityFO.getUserId()) == null) {
            return ApiResHelper.notFound("has not found user");
        }
        UserServiceAuthority serviceAuthority = this.serviceManageModule.addServiceAuthority(serviceAuthorityFO.getServiceId(), serviceAuthorityFO.getUserId());
        return ApiResHelper.successData(serviceAuthority);
    }
}

