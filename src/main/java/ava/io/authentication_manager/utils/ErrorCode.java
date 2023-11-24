package ava.io.authentication_manager.utils;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {

    SUCCESSFUL("Successful"),
    CREATED("Your account has been created successfully"),
    SENT_PHONE_OTP("OTP has been sent to your phone"),
    SENT_MAIL_OTP("OTP has been sent to you mail"),
    PHONE_VERIFIED("Your phone has been verified successfully"),
    MAIL_VERIFIED("Your email has been verified successfully"),
    RESET_PASSWORD("Your password has been reset successfully"),
    FAILED("Somethings went wrong"),
    EXITED_USER("User already exist"),
    EXITED_USER_IN_DB("User already exist in service db"),
    USER_NOT_FOUND( "User Not Found"),
    WRONG_PASSWORD("Not a valid password"),
    WRONG_OTP("Wrong verification code"),
    NOT_VALID_LICENCE("The licence number not valid"),
    NOT_VALID_PHONE("Not a valid phone number"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.getReasonPhrase() + ", Wrong credentials"),
    UNAUTHORIZED_TENANT(HttpStatus.UNAUTHORIZED.getReasonPhrase()+ ", this is not"),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN.getReasonPhrase()),
    DELETE_USER("User has been deleted"),
    ROLE_NOTE_EXIST("Role does not exist"),
    ROLE_REMOVED(" role has been removed"),
    MESSAGING_SERVICE_ISSUE("Something went Wrong will Sending verification code"),
    CONFLICT_ON_DB("More than one user has been Found"),
    LOGGED_OUT("User successfile logged out"),
    UN_TRUSTED_ISSUER("Not trusted issuer"),
    CHANGED_PASSWORD("Your password has changed successfully"),
    CHANGED_PHONE("Your phone number has changed successfully"),
    ADDED_USER_TO_ORG("The following user has been added to the organization"),
    Updated("User has been updated"), EXISTED_GROUP("Already registered Clinic"),
    EMAIL_NOT_VERIFIED("User email is not verified"),
    USER_DISABLED("User has been disabled successfully");





    private final String message;


    ErrorCode(  String message) {

        this.message = message;
    }


    /**
     * return Error Code regarding a boolean condition
     */
    public static ErrorCode isSuccess(Boolean isSuccess){
        return isSuccess ? ErrorCode.SUCCESSFUL : ErrorCode.FAILED;
    }



}
