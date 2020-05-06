/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Repository
 */
package cn.springcloud.gray.server.dao.repository;

import cn.springcloud.gray.server.dao.model.GrayDecisionDO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrayDecisionRepository
extends JpaRepository<GrayDecisionDO, Long> {
    public List<GrayDecisionDO> findByPolicyId(Long var1);

    public void deleteAllByPolicyId(Long var1);

    public Page<GrayDecisionDO> findAllByPolicyId(Long var1, Pageable var2);
}

