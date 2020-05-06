/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.module.user.domain;

import java.util.Arrays;
import java.util.Date;
import org.apache.commons.lang3.ArrayUtils;

public class UserInfo {
    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_EDITOR = "editor";
    private String userId;
    private String name;
    private String account;
    private String[] roles;
    private int status = 1;
    private String operator;
    private Date operateTime;

    public boolean isAdmin() {
        return ArrayUtils.contains(this.roles, ROLE_ADMIN);
    }

    public String getUserId() {
        return this.userId;
    }

    public String getName() {
        return this.name;
    }

    public String getAccount() {
        return this.account;
    }

    public String[] getRoles() {
        return this.roles;
    }

    public int getStatus() {
        return this.status;
    }

    public String getOperator() {
        return this.operator;
    }

    public Date getOperateTime() {
        return this.operateTime;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UserInfo)) {
            return false;
        }
        UserInfo other = (UserInfo)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$userId = this.getUserId();
        String other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) {
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
        if (!Arrays.deepEquals(this.getRoles(), other.getRoles())) {
            return false;
        }
        if (this.getStatus() != other.getStatus()) {
            return false;
        }
        String this$operator = this.getOperator();
        String other$operator = other.getOperator();
        if (this$operator == null ? other$operator != null : !this$operator.equals(other$operator)) {
            return false;
        }
        Date this$operateTime = this.getOperateTime();
        Date other$operateTime = other.getOperateTime();
        return !(this$operateTime == null ? other$operateTime != null : !((Object)this$operateTime).equals(other$operateTime));
    }

    protected boolean canEqual(Object other) {
        return other instanceof UserInfo;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $userId = this.getUserId();
        result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
        String $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        String $account = this.getAccount();
        result = result * 59 + ($account == null ? 43 : $account.hashCode());
        result = result * 59 + Arrays.deepHashCode(this.getRoles());
        result = result * 59 + this.getStatus();
        String $operator = this.getOperator();
        result = result * 59 + ($operator == null ? 43 : $operator.hashCode());
        Date $operateTime = this.getOperateTime();
        result = result * 59 + ($operateTime == null ? 43 : ((Object)$operateTime).hashCode());
        return result;
    }

    public String toString() {
        return "UserInfo(userId=" + this.getUserId() + ", name=" + this.getName() + ", account=" + this.getAccount() + ", roles=" + Arrays.deepToString(this.getRoles()) + ", status=" + this.getStatus() + ", operator=" + this.getOperator() + ", operateTime=" + this.getOperateTime() + ")";
    }
}

