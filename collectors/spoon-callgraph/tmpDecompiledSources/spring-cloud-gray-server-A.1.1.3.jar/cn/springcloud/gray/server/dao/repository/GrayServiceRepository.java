/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Repository
 */
package cn.springcloud.gray.server.dao.repository;

import cn.springcloud.gray.server.dao.model.GrayServiceDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrayServiceRepository
extends JpaRepository<GrayServiceDO, String> {
}

