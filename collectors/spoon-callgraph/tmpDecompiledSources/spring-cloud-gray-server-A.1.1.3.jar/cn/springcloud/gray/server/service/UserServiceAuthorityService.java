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

import cn.springcloud.gray.server.dao.mapper.ModelMapper;
import cn.springcloud.gray.server.dao.mapper.UserServiceAuthorityMapper;
import cn.springcloud.gray.server.dao.model.UserServiceAuthorityDO;
import cn.springcloud.gray.server.dao.repository.UserServiceAuthorityRepository;
import cn.springcloud.gray.server.module.user.domain.UserServiceAuthority;
import cn.springcloud.gray.server.service.AbstraceCRUDService;
import cn.springcloud.gray.server.utils.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class UserServiceAuthorityService
extends AbstraceCRUDService<UserServiceAuthority, UserServiceAuthorityRepository, UserServiceAuthorityDO, Long> {
    @Autowired
    private UserServiceAuthorityRepository repository;
    @Autowired
    private UserServiceAuthorityMapper mapper;

    @Override
    protected UserServiceAuthorityRepository getRepository() {
        return this.repository;
    }

    @Override
    protected ModelMapper<UserServiceAuthority, UserServiceAuthorityDO> getModelMapper() {
        return this.mapper;
    }

    public UserServiceAuthority findByServiceIdAndUserId(String serviceId, String userId) {
        return (UserServiceAuthority)this.do2model(this.repository.findByServiceIdAndUserId(serviceId, userId));
    }

    public Page<UserServiceAuthority> listAllServiceAuthorities(String userId, Pageable pageable) {
        return PaginationUtils.convert(pageable, this.repository.findAllByUserId(userId, pageable), this.getModelMapper());
    }

    public void deleteServiceAuthorities(String serviceId) {
        this.getRepository().deleteAllByServiceId(serviceId);
    }

    public Page<UserServiceAuthority> listServiceAuthorities(String serviceId, Pageable pageable) {
        return PaginationUtils.convert(pageable, this.getRepository().findAllByServiceId(serviceId, pageable), this.getModelMapper());
    }
}

