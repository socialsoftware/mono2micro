/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.api.ApiRes
 *  cn.springcloud.gray.api.ApiRes$ApiResBuilder
 *  io.swagger.annotations.Api
 *  io.swagger.annotations.ApiImplicitParam
 *  io.swagger.annotations.ApiImplicitParams
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
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.GetMapping
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
import cn.springcloud.gray.server.module.user.domain.ServiceOwner;
import cn.springcloud.gray.server.module.user.domain.ServiceOwnerQuery;
import cn.springcloud.gray.server.module.user.domain.UserInfo;
import cn.springcloud.gray.server.resources.domain.fo.ServiceOwnerFO;
import cn.springcloud.gray.server.utils.ApiResHelper;
import cn.springcloud.gray.server.utils.PaginationUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiParam;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(value={"/gray/service/owner"})
public class ServiceOwnerResource {
    @Autowired
    private ServiceManageModule serviceManageModule;
    @Autowired
    private UserModule userModule;

    @ApiImplicitParams(value={@ApiImplicitParam(name="serviceId", value="\u670d\u52a1id", dataType="string"), @ApiImplicitParam(name="queryItem", value="\u67e5\u8be2\u9879,{0:\u5168\u90e8, 1:\u5df2\u7ed1\u5b9aowner, 2:\u672a\u7ed1\u5b9aowner}", dataType="int", defaultValue="0", allowableValues="0,1,2")})
    @GetMapping(value={"/page"})
    public ResponseEntity<ApiRes<List<ServiceOwner>>> list(@RequestParam(value="serviceId", required=false) String serviceId, @RequestParam(value="queryItem", required=false) int queryItem, @ApiParam @PageableDefault(direction=Sort.Direction.DESC) Pageable pageable) {
        ServiceOwnerQuery query = ServiceOwnerQuery.builder().serviceId(serviceId).queryItem(queryItem).build();
        Page<ServiceOwner> serviceOwnerPage = this.serviceManageModule.queryServiceOwners(query, pageable);
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(serviceOwnerPage);
        ApiRes data = ApiRes.builder().code("0").data((Object)serviceOwnerPage.getContent()).build();
        return new ResponseEntity((Object)data, (MultiValueMap)headers, HttpStatus.OK);
    }

    @RequestMapping(value={"/"}, method={RequestMethod.PUT})
    public ApiRes<Void> save(@Validated @RequestBody ServiceOwnerFO serviceOwnerFO) {
        ServiceOwner serviceOwner = this.serviceManageModule.getServiceOwner(serviceOwnerFO.getServiceId());
        if (serviceOwner == null) {
            return ApiResHelper.notFound();
        }
        if (StringUtils.isNotEmpty(serviceOwner.getUserId()) && !StringUtils.equals(serviceOwner.getUserId(), this.userModule.getCurrentUserId())) {
            return ApiResHelper.notAuthority();
        }
        if (this.userModule.getUserInfo(serviceOwnerFO.getUserId()) == null) {
            return ApiResHelper.notFound("has not found user");
        }
        this.serviceManageModule.transferServiceOwner(serviceOwnerFO.getServiceId(), serviceOwnerFO.getUserId());
        return ApiRes.builder().code("0").build();
    }
}

