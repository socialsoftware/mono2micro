/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.JpaSpecificationExecutor
 *  org.springframework.stereotype.Repository
 */
package cn.springcloud.gray.server.dao.repository;

import cn.springcloud.gray.server.dao.model.GrayInstanceDO;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GrayInstanceRepository
extends JpaRepository<GrayInstanceDO, String>,
JpaSpecificationExecutor<GrayInstanceDO> {
    public List<GrayInstanceDO> findByServiceId(String var1);

    public List<GrayInstanceDO> findAllByGrayStatus(String var1);

    public List<GrayInstanceDO> findAllByGrayStatusAndInstanceStatus(String var1, String var2);

    public Page<GrayInstanceDO> findAllByServiceId(String var1, Pageable var2);

    public List<GrayInstanceDO> findAllByServiceIdAndInstanceStatusIn(String var1, String[] var2);

    public List<GrayInstanceDO> findAllByGrayStatusAndInstanceStatusIn(String var1, String[] var2);

    public List<GrayInstanceDO> findAllByGrayStatusAndInstanceStatusInOrGrayLock(String var1, String[] var2, int var3);

    public List<GrayInstanceDO> findAllByLastUpdateDateBeforeAndInstanceStatusIn(Date var1, String[] var2);
}

