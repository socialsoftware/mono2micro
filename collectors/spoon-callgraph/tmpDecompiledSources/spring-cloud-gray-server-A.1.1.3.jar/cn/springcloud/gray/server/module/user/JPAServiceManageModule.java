/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageImpl
 *  org.springframework.data.domain.Pageable
 *  org.springframework.transaction.annotation.Transactional
 */
package cn.springcloud.gray.server.module.user;

import cn.springcloud.gray.server.module.user.ServiceManageModule;
import cn.springcloud.gray.server.module.user.UserModule;
import cn.springcloud.gray.server.module.user.domain.ServiceOwner;
import cn.springcloud.gray.server.module.user.domain.ServiceOwnerQuery;
import cn.springcloud.gray.server.module.user.domain.UserServiceAuthority;
import cn.springcloud.gray.server.service.ServiceOwnerService;
import cn.springcloud.gray.server.service.UserServiceAuthorityService;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public class JPAServiceManageModule
implements ServiceManageModule {
    private UserModule userModule;
    private UserServiceAuthorityService userServiceAuthorityService;
    private ServiceOwnerService serviceOwnerService;

    public JPAServiceManageModule(UserModule userModule, UserServiceAuthorityService userServiceAuthorityService, ServiceOwnerService serviceOwnerService) {
        this.userModule = userModule;
        this.userServiceAuthorityService = userServiceAuthorityService;
        this.serviceOwnerService = serviceOwnerService;
    }

    @Override
    public boolean hasServiceAuthority(String serviceId, String userId) {
        return this.userServiceAuthorityService.findByServiceIdAndUserId(serviceId, userId) != null;
    }

    @Override
    public boolean isServiceOwner(String serviceId, String userId) {
        ServiceOwner serviceOwner = this.getServiceOwner(serviceId);
        return serviceOwner == null ? false : StringUtils.equals(serviceOwner.getUserId(), userId);
    }

    @Override
    public boolean hasServiceAuthority(String serviceId) {
        String userId = this.userModule.getCurrentUserId();
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        return this.hasServiceAuthority(serviceId, userId);
    }

    @Override
    public Page<UserServiceAuthority> listAllUserServiceAuthorities(Pageable pageable) {
        return this.userServiceAuthorityService.findAllModels(pageable);
    }

    @Override
    public Page<UserServiceAuthority> listServiceAuthorities(String serviceId, Pageable pageable) {
        return this.userServiceAuthorityService.listServiceAuthorities(serviceId, pageable);
    }

    @Override
    public Page<String> listAllUserServiceIds(Pageable pageable) {
        Page<UserServiceAuthority> entities = this.listAllUserServiceAuthorities(pageable);
        List serviceIds = entities.getContent().stream().map(UserServiceAuthority::getServiceId).collect(Collectors.toList());
        return new PageImpl(serviceIds, pageable, entities.getTotalElements());
    }

    @Override
    public Page<UserServiceAuthority> listAllServiceAuthorities(String userId, Pageable pageable) {
        return this.userServiceAuthorityService.listAllServiceAuthorities(userId, pageable);
    }

    @Override
    public Page<String> listAllServiceIds(String userId, Pageable pageable) {
        Page<UserServiceAuthority> entities = this.listAllServiceAuthorities(userId, pageable);
        List serviceIds = entities.getContent().stream().map(UserServiceAuthority::getServiceId).collect(Collectors.toList());
        return new PageImpl(serviceIds, pageable, entities.getTotalElements());
    }

    @Transactional
    @Override
    public ServiceOwner addServiceOwner(String serviceId) {
        return this.addServiceOwner(serviceId, this.userModule.getCurrentUserId());
    }

    @Override
    public ServiceOwner insertServiceOwner(String serviceId) {
        ServiceOwner serviceOwner = new ServiceOwner();
        serviceOwner.setServiceId(serviceId);
        return this.serviceOwnerService.saveModel(serviceOwner);
    }

    @Transactional
    @Override
    public ServiceOwner addServiceOwner(String serviceId, String userId) {
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        ServiceOwner serviceOwner = new ServiceOwner();
        serviceOwner.setServiceId(serviceId);
        serviceOwner.setUserId(userId);
        this.serviceOwnerService.saveModel(serviceOwner);
        this.addServiceAuthority(serviceId, userId);
        return null;
    }

    @Transactional
    @Override
    public ServiceOwner transferServiceOwner(String serviceId, String userId) {
        ServiceOwner serviceOwner = new ServiceOwner();
        serviceOwner.setServiceId(serviceId);
        serviceOwner.setUserId(userId);
        serviceOwner.setOperator(this.userModule.getCurrentUserId());
        serviceOwner.setOperateTime(new Date());
        this.serviceOwnerService.saveModel(serviceOwner);
        if (!this.hasServiceAuthority(serviceId, userId)) {
            this.addServiceAuthority(serviceId, userId);
        }
        return serviceOwner;
    }

    @Override
    public ServiceOwner getServiceOwner(String serviceId) {
        return (ServiceOwner)this.serviceOwnerService.findOneModel(serviceId);
    }

    @Override
    public Page<ServiceOwner> queryServiceOwners(ServiceOwnerQuery query, Pageable pageable) {
        return this.serviceOwnerService.queryServiceOwners(query, pageable);
    }

    @Override
    public UserServiceAuthority getServiceAuthority(Long id) {
        return (UserServiceAuthority)this.userServiceAuthorityService.findOneModel(id);
    }

    @Transactional
    @Override
    public void deleteSericeManeges(String serviceId) {
        this.deleteServiceOwner(serviceId);
        this.deleteServiceAuthorities(serviceId);
    }

    @Override
    public void deleteServiceOwner(String serviceId) {
        this.serviceOwnerService.delete(serviceId);
    }

    @Override
    public void deleteServiceAuthorities(String serviceId) {
        this.userServiceAuthorityService.deleteServiceAuthorities(serviceId);
    }

    @Override
    public void deleteServiceAuthority(Long id) {
        this.userServiceAuthorityService.delete(id);
    }

    @Override
    public UserServiceAuthority addServiceAuthority(String serviceId, String userId) {
        UserServiceAuthority userServiceAuthority = new UserServiceAuthority();
        userServiceAuthority.setServiceId(serviceId);
        userServiceAuthority.setUserId(userId);
        userServiceAuthority.setOperator(this.userModule.getCurrentUserId());
        userServiceAuthority.setOperateTime(new Date());
        return this.userServiceAuthorityService.saveModel(userServiceAuthority);
    }
}

