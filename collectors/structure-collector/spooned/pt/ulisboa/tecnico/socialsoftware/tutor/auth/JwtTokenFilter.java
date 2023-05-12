package pt.ulisboa.tecnico.socialsoftware.tutor.auth;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
public class JwtTokenFilter extends org.springframework.web.filter.GenericFilterBean {
    private pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider jwtTokenProvider;

    JwtTokenFilter(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @java.lang.Override
    public void doFilter(javax.servlet.ServletRequest req, javax.servlet.ServletResponse res, javax.servlet.FilterChain filterChain) throws java.io.IOException, javax.servlet.ServletException {
        java.lang.String token = pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.getToken(((javax.servlet.http.HttpServletRequest) (req)));
        if (!token.isEmpty()) {
            org.springframework.security.core.Authentication auth = jwtTokenProvider.getAuthentication(token);
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(req, res);
    }
}