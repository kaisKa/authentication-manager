package ava.io.authentication_manager.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyRequest {
    private String countryCode;
    private String phone;
    private String verificationCode;
    private String email;

}
