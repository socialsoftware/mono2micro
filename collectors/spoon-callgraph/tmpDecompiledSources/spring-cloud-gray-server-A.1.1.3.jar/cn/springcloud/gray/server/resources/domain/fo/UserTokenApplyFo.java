/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  io.swagger.annotations.ApiModel
 *  io.swagger.annotations.ApiModelProperty
 *  javax.validation.constraints.NotNull
 *  org.springframework.security.core.authority.SimpleGrantedAuthority
 *  org.springframework.security.core.userdetails.User
 *  org.springframework.security.core.userdetails.UserDetails
 */
package cn.springcloud.gray.server.resources.domain.fo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ApiModel(value="\u7533\u8bf7oauth2 token\u7684\u57fa\u672c\u4fe1\u606f")
public class UserTokenApplyFo {
    private static final String[] DEFAULT_USER_ROLES = new String[]{"ROLE_USER"};
    @ApiModelProperty(value="\u7528\u6237\u540d")
    @NotNull
    private String username;
    @ApiModelProperty(value="\u7528\u6237\u89d2\u8272", allowableValues="ROLE_USER")
    private String[] userRoles = DEFAULT_USER_ROLES;
    @ApiModelProperty(value="\u6269\u5c55\u4fe1\u606f")
    private Map<String, String> extensions;

    public UserDetails createUserdetails() {
        ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>(this.getUserRoles().length);
        for (String role : this.getUserRoles()) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return new User(this.getUsername(), "", authorities);
    }

    public String getUsername() {
        return this.username;
    }

    public String[] getUserRoles() {
        return this.userRoles;
    }

    public Map<String, String> getExtensions() {
        return this.extensions;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserRoles(String[] userRoles) {
        this.userRoles = userRoles;
    }

    public void setExtensions(Map<String, String> extensions) {
        this.extensions = extensions;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UserTokenApplyFo)) {
            return false;
        }
        UserTokenApplyFo other = (UserTokenApplyFo)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$username = this.getUsername();
        String other$username = other.getUsername();
        if (this$username == null ? other$username != null : !this$username.equals(other$username)) {
            return false;
        }
        if (!Arrays.deepEquals(this.getUserRoles(), other.getUserRoles())) {
            return false;
        }
        Map<String, String> this$extensions = this.getExtensions();
        Map<String, String> other$extensions = other.getExtensions();
        return !(this$extensions == null ? other$extensions != null : !((Object)this$extensions).equals(other$extensions));
    }

    protected boolean canEqual(Object other) {
        return other instanceof UserTokenApplyFo;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $username = this.getUsername();
        result = result * 59 + ($username == null ? 43 : $username.hashCode());
        result = result * 59 + Arrays.deepHashCode(this.getUserRoles());
        Map<String, String> $extensions = this.getExtensions();
        result = result * 59 + ($extensions == null ? 43 : ((Object)$extensions).hashCode());
        return result;
    }

    public String toString() {
        return "UserTokenApplyFo(username=" + this.getUsername() + ", userRoles=" + Arrays.deepToString(this.getUserRoles()) + ", extensions=" + this.getExtensions() + ")";
    }
}

