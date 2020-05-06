/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.server.oauth2;

import cn.springcloud.gray.server.oauth2.TokenRequestInfo;
import java.util.Map;

public class Oauth2TokenRequest {
    private String clientId;
    private Map<String, String> parameters;
    private TokenRequestInfo tokenRequestInfo;

    public static Oauth2TokenRequestBuilder builder() {
        return new Oauth2TokenRequestBuilder();
    }

    public String getClientId() {
        return this.clientId;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public TokenRequestInfo getTokenRequestInfo() {
        return this.tokenRequestInfo;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setTokenRequestInfo(TokenRequestInfo tokenRequestInfo) {
        this.tokenRequestInfo = tokenRequestInfo;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Oauth2TokenRequest)) {
            return false;
        }
        Oauth2TokenRequest other = (Oauth2TokenRequest)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$clientId = this.getClientId();
        String other$clientId = other.getClientId();
        if (this$clientId == null ? other$clientId != null : !this$clientId.equals(other$clientId)) {
            return false;
        }
        Map<String, String> this$parameters = this.getParameters();
        Map<String, String> other$parameters = other.getParameters();
        if (this$parameters == null ? other$parameters != null : !((Object)this$parameters).equals(other$parameters)) {
            return false;
        }
        TokenRequestInfo this$tokenRequestInfo = this.getTokenRequestInfo();
        TokenRequestInfo other$tokenRequestInfo = other.getTokenRequestInfo();
        return !(this$tokenRequestInfo == null ? other$tokenRequestInfo != null : !((Object)this$tokenRequestInfo).equals(other$tokenRequestInfo));
    }

    protected boolean canEqual(Object other) {
        return other instanceof Oauth2TokenRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $clientId = this.getClientId();
        result = result * 59 + ($clientId == null ? 43 : $clientId.hashCode());
        Map<String, String> $parameters = this.getParameters();
        result = result * 59 + ($parameters == null ? 43 : ((Object)$parameters).hashCode());
        TokenRequestInfo $tokenRequestInfo = this.getTokenRequestInfo();
        result = result * 59 + ($tokenRequestInfo == null ? 43 : ((Object)$tokenRequestInfo).hashCode());
        return result;
    }

    public String toString() {
        return "Oauth2TokenRequest(clientId=" + this.getClientId() + ", parameters=" + this.getParameters() + ", tokenRequestInfo=" + this.getTokenRequestInfo() + ")";
    }

    public Oauth2TokenRequest(String clientId, Map<String, String> parameters, TokenRequestInfo tokenRequestInfo) {
        this.clientId = clientId;
        this.parameters = parameters;
        this.tokenRequestInfo = tokenRequestInfo;
    }

    public static class Oauth2TokenRequestBuilder {
        private String clientId;
        private Map<String, String> parameters;
        private TokenRequestInfo tokenRequestInfo;

        Oauth2TokenRequestBuilder() {
        }

        public Oauth2TokenRequestBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Oauth2TokenRequestBuilder parameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Oauth2TokenRequestBuilder tokenRequestInfo(TokenRequestInfo tokenRequestInfo) {
            this.tokenRequestInfo = tokenRequestInfo;
            return this;
        }

        public Oauth2TokenRequest build() {
            return new Oauth2TokenRequest(this.clientId, this.parameters, this.tokenRequestInfo);
        }

        public String toString() {
            return "Oauth2TokenRequest.Oauth2TokenRequestBuilder(clientId=" + this.clientId + ", parameters=" + this.parameters + ", tokenRequestInfo=" + this.tokenRequestInfo + ")";
        }
    }

}

