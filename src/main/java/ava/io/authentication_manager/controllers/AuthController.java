package ava.io.authentication_manager.controllers;

import ava.io.authentication_manager.config.mullti_tenant.TokenResolver;
import ava.io.authentication_manager.dtos.UserCredentials;
import ava.io.authentication_manager.dtos.UserDto;
import ava.io.authentication_manager.dtos.mappers.UserRepresentationMapper;
import ava.io.authentication_manager.enums.Channel;
import ava.io.authentication_manager.enums.Tenant;
import ava.io.authentication_manager.model.*;
import ava.io.authentication_manager.services.AuthService;
import ava.io.authentication_manager.services.KeycloakService;
import ava.io.authentication_manager.utils.ErrorCode;
import ava.io.authentication_manager.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.SneakyThrows;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.*;

@RestController
@RequestMapping("${spring.base_url}" + "/auth")
public class AuthController {


    private final KeycloakService keycloakService;
    private final AuthService authService;
    private final UserRepresentationMapper userRepresentationMapper;


    public AuthController(KeycloakService keycloakService, AuthService authService, UserRepresentationMapper userRepresentationMapper) {
        this.keycloakService = keycloakService;
        this.authService = authService;
        this.userRepresentationMapper = userRepresentationMapper;
    }

    @GetMapping("/hello")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<String>> greeting() {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), "Hello there!!!"));
    }

    /**
     * 1. Register a user <br>
     * 2. Assign the related role <br>
     * 3. save the user in localdb <br>
     *
     * @param userDTO Represent the data needed to create a new user
     */
    @PostMapping("/tenant/{tenant}")
    @Operation(summary = "Register a new user into the system, providing the verification channel and the tenant type")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<UserDto>> register(@PathVariable("tenant") String tenant, @RequestBody @Valid UserDto userDTO, @RequestParam("channel") Channel channel) {
        try (Response response = authService.register(userDTO, Tenant.valueOf(tenant.toUpperCase()), channel)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response.getStatus() != HttpResponseCodes.SC_CREATED
                    ? new GeneralResponse<>(true, response.getStatusInfo().getReasonPhrase(), null) :
                    new GeneralResponse<>(false, ErrorCode.CREATED.getMessage(), userDTO));
        }
    }

    @PostMapping("/tenant/{tenant}/get-token")
    @Operation(summary = "Get Token and some user info , by user name and password")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<LoginResponse>> getToken(@PathVariable String tenant, @RequestBody UserCredentials userCredentials) throws URISyntaxException {
        LoginResponse res = authService.login(tenant, userCredentials);
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), res));
    }

    /**
     * Regardless of the verification channel, a code will be received and verified regarding the code in db.
     *
     * @param verifyRequest Represent the data needed to verify the phone to be verified <br>
     */
    @PutMapping("/tenant/{tenant}/verify-phone")
    @Operation(summary = "Used to verify a phone after a successful registration ")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> verifyPhone(@PathVariable String tenant, @RequestBody VerifyRequest verifyRequest) {
        var res = authService.verifyPhone(tenant, verifyRequest);
        return ResponseEntity.ok(new GeneralResponse<>(!res,
                ErrorCode.PHONE_VERIFIED.getMessage(),
                Map.of("isVerified", res)));
    }


    @PutMapping("/tenant/{tenant}/verify-email")
    @Operation(summary = "Used to verify a email after a successful registration ")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> verifyEmail(@PathVariable String tenant, @RequestBody VerifyRequest verifyRequest) {
        var res = authService.verifyMail(tenant, verifyRequest);
        return ResponseEntity.ok(new GeneralResponse<>(!res,
                ErrorCode.MAIL_VERIFIED.getMessage(),
                Map.of("isVerified", res)));
    }


    @Profile("stage")
    @PostMapping("/refresh-token")
    @Operation(summary = "Used to get a new token when it expires using the old refresh token")
    @SecurityRequirement(name = "Bearer Authentication")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<AccessTokenResponse>> refreshToken(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication, @RequestBody RefreshToken body) throws URISyntaxException {
        return ResponseEntity.ok(new GeneralResponse<>(true, ErrorCode.SUCCESSFUL.getMessage(), keycloakService.refreshAccessToken(TokenResolver.resolveTenant(authentication), body.getRefreshToken())));
    }


    @GetMapping("/tenant/{tenant}/identify/{phone}")
    @Operation(summary = "Get the user name by a phone number")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> identify(@PathVariable String tenant, @PathVariable String phone) {
        var res = keycloakService.getUsernameByPhoneNum(tenant, phone);
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), res));
    }

    @GetMapping("/tenant/{tenant}/identify/mail/{email}")
    @Operation(summary = "Get the user name by a email number")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> identifyByMail(@PathVariable String tenant, @PathVariable String email) {
        var res = keycloakService.getUserNameByEmail(tenant, email);
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), res));
    }


    @GetMapping("/tenant/{tenant}/send-verification-code/{phone}")
    @Operation(summary = "Send verification code via sms")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> sendVerificationCode(@PathVariable String tenant, @PathVariable String phone) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SENT_PHONE_OTP.getMessage(), Map.of("response", authService.sendVerificationSms(tenant, phone))));
    }

    @GetMapping("/tenant/{tenant}/mail-verification-code/{email}")
    @Operation(summary = "Send verification code via mail")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> mailVerificationCode(@PathVariable String tenant, @PathVariable String email) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SENT_MAIL_OTP.getMessage(), Map.of("response", authService.sendVerificationMail(tenant, email))));
    }

    @PostMapping("/tenant/{tenant}/verifying-request")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> verifyingRequest(@PathVariable String tenant, @RequestBody VerifyRequest verifyRequest, @RequestParam Channel channel) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SENT_MAIL_OTP.getMessage(), Map.of("response", authService.verifyingRequest(tenant, verifyRequest, channel))));
    }


    @Deprecated
    @PostMapping("/tenant/{tenant}/verify-otp")
    @Operation(summary = "Verify an otp that has been sent by SMS")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> verifyOtp(@PathVariable String tenant, @RequestBody VerifyRequest verifyRequest) {
        String res = authService.verifyOTp(tenant, verifyRequest);
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.isSuccess(res != null).getMessage(),
                Map.of("response", true, "tmp_code", Objects.requireNonNull(res))));
    }

    @PostMapping("/tenant/{tenant}/verify")
    @Operation(summary = "Verify an otp that has been sent by any chanel, notice u need to specify the channel")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> verifyOtp(@PathVariable String tenant, @RequestBody VerifyRequest verifyRequest, @RequestParam Channel channel) {

        String res = authService.verifyOTp(tenant, verifyRequest, channel);
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.isSuccess(res != null).getMessage(),
                Map.of("response", true, "tmp_code", Objects.requireNonNull(res))));
    }

    @PostMapping("/changePassword")
    @Operation(summary = "Change the password for a user, does not require verification")
    @SecurityRequirement(name = "Bearer Authentication")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> changePassword(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication, @RequestBody ChangePasswordRequest request) {
        boolean res = authService.changePassword(TokenResolver.resolveTenant(authentication), TokenResolver.resolveId(authentication), request);
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.CHANGED_PASSWORD.getMessage(), Map.of("response", res)));
    }

    @PostMapping("/changePhone")
    @Operation(summary = "Change the phone for a user, verification is required")
    @SecurityRequirement(name = "Bearer Authentication")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> changePhone(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication, @RequestBody ChangePhoneRequest request) {
        boolean res = authService.changePhone(TokenResolver.resolveTenant(authentication), TokenResolver.resolveId(), request);
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.CHANGED_PHONE.getMessage(), Map.of("response", res)));
    }


    @PutMapping("/tenant/{tenant}/resetPassword")
    @Operation(summary = "Reset user password ")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> resetPassword(@PathVariable String tenant, @RequestBody ResetPasswordRequest resetPasswordRequest) {
        authService.resetPassword(tenant, Helper.getGSM(resetPasswordRequest.getCountryCode(), resetPasswordRequest.getPhone()), resetPasswordRequest);
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.RESET_PASSWORD.getMessage(), Map.of("response", "done")));
    }


    @Deprecated
    @GetMapping("/users/{id}") //user id in db
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GeneralResponse<UserDto>> getUserInfo(@PathVariable UUID id, @Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), userRepresentationMapper.toDto(keycloakService.getUserInfo(TokenResolver.resolveTenant(authentication), id.toString()))));
//        Optional<UserLoginData> user = userLoginDataService.getUserLoginRepo().findById(id);
//        if (!user.isPresent())
//            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
//        else
//            return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), loginDataMapper.toDto(user.get())));
    }

    @PutMapping("/user/info")
    @Operation(summary = "update some information of a user")
    @SecurityRequirement(name = "Bearer Authentication")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> updateUserInfo(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication, @RequestBody UserDto userDTO) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.Updated.getMessage(), authService.updateUserInfo(TokenResolver.resolveTenant(authentication), TokenResolver.resolveId(authentication), userDTO)));
    }

    @SneakyThrows
    @Operation(summary = "Get the user information")
    @GetMapping("/user/info")
    @SecurityRequirement(name = "Bearer Authentication")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<UserDto>> getUserInfo(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication) {//(@AuthenticationPrincipal Jwt principal) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), userRepresentationMapper.toDto(keycloakService.getUserInfo(TokenResolver.resolveTenant(authentication), TokenResolver.resolveId()))));
    }


