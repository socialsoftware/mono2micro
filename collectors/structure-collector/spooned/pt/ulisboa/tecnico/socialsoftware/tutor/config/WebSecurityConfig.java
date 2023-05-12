package pt.ulisboa.tecnico.socialsoftware.tutor.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
@org.springframework.context.annotation.Configuration
@org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider jwtTokenProvider;

    @org.springframework.beans.factory.annotation.Value("${spring.profiles.active}")
    private java.lang.String activeProfile;

    @java.lang.Override
    public void configure(org.springframework.security.config.annotation.web.builders.WebSecurity web) throws java.lang.Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @java.lang.Override
    protected void configure(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws java.lang.Exception {
        if (activeProfile.equals("dev")) {
            http.httpBasic().disable().csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests().antMatchers(HttpMethod.OPTIONS, "/**").permitAll().antMatchers("/auth/**").permitAll().antMatchers("/images/**").permitAll().antMatchers("/swagger-ui.html").permitAll().antMatchers("/favicon.ico").permitAll().antMatchers("/webjars/*").permitAll().antMatchers("/webjars/**").permitAll().antMatchers("/swagger-resources/*").permitAll().antMatchers("/swagger-resources/**").permitAll().antMatchers("/v2/*").permitAll().antMatchers("/v2/**").permitAll().antMatchers("/csrf").permitAll().antMatchers("/").permitAll().anyRequest().authenticated().and().apply(new pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtConfigurer(jwtTokenProvider));
        } else {
            http.httpBasic().disable().csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests().antMatchers(HttpMethod.OPTIONS, "/**").permitAll().antMatchers("/auth/**").permitAll().antMatchers("/images/**").permitAll().anyRequest().authenticated().and().apply(new pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtConfigurer(jwtTokenProvider));
        }
    }

    @org.springframework.context.annotation.Bean
    org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        final org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new org.springframework.web.cors.CorsConfiguration().applyPermitDefaultValues());
        return source;
    }
}