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
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package cn.springcloud.gray.server.resources.rest;

import cn.springcloud.gray.api.ApiRes;
import cn.springcloud.gray.server.module.audit.OperateAuditModule;
import cn.springcloud.gray.server.module.audit.domain.OperateQuery;
import cn.springcloud.gray.server.module.audit.domain.OperateRecord;
import cn.springcloud.gray.server.resources.domain.fo.OperateQueryFO;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(value={"/gray/operate/record"})
public class OperateRecordResource {
    @Autowired
    private OperateAuditModule operateAuditModule;

    @GetMapping(value={"/page"})
    public ResponseEntity<ApiRes<List<OperateRecord>>> list(@Validated OperateQueryFO fo, @ApiParam @PageableDefault(sort={"operateTime"}, direction=Sort.Direction.DESC) Pageable pageable) {
        OperateQuery query = fo.toOperateQuery();
        Page<OperateRecord> operateRecordPage = this.operateAuditModule.queryRecords(query, pageable);
        HttpHeaders headers = PaginationUtils.generatePaginationHttpHeaders(operateRecordPage);
        ApiRes data = ApiRes.builder().code("0").data((Object)operateRecordPage.getContent()).build();
        return new ResponseEntity((Object)data, (MultiValueMap)headers, HttpStatus.OK);
    }
}