//    @DeleteMapping("/{id}")
//    @Operation(summary = "Delete user by its id")
//    @Deprecated
//    public ResponseEntity<GeneralResponse<UserDTO>> delete(@PathVariable UUID id){
//        var res = authService.delete(id);
//        return ResponseEntity.ok(new GeneralResponse<>(false,ErrorCode.DELETE_USER.getMessage(), res));
//    }
//
//
//    @DeleteMapping("/phone/{phone}")
//    @Operation(summary = "Delete user by phone")
//    @Deprecated
//    public ResponseEntity<GeneralResponse<UserDTO>> delete(@PathVariable String phone){
//        var res = authService.deleteByPhone(phone);
//        return ResponseEntity.ok(new GeneralResponse<>(false,ErrorCode.DELETE_USER.getMessage(), res));
//    }


    @GetMapping("/logout")
    @Operation(summary = "Sign out for a logged in user ")
    @SecurityRequirement(name = "Bearer Authentication")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> logout(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication) {
        authService.logout(TokenResolver.resolveTenant(authentication), TokenResolver.resolveId());
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.LOGGED_OUT.getMessage(), Map.of("response", "logged out")));
    }

    @PutMapping("/user/di-activate")
    @Operation(summary = "di activate the user account")
    @SecurityRequirement(name = "Bearer Authentication")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> diActivateUser(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication) {
        return ResponseEntity.ok().body(new GeneralResponse<>(false, ErrorCode.USER_DISABLED.getMessage(), authService.diActivateUser(TokenResolver.resolveTenant(authentication), TokenResolver.resolveId(authentication))));
    }

    @PutMapping("/user/delete")
    @Operation(summary = "Permanent deletion of users")
    @SecurityRequirement(name = "Bearer Authentication")
    @SuppressWarnings("unused")
    public ResponseEntity<GeneralResponse<Object>> user(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication) {
        return ResponseEntity.ok().body(new GeneralResponse<>(false, ErrorCode.USER_DISABLED.getMessage(), authService.delete(TokenResolver.resolveTenant(authentication), TokenResolver.resolveId(authentication))));
    }

}
