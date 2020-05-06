/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.stereotype.Repository
 */
package cn.springcloud.gray.server.dao.repository;

import cn.springcloud.gray.server.dao.model.GrayTrackDO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GrayTrackRepository
extends JpaRepository<GrayTrackDO, Long> {
    public Page<GrayTrackDO> findAllByInstanceId(String var1, Pageable var2);

    public List<GrayTrackDO> findAllByInstanceId(String var1);

    @Query(value="SELECT do FROM GrayTrackDO do WHERE serviceId = ?1 AND (instanceId = null or instanceId='') ")
    public List<GrayTrackDO> findAllByServiceIdAndInstanceIdIsEmpty(String var1);

    @Query(value="SELECT do FROM GrayTrackDO do WHERE serviceId = ?1 AND (instanceId = null or instanceId='') ")
    public Page<GrayTrackDO> findAllByServiceIdAndInstanceIdIsEmpty(String var1, Pageable var2);

    public Page<GrayTrackDO> findAllByServiceId(String var1, Pageable var2);
}

