/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 */
package cn.springcloud.gray.server.module.user;

import cn.springcloud.gray.server.module.user.domain.UserInfo;
import cn.springcloud.gray.server.module.user.domain.UserQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserModule {
    public UserInfo login(String var1, String var2);

    public UserInfo register(UserInfo var1, String var2);

    public void disableUser(String var1);

    public void enableUser(String var1);

    public void resetPassword(String var1, String var2);

    public boolean resetPassword(String var1, String var2, String var3);

    public UserInfo getUserInfo(String var1);

    public UserInfo getCurrentUserInfo();

    public Page<UserInfo> query(UserQuery var1, Pageable var2);

    public String getCurrentUserId();

    public UserInfo updateUserInfo(UserInfo var1);
}

