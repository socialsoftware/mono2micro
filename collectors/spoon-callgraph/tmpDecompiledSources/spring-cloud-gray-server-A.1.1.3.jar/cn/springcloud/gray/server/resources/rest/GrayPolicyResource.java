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
 *  org.springframework.web.bind.annotation.PathVariable
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
import cn.springcloud.gray.server.module.gray.domain.GrayPolicy;
import cn.springcloud.gray.server.module.user.ServiceManageModule;
import cn.springcloud.gray.server.module.user.UserModule;
import cn.springcloud.gray.server.utils.ApiResHelper;
import cn.springcloud.gray.server.utils.PaginationUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/gray/policy"})
public class GrayPolicyResource {
    @Autowired
    private GrayServerModule grayServerModule;
    @Autowired
    private ServiceManageModule serviceManageModule;
    @Autowired
    private GrayServiceIdFinder grayServiceIdFinder;
    @Autowired
    private UserModule userModule;

    @RequestMapping(value={"list"}, method={RequestMethod.GET}, params={"instanceId"})
    public ApiRes<List<GrayPolicy>> listByInstanceId(@RequestParam(value="instanceId") String instanceId) {
        return ApiRes.builder().code("0").data(this.grayServerModule.listGrayPoliciesByInstanceId(instanceId)).build();
    }

    @GetMapping(value={"/page"})
    public ResponseEntity<ApiRes<List<GrayPolicy>>> page(@RequestParam(value="instanceId") String instanceId, @ApiParam @PageableDefault(sort={"id"}, direction=Sort.Direction.DESC) Pageable pageable) {
        Page<GrayPolicy> page = this.grayServerModule.listGrayPoliciesByInstanceId(instanceId, pageable);
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(page);
        ApiRes res = ApiRes.builder().code("0").data((Object)page.getContent()).build();
        return new ResponseEntity((Object)res, (MultiValueMap)headers, HttpStatus.OK);
    }

    @RequestMapping(value={"{id}"}, method={RequestMethod.DELETE})
    public ApiRes<Void> delete(@PathVariable(value="id") Long id) {
        if (!this.serviceManageModule.hasServiceAuthority(this.grayServiceIdFinder.getServiceId(GrayModelType.POLICY, id))) {
            return ApiResHelper.notAuthority();
        }
        this.grayServerModule.deleteGrayPolicy(id);
        return ApiRes.builder().code("0").build();
    }

    @RequestMapping(value={"/"}, method={RequestMethod.POST})
    public ApiRes<GrayPolicy> save(@RequestBody GrayPolicy grayPolicy) {
        if (!this.serviceManageModule.hasServiceAuthority(this.grayServiceIdFinder.getServiceId(GrayModelType.INSTANCE, grayPolicy.getInstanceId()))) {
            return ApiResHelper.notAuthority();
        }
        grayPolicy.setOperator(this.userModule.getCurrentUserId());
        grayPolicy.setOperateTime(new Date());
        return ApiRes.builder().code("0").data((Object)this.grayServerModule.saveGrayPolicy(grayPolicy)).build();
    }
}

