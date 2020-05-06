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

import cn.springcloud.gray.server.dao.mapper.GrayPolicyMapper;
import cn.springcloud.gray.server.dao.mapper.ModelMapper;
import cn.springcloud.gray.server.dao.model.GrayPolicyDO;
import cn.springcloud.gray.server.dao.repository.GrayPolicyRepository;
import cn.springcloud.gray.server.module.gray.domain.GrayPolicy;
import cn.springcloud.gray.server.service.AbstraceCRUDService;
import cn.springcloud.gray.server.service.GrayDecisionService;
import cn.springcloud.gray.server.utils.PaginationUtils;
import java.util.List;
import java.util.function.Consumer;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class GrayPolicyService
extends AbstraceCRUDService<GrayPolicy, GrayPolicyRepository, GrayPolicyDO, Long> {
    @Autowired
    private GrayPolicyRepository repository;
    @Autowired
    private GrayDecisionService grayDecisionService;
    @Autowired
    private GrayPolicyMapper grayPolicyMapper;

    @Override
    protected GrayPolicyRepository getRepository() {
        return this.repository;
    }

    @Override
    protected ModelMapper<GrayPolicy, GrayPolicyDO> getModelMapper() {
        return this.grayPolicyMapper;
    }

    public List<GrayPolicy> findByInstanceId(String instanceId) {
        return this.grayPolicyMapper.dos2models(this.repository.findByInstanceId(instanceId));
    }

    public void deleteByInstanceId(String instanceId) {
        this.findByInstanceId(instanceId).forEach(entity -> {
            this.delete(entity.getId());
            this.grayDecisionService.deleteAllByPolicyId(entity.getId());
        });
    }

    @Transactional
    public void deleteReactById(Long id) {
        this.delete(id);
        this.grayDecisionService.deleteAllByPolicyId(id);
    }

    public Page<GrayPolicy> listGrayPoliciesByInstanceId(String instanceId, Pageable pageable) {
        Page<GrayPolicyDO> entities = this.repository.findAllByInstanceId(instanceId, pageable);
        return PaginationUtils.convert(pageable, entities, this.grayPolicyMapper);
    }
}

