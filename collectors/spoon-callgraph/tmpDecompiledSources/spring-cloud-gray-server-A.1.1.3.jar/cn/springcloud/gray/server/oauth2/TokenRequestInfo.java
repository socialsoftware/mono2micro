/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.security.core.userdetails.UserDetails
 */
package cn.springcloud.gray.server.oauth2;

import java.util.Map;
import org.springframework.security.core.userdetails.UserDetails;

public class TokenRequestInfo {
    private UserDetails userDetails;
    private Map<String, String> extensionProperties;

    public static TokenRequestInfoBuilder builder() {
        return new TokenRequestInfoBuilder();
    }

    public UserDetails getUserDetails() {
        return this.userDetails;
    }

    public Map<String, String> getExtensionProperties() {
        return this.extensionProperties;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public void setExtensionProperties(Map<String, String> extensionProperties) {
        this.extensionProperties = extensionProperties;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TokenRequestInfo)) {
            return false;
        }
        TokenRequestInfo other = (TokenRequestInfo)o;
        if (!other.canEqual(this)) {
            return false;
        }
        UserDetails this$userDetails = this.getUserDetails();
        UserDetails other$userDetails = other.getUserDetails();
        if (this$userDetails == null ? other$userDetails != null : !this$userDetails.equals((Object)other$userDetails)) {
            return false;
        }
        Map<String, String> this$extensionProperties = this.getExtensionProperties();
        Map<String, String> other$extensionProperties = other.getExtensionProperties();
        return !(this$extensionProperties == null ? other$extensionProperties != null : !((Object)this$extensionProperties).equals(other$extensionProperties));
    }

    protected boolean canEqual(Object other) {
        return other instanceof TokenRequestInfo;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        UserDetails $userDetails = this.getUserDetails();
        result = result * 59 + ($userDetails == null ? 43 : $userDetails.hashCode());
        Map<String, String> $extensionProperties = this.getExtensionProperties();
        result = result * 59 + ($extensionProperties == null ? 43 : ((Object)$extensionProperties).hashCode());
        return result;
    }

    public String toString() {
        return "TokenRequestInfo(userDetails=" + (Object)this.getUserDetails() + ", extensionProperties=" + this.getExtensionProperties() + ")";
    }

    public TokenRequestInfo(UserDetails userDetails, Map<String, String> extensionProperties) {
        this.userDetails = userDetails;
        this.extensionProperties = extensionProperties;
    }

    public static class TokenRequestInfoBuilder {
        private UserDetails userDetails;
        private Map<String, String> extensionProperties;

        TokenRequestInfoBuilder() {
        }

        public TokenRequestInfoBuilder userDetails(UserDetails userDetails) {
            this.userDetails = userDetails;
            return this;
        }

        public TokenRequestInfoBuilder extensionProperties(Map<String, String> extensionProperties) {
            this.extensionProperties = extensionProperties;
            return this;
        }

        public TokenRequestInfo build() {
            return new TokenRequestInfo(this.userDetails, this.extensionProperties);
        }

        public String toString() {
            return "TokenRequestInfo.TokenRequestInfoBuilder(userDetails=" + (Object)this.userDetails + ", extensionProperties=" + this.extensionProperties + ")";
        }
    }

}

