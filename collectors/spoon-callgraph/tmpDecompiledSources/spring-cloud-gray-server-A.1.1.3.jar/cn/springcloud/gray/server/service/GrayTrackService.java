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

import cn.springcloud.gray.server.dao.mapper.GrayTrackMapper;
import cn.springcloud.gray.server.dao.mapper.ModelMapper;
import cn.springcloud.gray.server.dao.model.GrayTrackDO;
import cn.springcloud.gray.server.dao.repository.GrayTrackRepository;
import cn.springcloud.gray.server.module.gray.domain.GrayTrack;
import cn.springcloud.gray.server.service.AbstraceCRUDService;
import cn.springcloud.gray.server.utils.PaginationUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class GrayTrackService
extends AbstraceCRUDService<GrayTrack, GrayTrackRepository, GrayTrackDO, Long> {
    @Autowired
    private GrayTrackRepository repository;
    @Autowired
    private GrayTrackMapper grayTrackMapper;

    @Override
    protected GrayTrackRepository getRepository() {
        return this.repository;
    }

    protected GrayTrackMapper getModelMapper() {
        return this.grayTrackMapper;
    }

    public List<GrayTrack> listGrayTracksEmptyInstanceByServiceId(String serviceId) {
        return this.dos2models(this.repository.findAllByServiceIdAndInstanceIdIsEmpty(serviceId));
    }

    public List<GrayTrack> listGrayTracksByInstanceId(String instanceId) {
        return this.dos2models(this.repository.findAllByInstanceId(instanceId));
    }

    public Page<GrayTrack> listGrayTracksEmptyInstanceByServiceId(String serviceId, Pageable pageable) {
        Page<GrayTrackDO> page = this.repository.findAllByServiceIdAndInstanceIdIsEmpty(serviceId, pageable);
        return PaginationUtils.convert(pageable, page, this.getModelMapper());
    }

    public Page<GrayTrack> listGrayTracksByInstanceId(String instanceId, Pageable pageable) {
        Page<GrayTrackDO> page = this.repository.findAllByInstanceId(instanceId, pageable);
        return PaginationUtils.convert(pageable, page, this.getModelMapper());
    }

    public Page<GrayTrack> listGrayTracks(String serviceId, Pageable pageable) {
        Page<GrayTrackDO> page = this.repository.findAllByServiceId(serviceId, pageable);
        return PaginationUtils.convert(pageable, page, this.getModelMapper());
    }

    public Page<GrayTrack> listGrayTracks(Pageable pageable) {
        Page page = this.repository.findAll(pageable);
        return PaginationUtils.convert(pageable, page, this.getModelMapper());
    }
}

