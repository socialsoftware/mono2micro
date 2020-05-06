/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.JpaSpecificationExecutor
 *  org.springframework.stereotype.Repository
 */
package cn.springcloud.gray.server.dao.repository;

import cn.springcloud.gray.server.dao.model.UserDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository
extends JpaRepository<UserDO, String>,
JpaSpecificationExecutor<UserDO> {
    public UserDO findByAccount(String var1);
}

