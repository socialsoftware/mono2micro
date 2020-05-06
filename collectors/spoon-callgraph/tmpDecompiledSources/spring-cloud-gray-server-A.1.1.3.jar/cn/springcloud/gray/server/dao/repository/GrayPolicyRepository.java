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

import cn.springcloud.gray.server.dao.model.GrayPolicyDO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrayPolicyRepository
extends JpaRepository<GrayPolicyDO, Long> {
    public List<GrayPolicyDO> findByInstanceId(String var1);

    public Page<GrayPolicyDO> findAllByInstanceId(String var1, Pageable var2);
}

