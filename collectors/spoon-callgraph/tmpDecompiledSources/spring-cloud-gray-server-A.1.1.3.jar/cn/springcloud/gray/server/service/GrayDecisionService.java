/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Service
 */
package cn.springcloud.gray.server.service;

import cn.springcloud.gray.server.dao.mapper.GrayDecisionMapper;
import cn.springcloud.gray.server.dao.mapper.ModelMapper;
import cn.springcloud.gray.server.dao.model.GrayDecisionDO;
import cn.springcloud.gray.server.dao.repository.GrayDecisionRepository;
import cn.springcloud.gray.server.module.gray.domain.GrayDecision;
import cn.springcloud.gray.server.service.AbstraceCRUDService;
import cn.springcloud.gray.server.utils.PaginationUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class GrayDecisionService
extends AbstraceCRUDService<GrayDecision, GrayDecisionRepository, GrayDecisionDO, Long> {
    @Autowired
    private GrayDecisionRepository repository;
    @Autowired
    private GrayDecisionMapper grayDecisionMapper;

    @Override
    protected GrayDecisionRepository getRepository() {
        return this.repository;
    }

    @Override
    protected ModelMapper<GrayDecision, GrayDecisionDO> getModelMapper() {
        return this.grayDecisionMapper;
    }

    public List<GrayDecision> findByPolicyId(Long policyId) {
        return this.grayDecisionMapper.dos2models(this.repository.findByPolicyId(policyId));
    }

    public void deleteAllByPolicyId(Long policyId) {
        this.repository.deleteAllByPolicyId(policyId);
    }

    public Page<GrayDecision> listGrayDecisionsByPolicyId(Long policyId, Pageable pageable) {
        Page<GrayDecisionDO> entities = this.repository.findAllByPolicyId(policyId, pageable);
        return PaginationUtils.convert(pageable, entities, this.grayDecisionMapper);
    }
}

