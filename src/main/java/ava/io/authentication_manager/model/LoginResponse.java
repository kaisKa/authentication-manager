package ava.io.authentication_manager.model;

import ava.io.authentication_manager.dtos.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    @JsonProperty("access_token")
    protected String token;
    @JsonProperty("expires_in")
    protected long expiresIn;
    @JsonProperty("refresh_expires_in")
    protected long refreshExpiresIn;
    @JsonProperty("refresh_token")
    protected String refreshToken;
    @JsonProperty("token_type")
    protected String tokenType;
    @JsonProperty("id_token")
    protected String idToken;
    @JsonProperty("not-before-policy")
    protected int notBeforePolicy;
    @JsonProperty("session_state")
    protected String sessionState;
    @JsonIgnore
    protected Map<String, Object> otherClaims = new HashMap();
    @JsonProperty("scope")
    protected String scope;
    @JsonIgnore
    @JsonProperty("error")
    protected String error;
    @JsonIgnore
    @JsonProperty("error_description")
    protected String errorDescription;
    @JsonIgnore
    @JsonProperty("error_uri")
    protected String errorUri;


    //  ***** info to be returned *****  //

    private UserDto userInfo;
}
