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

import cn.springcloud.gray.server.dao.model.UserServiceAuthorityDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserServiceAuthorityRepository
extends JpaRepository<UserServiceAuthorityDO, Long> {
    public UserServiceAuthorityDO findByServiceIdAndUserId(String var1, String var2);

    public Page<UserServiceAuthorityDO> findAllByUserId(String var1, Pageable var2);

    public int deleteAllByServiceId(String var1);

    public Page<UserServiceAuthorityDO> findAllByServiceId(String var1, Pageable var2);
}

