/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModel
 */
package cn.springcloud.gray.server.resources.domain.fo;

import io.swagger.annotations.ApiModel;

@ApiModel
public class UpdatePasswordFO {
    private String oldPassword;
    private String newPassword;

    public String getOldPassword() {
        return this.oldPassword;
    }

    public String getNewPassword() {
        return this.newPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UpdatePasswordFO)) {
            return false;
        }
        UpdatePasswordFO other = (UpdatePasswordFO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$oldPassword = this.getOldPassword();
        String other$oldPassword = other.getOldPassword();
        if (this$oldPassword == null ? other$oldPassword != null : !this$oldPassword.equals(other$oldPassword)) {
            return false;
        }
        String this$newPassword = this.getNewPassword();
        String other$newPassword = other.getNewPassword();
        return !(this$newPassword == null ? other$newPassword != null : !this$newPassword.equals(other$newPassword));
    }

    protected boolean canEqual(Object other) {
        return other instanceof UpdatePasswordFO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $oldPassword = this.getOldPassword();
        result = result * 59 + ($oldPassword == null ? 43 : $oldPassword.hashCode());
        String $newPassword = this.getNewPassword();
        result = result * 59 + ($newPassword == null ? 43 : $newPassword.hashCode());
        return result;
    }

    public String toString() {
        return "UpdatePasswordFO(oldPassword=" + this.getOldPassword() + ", newPassword=" + this.getNewPassword() + ")";
    }
}

