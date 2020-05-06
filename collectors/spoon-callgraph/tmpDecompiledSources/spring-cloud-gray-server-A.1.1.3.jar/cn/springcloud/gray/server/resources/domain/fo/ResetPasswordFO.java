/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModel
 */
package cn.springcloud.gray.server.resources.domain.fo;

import io.swagger.annotations.ApiModel;

@ApiModel
public class ResetPasswordFO {
    private String userId;
    private String password;

    public String getUserId() {
        return this.userId;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ResetPasswordFO)) {
            return false;
        }
        ResetPasswordFO other = (ResetPasswordFO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$userId = this.getUserId();
        String other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) {
            return false;
        }
        String this$password = this.getPassword();
        String other$password = other.getPassword();
        return !(this$password == null ? other$password != null : !this$password.equals(other$password));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ResetPasswordFO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $userId = this.getUserId();
        result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
        String $password = this.getPassword();
        result = result * 59 + ($password == null ? 43 : $password.hashCode());
        return result;
    }

    public String toString() {
        return "ResetPasswordFO(userId=" + this.getUserId() + ", password=" + this.getPassword() + ")";
    }
}

