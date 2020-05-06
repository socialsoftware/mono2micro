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

import cn.springcloud.gray.server.dao.mapper.GrayServiceMapper;
import cn.springcloud.gray.server.dao.mapper.ModelMapper;
import cn.springcloud.gray.server.dao.model.GrayServiceDO;
import cn.springcloud.gray.server.dao.repository.GrayServiceRepository;
import cn.springcloud.gray.server.module.gray.domain.GrayInstance;
import cn.springcloud.gray.server.module.gray.domain.GrayService;
import cn.springcloud.gray.server.service.AbstraceCRUDService;
import cn.springcloud.gray.server.service.GrayInstanceService;
import cn.springcloud.gray.server.utils.PaginationUtils;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class GrayServiceService
extends AbstraceCRUDService<GrayService, GrayServiceRepository, GrayServiceDO, String> {
    @Autowired
    private GrayServiceRepository repository;
    @Autowired
    private GrayInstanceService grayInstanceService;
    @Autowired
    private GrayServiceMapper grayServiceMapper;

    @Override
    protected GrayServiceRepository getRepository() {
        return this.repository;
    }

    @Override
    protected ModelMapper<GrayService, GrayServiceDO> getModelMapper() {
        return this.grayServiceMapper;
    }

    public void deleteById(String id) {
        this.delete(id);
        this.grayInstanceService.findByServiceId(id);
    }

    public void deleteReactById(String id) {
        this.delete(id);
        this.grayInstanceService.findByServiceId(id).forEach(entity -> this.grayInstanceService.deleteReactById(entity.getInstanceId()));
    }

    public Page<GrayService> listAllGrayServices(Pageable pageable) {
        Page entities = this.repository.findAll(pageable);
        return PaginationUtils.convert(pageable, entities, this.grayServiceMapper);
    }
}

