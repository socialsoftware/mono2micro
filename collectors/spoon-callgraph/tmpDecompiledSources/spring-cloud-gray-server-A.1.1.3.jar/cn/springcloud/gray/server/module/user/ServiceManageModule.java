/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 */
package cn.springcloud.gray.server.module.user;

import cn.springcloud.gray.server.module.user.domain.ServiceOwner;
import cn.springcloud.gray.server.module.user.domain.ServiceOwnerQuery;
import cn.springcloud.gray.server.module.user.domain.UserServiceAuthority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceManageModule {
    public boolean hasServiceAuthority(String var1, String var2);

    public boolean isServiceOwner(String var1, String var2);

    public boolean hasServiceAuthority(String var1);

    public Page<UserServiceAuthority> listAllUserServiceAuthorities(Pageable var1);

    public Page<UserServiceAuthority> listServiceAuthorities(String var1, Pageable var2);

    public Page<String> listAllUserServiceIds(Pageable var1);

    public Page<UserServiceAuthority> listAllServiceAuthorities(String var1, Pageable var2);

    public Page<String> listAllServiceIds(String var1, Pageable var2);

    public ServiceOwner addServiceOwner(String var1);

    public ServiceOwner insertServiceOwner(String var1);

    public ServiceOwner addServiceOwner(String var1, String var2);

    public ServiceOwner transferServiceOwner(String var1, String var2);

    public ServiceOwner getServiceOwner(String var1);

    public Page<ServiceOwner> queryServiceOwners(ServiceOwnerQuery var1, Pageable var2);

    public UserServiceAuthority getServiceAuthority(Long var1);

    public void deleteSericeManeges(String var1);

    public void deleteServiceOwner(String var1);

    public void deleteServiceAuthorities(String var1);

    public void deleteServiceAuthority(Long var1);

    public UserServiceAuthority addServiceAuthority(String var1, String var2);
}

