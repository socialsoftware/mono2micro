package pt.ulisboa.tecnico.socialsoftware.tutor.auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@org.springframework.web.bind.annotation.RestController
public class AuthController {
    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthService authService;

    @org.springframework.beans.factory.annotation.Value("${base.url}")
    private java.lang.String baseUrl;

    @org.springframework.beans.factory.annotation.Value("${oauth.consumer.key}")
    private java.lang.String oauthConsumerKey;

    @org.springframework.beans.factory.annotation.Value("${oauth.consumer.secret}")
    private java.lang.String oauthConsumerSecret;

    @org.springframework.beans.factory.annotation.Value("${callback.url}")
    private java.lang.String callbackUrl;

    @org.springframework.web.bind.annotation.GetMapping("/auth/fenix")
    public pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto fenixAuth(@org.springframework.web.bind.annotation.RequestParam
    java.lang.String code) {
        pt.ulisboa.tecnico.socialsoftware.tutor.auth.FenixEduInterface fenix = new pt.ulisboa.tecnico.socialsoftware.tutor.auth.FenixEduInterface(baseUrl, oauthConsumerKey, oauthConsumerSecret, callbackUrl);
        fenix.authenticate(code);
        return this.authService.fenixAuth(fenix);
    }

    @org.springframework.web.bind.annotation.GetMapping("/auth/demo/student")
    public pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto demoStudentAuth() {
        return this.authService.demoStudentAuth();
    }

    @org.springframework.web.bind.annotation.GetMapping("/auth/demo/teacher")
    public pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto demoTeacherAuth() {
        return this.authService.demoTeacherAuth();
    }

    @org.springframework.web.bind.annotation.GetMapping("/auth/demo/admin")
    public pt.ulisboa.tecnico.socialsoftware.tutor.auth.AuthDto demoAdminAuth() {
        return this.authService.demoAdminAuth();
    }
}