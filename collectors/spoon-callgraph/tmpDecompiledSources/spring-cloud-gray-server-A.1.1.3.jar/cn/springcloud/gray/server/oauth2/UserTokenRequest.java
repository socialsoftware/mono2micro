/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.security.core.userdetails.UserDetails
 *  org.springframework.security.oauth2.provider.ClientDetails
 *  org.springframework.security.oauth2.provider.OAuth2Request
 *  org.springframework.security.oauth2.provider.TokenRequest
 */
package cn.springcloud.gray.server.oauth2;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;

public class UserTokenRequest
extends TokenRequest {
    private static final long serialVersionUID = 7584363578550394391L;
    private UserDetails userDetails;
    private Map<String, Serializable> extensionProperties;

    public UserTokenRequest(Map<String, String> requestParameters, String clientId, Collection<String> scope, String grantType) {
        super(requestParameters, clientId, scope, grantType);
    }

    public UserTokenRequest(TokenRequest tokenRequest, UserDetails userDetails, Map<String, Serializable> extensionProperties) {
        this(tokenRequest.getRequestParameters(), tokenRequest.getClientId(), tokenRequest.getScope(), tokenRequest.getGrantType());
        this.setExtensionProperties(extensionProperties);
        this.userDetails = userDetails;
    }

    public Map<String, Serializable> getExtensionProperties() {
        return this.extensionProperties;
    }

    public void setExtensionProperties(Map<String, Serializable> extensionProperties) {
        this.extensionProperties = extensionProperties;
    }

    public UserDetails getUserDetails() {
        return this.userDetails;
    }

    public OAuth2Request createOAuth2Request(ClientDetails client) {
        Map requestParameters = this.getRequestParameters();
        HashMap<String, String> modifiable = new HashMap<String, String>(requestParameters);
        modifiable.remove("password");
        modifiable.remove("client_secret");
        modifiable.put("grant_type", this.getGrantType());
        return new OAuth2Request(modifiable, client.getClientId(), client.getAuthorities(), true, this.getScope(), client.getResourceIds(), null, null, this.extensionProperties);
    }
}

