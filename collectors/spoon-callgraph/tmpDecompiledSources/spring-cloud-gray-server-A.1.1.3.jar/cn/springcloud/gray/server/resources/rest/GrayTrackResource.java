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
import cn.springcloud.gray.server.module.gray.GrayServerTrackModule;
import cn.springcloud.gray.server.module.gray.domain.GrayTrack;
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
@RequestMapping(value={"/gray/track"})
public class GrayTrackResource {
    @Autowired
    private GrayServerTrackModule grayServerTrackModule;
    @Autowired
    private ServiceManageModule serviceManageModule;
    @Autowired
    private UserModule userModule;

    @RequestMapping(value={"listByInstance"}, method={RequestMethod.GET}, params={"instanceId"})
    public ApiRes<List<GrayTrack>> listByInstance(@RequestParam(value="instanceId") String instanceId) {
        return ApiRes.builder().code("0").data(this.grayServerTrackModule.listGrayTracksByInstanceId(instanceId)).build();
    }

    @GetMapping(value={"/pageByInstance"})
    public ResponseEntity<ApiRes<List<GrayTrack>>> pageByInstance(@RequestParam(value="instanceId") String instanceId, @ApiParam @PageableDefault(direction=Sort.Direction.DESC) Pageable pageable) {
        Page<GrayTrack> page = this.grayServerTrackModule.listGrayTracksByInstanceId(instanceId, pageable);
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(page);
        return new ResponseEntity((Object)ApiRes.builder().code("0").data((Object)page.getContent()).build(), (MultiValueMap)headers, HttpStatus.OK);
    }

    @RequestMapping(value={"listByService"}, method={RequestMethod.GET}, params={"serviceId"})
    public ApiRes<List<GrayTrack>> listByService(@RequestParam(value="serviceId") String serviceId) {
        return ApiRes.builder().code("0").data(this.grayServerTrackModule.listGrayTracksEmptyInstanceByServiceId(serviceId)).build();
    }

    @GetMapping(value={"/pageByService"})
    public ResponseEntity<ApiRes<List<GrayTrack>>> pageByService(@RequestParam(value="serviceId") String serviceId, @ApiParam @PageableDefault(sort={"id"}, direction=Sort.Direction.DESC) Pageable pageable) {
        Page<GrayTrack> page = this.grayServerTrackModule.listGrayTracks(serviceId, pageable);
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(page);
        return new ResponseEntity((Object)ApiRes.builder().code("0").data((Object)page.getContent()).build(), (MultiValueMap)headers, HttpStatus.OK);
    }

    @GetMapping(value={"/page"})
    public ResponseEntity<ApiRes<List<GrayTrack>>> page(@ApiParam @PageableDefault(sort={"id"}, direction=Sort.Direction.DESC) Pageable pageable) {
        Page<GrayTrack> page = this.grayServerTrackModule.listGrayTracks(pageable);
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(page);
        return new ResponseEntity((Object)ApiRes.builder().code("0").data((Object)page.getContent()).build(), (MultiValueMap)headers, HttpStatus.OK);
    }

    @RequestMapping(value={"{id}"}, method={RequestMethod.DELETE})
    public ApiRes<Void> delete(@PathVariable(value="id") Long id) {
        GrayTrack grayTrack = this.grayServerTrackModule.getGrayTrack(id);
        if (grayTrack != null) {
            if (!this.serviceManageModule.hasServiceAuthority(grayTrack.getServiceId())) {
                return ApiResHelper.notAuthority();
            }
            this.grayServerTrackModule.deleteGrayTrack(id);
        }
        return ApiRes.builder().code("0").build();
    }

    @RequestMapping(value={"/"}, method={RequestMethod.POST})
    public ApiRes<GrayTrack> save(@RequestBody GrayTrack track) {
        if (!this.serviceManageModule.hasServiceAuthority(track.getServiceId())) {
            return ApiResHelper.notAuthority();
        }
        track.setOperator(this.userModule.getCurrentUserId());
        track.setOperateTime(new Date());
        return ApiRes.builder().code("0").data((Object)this.grayServerTrackModule.saveGrayTrack(track)).build();
    }
}

