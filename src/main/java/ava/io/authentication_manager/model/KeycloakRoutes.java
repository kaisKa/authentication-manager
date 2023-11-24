package ava.io.authentication_manager.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KeycloakRoutes {
    public String TOKEN;
    public String LOGOUT;
    public String authorization_code;
    public String USER_INFO;
    public String changePassword;
    public String resetPassword ;
    public String cert;
    public String realm;
}
