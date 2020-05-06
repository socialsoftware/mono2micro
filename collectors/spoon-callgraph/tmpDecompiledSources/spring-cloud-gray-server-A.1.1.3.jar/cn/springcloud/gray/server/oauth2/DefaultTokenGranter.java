/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.security.authentication.UsernamePasswordAuthenticationToken
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.userdetails.UserDetails
 *  org.springframework.security.oauth2.provider.ClientDetails
 *  org.springframework.security.oauth2.provider.ClientDetailsService
 *  org.springframework.security.oauth2.provider.OAuth2Authentication
 *  org.springframework.security.oauth2.provider.OAuth2Request
 *  org.springframework.security.oauth2.provider.OAuth2RequestFactory
 *  org.springframework.security.oauth2.provider.TokenRequest
 *  org.springframework.security.oauth2.provider.token.AbstractTokenGranter
 *  org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices
 */
package cn.springcloud.gray.server.oauth2;

import cn.springcloud.gray.server.oauth2.UserTokenRequest;
import java.util.Collection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

public class DefaultTokenGranter
extends AbstractTokenGranter {
    public static final String GRANT_TYPE = "default";

    public DefaultTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
    }

    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        UserDetails userDetails = null;
        if (tokenRequest instanceof UserTokenRequest) {
            userDetails = ((UserTokenRequest)tokenRequest).getUserDetails();
        }
        return this.getOAuth2Authentication(client, tokenRequest, userDetails);
    }

    public OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken((Object)userDetails.getUsername(), (Object)userDetails.getPassword(), userDetails.getAuthorities());
        OAuth2Request storedOAuth2Request = this.getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, (Authentication)authentication);
    }
}

