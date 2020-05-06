/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 */
package cn.springcloud.gray.server.module.user;

import cn.springcloud.gray.server.module.user.UserModule;
import cn.springcloud.gray.server.module.user.domain.UserInfo;
import cn.springcloud.gray.server.module.user.domain.UserQuery;
import cn.springcloud.gray.server.oauth2.Oauth2Service;
import cn.springcloud.gray.server.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class JPAUserModule
implements UserModule {
    private Oauth2Service oauth2Service;
    private UserService userService;

    public JPAUserModule(UserService userService, Oauth2Service oauth2Service) {
        this.oauth2Service = oauth2Service;
        this.userService = userService;
    }

    @Override
    public UserInfo login(String account, String password) {
        return this.userService.login(account, password);
    }

    @Override
    public UserInfo register(UserInfo userInfo, String password) {
        return this.userService.register(userInfo, password);
    }

    @Override
    public void disableUser(String userId) {
        this.userService.updateUserStatus(userId, 0);
    }

    @Override
    public void enableUser(String userId) {
        this.userService.updateUserStatus(userId, 1);
    }

    @Override
    public void resetPassword(String userId, String password) {
        this.userService.resetPassword(userId, password);
    }

    @Override
    public boolean resetPassword(String userId, String oldPassword, String newPassword) {
        return this.userService.resetPassword(userId, oldPassword, newPassword);
    }

    @Override
    public UserInfo getUserInfo(String userId) {
        return (UserInfo)this.userService.findOneModel(userId);
    }

    @Override
    public UserInfo getCurrentUserInfo() {
        return this.getUserInfo(this.getCurrentUserId());
    }

    @Override
    public Page<UserInfo> query(UserQuery userQuery, Pageable pageable) {
        return this.userService.query(userQuery, pageable);
    }

    @Override
    public String getCurrentUserId() {
        return this.oauth2Service.getUserPrincipal();
    }

    @Override
    public UserInfo updateUserInfo(UserInfo userInfo) {
        return this.userService.updateUserInfo(userInfo);
    }
}

