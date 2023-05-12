package pt.ulisboa.tecnico.socialsoftware.tutor.auth;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
public class JwtConfigurer extends org.springframework.security.config.annotation.SecurityConfigurerAdapter<org.springframework.security.web.DefaultSecurityFilterChain, org.springframework.security.config.annotation.web.builders.HttpSecurity> {
    private pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider jwtTokenProvider;

    public JwtConfigurer(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @java.lang.Override
    public void configure(org.springframework.security.config.annotation.web.builders.HttpSecurity http) {
        pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenFilter customFilter = new pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenFilter(jwtTokenProvider);
        http.addFilterBefore(customFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
    }
}