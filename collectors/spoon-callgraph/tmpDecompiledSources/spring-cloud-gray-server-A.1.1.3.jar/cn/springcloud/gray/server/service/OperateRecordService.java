/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.domain.Specification
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Service
 */
package cn.springcloud.gray.server.service;

import cn.springcloud.gray.server.dao.mapper.ModelMapper;
import cn.springcloud.gray.server.dao.mapper.OperateRecordMapper;
import cn.springcloud.gray.server.dao.model.OperateRecordDO;
import cn.springcloud.gray.server.dao.repository.OperateRecordRepository;
import cn.springcloud.gray.server.module.audit.domain.OperateQuery;
import cn.springcloud.gray.server.module.audit.domain.OperateRecord;
import cn.springcloud.gray.server.service.AbstraceCRUDService;
import cn.springcloud.gray.server.utils.PaginationUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class OperateRecordService
extends AbstraceCRUDService<OperateRecord, OperateRecordRepository, OperateRecordDO, Long> {
    @Autowired
    private OperateRecordRepository repository;
    @Autowired
    private OperateRecordMapper userMapper;

    @Override
    protected OperateRecordRepository getRepository() {
        return this.repository;
    }

    @Override
    protected ModelMapper<OperateRecord, OperateRecordDO> getModelMapper() {
        return this.userMapper;
    }

    public Page<OperateRecord> query(final OperateQuery operateQuery, Pageable pageable) {
        Specification<OperateRecordDO> specification = new Specification<OperateRecordDO>(){

            public Predicate toPredicate(Root<OperateRecordDO> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                ArrayList<Predicate> predicates = new ArrayList<Predicate>();
                if (StringUtils.isNotEmpty(operateQuery.getApiResCode())) {
                    predicates.add(cb.equal(root.get("apiResCode").as(String.class), operateQuery.getApiResCode()));
                }
                if (StringUtils.isNotEmpty(operateQuery.getOperator())) {
                    predicates.add(cb.equal(root.get("operator").as(String.class), operateQuery.getOperator()));
                }
                if (StringUtils.isNotEmpty(operateQuery.getIp())) {
                    predicates.add(cb.equal(root.get("ip").as(String.class), operateQuery.getIp()));
                }
                if (!Objects.isNull(operateQuery.getOperateState())) {
                    predicates.add(cb.equal(root.get("operateState").as(Integer.class), operateQuery.getOperateState()));
                }
                if (StringUtils.isNotEmpty(operateQuery.getUri())) {
                    predicates.add(cb.like(root.get("uri").as(String.class), operateQuery.getUri() + "%"));
                }
                if (StringUtils.isNotEmpty(operateQuery.getHandler())) {
                    predicates.add(cb.like(root.get("handler").as(String.class), operateQuery.getHandler() + "%"));
                }
                if (!Objects.isNull(operateQuery.getOperateStartTime())) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("operateTime").as(Date.class), operateQuery.getOperateStartTime()));
                }
                if (!Objects.isNull(operateQuery.getOperateEndTime())) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("operateTime").as(Date.class), operateQuery.getOperateEndTime()));
                }
                query.where(predicates.toArray(new Predicate[predicates.size()]));
                return query.getRestriction();
            }
        };
        Page doPage = this.repository.findAll((Specification)specification, pageable);
        return PaginationUtils.convert(pageable, doPage, this.getModelMapper());
    }

}

