package pt.ulisboa.tecnico.socialsoftware.tutor.auth;
public class AuthDto implements java.io.Serializable {
    private java.lang.String token;

    private pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto user;

    public AuthDto(java.lang.String token, pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto user) {
        this.token = token;
        this.user = user;
    }

    public java.lang.String getToken() {
        return token;
    }

    public void setToken(java.lang.String token) {
        this.token = token;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto getUser() {
        return user;
    }

    public void setUser(pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto user) {
        this.user = user;
    }
}