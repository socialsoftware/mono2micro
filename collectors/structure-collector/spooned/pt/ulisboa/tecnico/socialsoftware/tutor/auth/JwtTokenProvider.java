package pt.ulisboa.tecnico.socialsoftware.tutor.auth;
import io.jsonwebtoken.*;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
@org.springframework.stereotype.Component
public class JwtTokenProvider {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.class);

    private pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository userRepository;

    private static java.security.PublicKey publicKey;

    private static java.security.PrivateKey privateKey;

    public JwtTokenProvider(pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static void generateKeys() {
        try {
            java.security.KeyPairGenerator keyPairGenerator = java.security.KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();
            pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.privateKey = keyPair.getPrivate();
            pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.publicKey = keyPair.getPublic();
        } catch (java.lang.Exception e) {
            pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.logger.error("Unable to generate keys");
        }
    }

    static java.lang.String generateToken(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user) {
        if (pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.publicKey == null) {
            pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.generateKeys();
        }
        Claims claims = Jwts.claims().setSubject(java.lang.String.valueOf(user.getId()));
        claims.put("role", user.getRole());
        java.util.Date now = new java.util.Date();
        java.util.Date expiryDate = new java.util.Date(now.getTime() + (((1000 * 60) * 60) * 24));
        return Jwts.builder().setClaims(claims).setIssuedAt(new java.util.Date()).setExpiration(expiryDate).signWith(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.privateKey).compact();
    }

    static java.lang.String getToken(javax.servlet.http.HttpServletRequest req) {
        java.lang.String authHeader = req.getHeader("Authorization");
        if ((authHeader != null) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        } else if ((authHeader != null) && authHeader.startsWith("AUTH")) {
            return authHeader.substring(4);
        } else if (authHeader != null) {
            return authHeader;
        }
        return "";
    }

    static int getUserId(java.lang.String token) {
        try {
            return java.lang.Integer.parseInt(Jwts.parserBuilder().setSigningKey(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.publicKey).build().parseClaimsJws(token).getBody().getSubject());
        } catch (MalformedJwtException ex) {
            pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.logger.error("Invalkey JWT token");
        } catch (ExpiredJwtException ex) {
            pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.logger.error("Unsupported JWT token");
        } catch (java.lang.IllegalArgumentException ex) {
            pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.logger.error("JWT claims string is empty.");
        }
        throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR);
    }

    org.springframework.security.core.Authentication getAuthentication(java.lang.String token) {
        pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = this.userRepository.findById(pt.ulisboa.tecnico.socialsoftware.tutor.auth.JwtTokenProvider.getUserId(token)).orElseThrow(() -> new <USER_NOT_FOUND>pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(getUserId(token)));
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
    }
}