/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.api.ApiRes
 *  cn.springcloud.gray.api.ApiRes$ApiResBuilder
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.core.io.ClassPathResource
 *  org.springframework.core.io.Resource
 *  org.springframework.http.HttpMethod
 *  org.springframework.security.access.AccessDeniedException
 *  org.springframework.security.authentication.AuthenticationManager
 *  org.springframework.security.config.annotation.SecurityBuilder
 *  org.springframework.security.config.annotation.web.HttpSecurityBuilder
 *  org.springframework.security.config.annotation.web.builders.HttpSecurity
 *  org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
 *  org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer
 *  org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer$AuthorizedUrl
 *  org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer$ExpressionInterceptUrlRegistry
 *  org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
 *  org.springframework.security.core.AuthenticationException
 *  org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder
 *  org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder$ClientBuilder
 *  org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder
 *  org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
 *  org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
 *  org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
 *  org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
 *  org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
 *  org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
 *  org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
 *  org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
 *  org.springframework.security.oauth2.provider.ClientDetailsService
 *  org.springframework.security.oauth2.provider.OAuth2RequestFactory
 *  org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator
 *  org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter
 *  org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory
 *  org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices
 *  org.springframework.security.oauth2.provider.token.TokenEnhancer
 *  org.springframework.security.oauth2.provider.token.TokenStore
 *  org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
 *  org.springframework.security.oauth2.provider.token.store.JwtTokenStore
 *  org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory
 *  org.springframework.security.web.AuthenticationEntryPoint
 *  org.springframework.security.web.access.AccessDeniedHandler
 */
package cn.springcloud.gray.server.configuration;

import cn.springcloud.gray.api.ApiRes;
import cn.springcloud.gray.server.oauth2.DefaultTokenGranter;
import cn.springcloud.gray.server.oauth2.Oauth2Service;
import cn.springcloud.gray.server.utils.WebHelper;
import java.io.IOException;
import java.security.KeyPair;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

@ConditionalOnProperty(value={"gray.server.security.oauth2.enabled"}, matchIfMissing=true)
@Configuration
public class OAuth2Config {
    private static final Logger log = LoggerFactory.getLogger(OAuth2Config.class);

    @Bean
    public JwtTokenStore tokenStore() {
        return new JwtTokenStore(this.jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyPair keyPair = new KeyStoreKeyFactory((Resource)new ClassPathResource("keystore.jks"), "password".toCharArray()).getKeyPair("selfsigned");
        converter.setKeyPair(keyPair);
        return converter;
    }

    @Bean
    public OAuth2RequestFactory requestFactory(ClientDetailsService clientDetailsService) {
        return new DefaultOAuth2RequestFactory(clientDetailsService);
    }

    @Bean
    public RefreshTokenGranter refreshTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        return new RefreshTokenGranter(tokenServices, clientDetailsService, requestFactory);
    }

    @Bean
    public DefaultTokenGranter defaultTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        return new DefaultTokenGranter(tokenServices, clientDetailsService, requestFactory);
    }

    @Bean
    public Oauth2Service oauth2Service(ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, DefaultTokenGranter defaultTokenGranter) {
        return new Oauth2Service(clientDetailsService, requestFactory, defaultTokenGranter);
    }

    @Configuration
    @EnableAuthorizationServer
    public static class OAuth2Config2
    extends AuthorizationServerConfigurerAdapter {
        @Autowired
        private AuthenticationManager authenticationManager;
        @Autowired
        private JwtTokenStore tokenStore;
        @Autowired
        private JwtAccessTokenConverter jwtAccessTokenConverter;

        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.inMemory().withClient("gray-server").secret("V@JA-#i+6BkDhhq9").authorizedGrantTypes(new String[]{"client_credentials", "refresh_token", "default"}).accessTokenValiditySeconds(2592000).refreshTokenValiditySeconds(5184000);
        }

        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
            security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
        }

        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.tokenStore((TokenStore)this.tokenStore).tokenEnhancer((TokenEnhancer)this.jwtAccessTokenConverter).authenticationManager(this.authenticationManager);
            endpoints.exceptionTranslator(null);
        }
    }

    @Configuration
    @EnableResourceServer
    public static class ResourceServerConfiguration
    extends ResourceServerConfigurerAdapter {
        @Autowired
        private TokenStore tokenStore;

        public void configure(HttpSecurity http) throws Exception {
            ((HttpSecurity)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl)((HttpSecurity)http.formLogin().and()).authorizeRequests().antMatchers(new String[]{"/gray/user/login"})).permitAll().antMatchers(new String[]{"/gray/user/login"})).permitAll().antMatchers(new String[]{"/gray/instances/enable"})).permitAll().antMatchers(new String[]{"/gray/instances"})).permitAll().antMatchers(new String[]{"/gray/trackDefinitions"})).permitAll().antMatchers(HttpMethod.OPTIONS, new String[]{"/gray/**"})).permitAll().antMatchers(new String[]{"/gray/service/**"})).authenticated().antMatchers(new String[]{"/gray/policy/**"})).authenticated().antMatchers(new String[]{"/gray/decision/**"})).authenticated().antMatchers(new String[]{"/gray/discover/**"})).authenticated().antMatchers(new String[]{"/gray/track/**"})).authenticated().anyRequest()).permitAll().and()).csrf().disable();
        }

        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.authenticationEntryPoint(new AuthenticationEntryPoint(){

                public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
                    log.debug("path:{} -> {}", (Object)request.getServletPath(), (Object)authException.getMessage());
                    response.setContentType("application/json");
                    response.setStatus(401);
                    ApiRes res = ApiRes.builder().code(String.valueOf(401)).message("\u65e0\u6743\u8bbf\u95ee").build();
                    WebHelper.response(response, (Object)res);
                }
            }).accessDeniedHandler(new AccessDeniedHandler(){

                public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
                    log.debug("path:{} -> {}", (Object)request.getServletPath(), (Object)accessDeniedException.getMessage());
                    response.setContentType("application/json");
                    response.setStatus(400);
                    ApiRes res = ApiRes.builder().code(String.valueOf(400)).message("\u65e0\u6743\u8bbf\u95ee").build();
                    WebHelper.response(response, (Object)res);
                }
            });
        }

    }

}

