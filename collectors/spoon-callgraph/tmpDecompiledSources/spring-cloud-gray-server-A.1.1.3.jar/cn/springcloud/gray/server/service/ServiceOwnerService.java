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
import cn.springcloud.gray.server.dao.mapper.ServiceOwnerMapper;
import cn.springcloud.gray.server.dao.model.ServiceOwnerDO;
import cn.springcloud.gray.server.dao.repository.ServiceOwnerRepository;
import cn.springcloud.gray.server.module.user.domain.ServiceOwner;
import cn.springcloud.gray.server.module.user.domain.ServiceOwnerQuery;
import cn.springcloud.gray.server.service.AbstraceCRUDService;
import cn.springcloud.gray.server.utils.PaginationUtils;
import java.util.ArrayList;
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
public class ServiceOwnerService
extends AbstraceCRUDService<ServiceOwner, ServiceOwnerRepository, ServiceOwnerDO, String> {
    @Autowired
    private ServiceOwnerRepository repository;
    @Autowired
    private ServiceOwnerMapper serviceOwnerMapper;

    @Override
    protected ServiceOwnerRepository getRepository() {
        return this.repository;
    }

    @Override
    protected ModelMapper<ServiceOwner, ServiceOwnerDO> getModelMapper() {
        return this.serviceOwnerMapper;
    }

    public ServiceOwner findServiceOwner(String serviceId) {
        return (ServiceOwner)this.do2model(this.getRepository().findByServiceId(serviceId));
    }

    public Page<ServiceOwner> queryServiceOwners(final ServiceOwnerQuery serviceOwnerQuery, Pageable pageable) {
        Specification<ServiceOwnerDO> specification = new Specification<ServiceOwnerDO>(){

            public Predicate toPredicate(Root<ServiceOwnerDO> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                ArrayList<Predicate> predicates = new ArrayList<Predicate>();
                switch (serviceOwnerQuery.getQueryItem()) {
                    case 1: {
                        Predicate p1 = cb.isNotNull(root.get("userId"));
                        Predicate p2 = cb.notEqual(root.get("userId").as(String.class), "");
                        predicates.add(cb.and((Expression<Boolean>)p1, (Expression<Boolean>)p2));
                        break;
                    }
                    case 2: {
                        Predicate unbindP1 = cb.isNull(root.get("userId").as(String.class));
                        Predicate unbindP2 = cb.equal(root.get("userId").as(String.class), "");
                        predicates.add(cb.or((Expression<Boolean>)unbindP1, (Expression<Boolean>)unbindP2));
                    }
                }
                if (StringUtils.isNotEmpty(serviceOwnerQuery.getServiceId())) {
                    predicates.add(cb.equal(root.get("serviceId").as(String.class), serviceOwnerQuery.getServiceId()));
                }
                query.where(predicates.toArray(new Predicate[predicates.size()]));
                return query.getRestriction();
            }
        };
        Page doPage = this.repository.findAll((Specification)specification, pageable);
        return PaginationUtils.convert(pageable, doPage, this.getModelMapper());
    }

}

