/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModel
 */
package cn.springcloud.gray.server.resources.domain.fo;

import cn.springcloud.gray.server.module.user.domain.UserInfo;
import io.swagger.annotations.ApiModel;
import java.util.Arrays;

@ApiModel
public class UserRegisterFO {
    private String name;
    private String account;
    private String password;
    private String[] roles;
    private int status = 1;

    public UserInfo toUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setAccount(this.getAccount());
        userInfo.setName(this.getName());
        userInfo.setRoles(this.getRoles());
        userInfo.setStatus(this.getStatus());
        return userInfo;
    }

    public String getName() {
        return this.name;
    }

    public String getAccount() {
        return this.account;
    }

    public String getPassword() {
        return this.password;
    }

    public String[] getRoles() {
        return this.roles;
    }

    public int getStatus() {
        return this.status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UserRegisterFO)) {
            return false;
        }
        UserRegisterFO other = (UserRegisterFO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$name = this.getName();
        String other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        String this$account = this.getAccount();
        String other$account = other.getAccount();
        if (this$account == null ? other$account != null : !this$account.equals(other$account)) {
            return false;
        }
        String this$password = this.getPassword();
        String other$password = other.getPassword();
        if (this$password == null ? other$password != null : !this$password.equals(other$password)) {
            return false;
        }
        if (!Arrays.deepEquals(this.getRoles(), other.getRoles())) {
            return false;
        }
        return this.getStatus() == other.getStatus();
    }

    protected boolean canEqual(Object other) {
        return other instanceof UserRegisterFO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        String $account = this.getAccount();
        result = result * 59 + ($account == null ? 43 : $account.hashCode());
        String $password = this.getPassword();
        result = result * 59 + ($password == null ? 43 : $password.hashCode());
        result = result * 59 + Arrays.deepHashCode(this.getRoles());
        result = result * 59 + this.getStatus();
        return result;
    }

    public String toString() {
        return "UserRegisterFO(name=" + this.getName() + ", account=" + this.getAccount() + ", password=" + this.getPassword() + ", roles=" + Arrays.deepToString(this.getRoles()) + ", status=" + this.getStatus() + ")";
    }
}

