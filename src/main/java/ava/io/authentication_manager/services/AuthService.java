package ava.io.authentication_manager.services;


import ava.io.authentication_manager.config.AppConfig;
import ava.io.authentication_manager.config.PathBasedConfigResolver1;
import ava.io.authentication_manager.db.entities.UserAccount;
import ava.io.authentication_manager.dtos.*;
import ava.io.authentication_manager.dtos.mappers.AccessTokenMapper;
import ava.io.authentication_manager.dtos.mappers.UserRepresentationMapper;
import ava.io.authentication_manager.enums.Channel;
import ava.io.authentication_manager.enums.Role;
import ava.io.authentication_manager.enums.Tenant;
import ava.io.authentication_manager.model.*;
import ava.io.authentication_manager.model.VerifyRequest;
import ava.io.authentication_manager.utils.ErrorCode;
import ava.io.authentication_manager.utils.Helper;
import ava.io.authentication_manager.utils.custom_excpeption.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.*;

@Service
@Slf4j
public class AuthService {

    private final AppConfig appConfig;
    private final KeycloakService keycloakService;
    private final PathBasedConfigResolver1 config;
    private final TwilioService twilioService;
    private final RestTemplate restTemplate;
    private final UserRepresentationMapper userRepresentationMapper;
    private final UserAccountService userAccountService;
    private final AccessTokenMapper accessTokenMapper;
    private final ObjectMapper mapper;
    private final GmailService gmailService;


    public AuthService(AppConfig appConfig, KeycloakService keycloakService,
                       PathBasedConfigResolver1 config, TwilioService twilioService,
                       @Qualifier("notSecureRestTemplate") RestTemplate restTemplate,
                       UserRepresentationMapper userRepresentationMapper,
                       AccessTokenMapper accessTokenMapper, ObjectMapper mapper,
                       GmailService gmailService, UserAccountService userAccountService) {
        this.appConfig = appConfig;
        this.keycloakService = keycloakService;
        this.config = config;
        this.twilioService = twilioService;
        this.restTemplate = restTemplate;
        this.userRepresentationMapper = userRepresentationMapper;
        this.accessTokenMapper = accessTokenMapper;
        this.mapper = mapper;
        this.userAccountService = userAccountService;
        this.gmailService = gmailService;
    }

    //    /**
//     * 1. get the access token for the user
//     * 2. parser token
//     * 3. get the role and compare it with the passed one
//     * 4. case it matches -> return the access token
//     * 5. case it does not -> raise 401 ex
//     *
//     * @param request
//     * @return
//     */
//    public LoginResponse login(String tenant, UserCredentials userCredentials) {
//        final LoginResponse resp = new LoginResponse();
//        Optional<UserLoginData> user = userLoginService.getUserLoginRepo().findByUserName(userCredentials.getUsername());
//        if (user.isEmpty())
//            throw new UserNotFoundException();
//
//        resp.setUserInfo(loginDataMapper.toDto(user.get()));
//
//        if (!resp.getUserInfo().getIsVerified())
//            return resp;
//
//
//        AccessTokenResponse accessToken = keycloakService.getAccessToken(tenant, userCredentials);
//
//
//        var resp1 = accessTokenMapper.toDto(accessToken);
//        user.ifPresent(userLoginData -> resp1.setUserInfo(loginDataMapper.toDto(userLoginData)));
//        return resp1;
//    }
    public LoginResponse login(String tenant, UserCredentials request) {
        LoginResponse resp = new LoginResponse();
        boolean isClinic = Tenant.CLINIC.getTenant().equalsIgnoreCase(tenant);
        UserRepresentation kUser = isClinic
                ? keycloakService.getUserByEmail(tenant, request.getUsername())
                : keycloakService.getUserByUserName(tenant, request.getUsername());

        if (kUser == null)
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());

        resp.setUserInfo(userRepresentationMapper.toDto(kUser));

        if (!kUser.isEmailVerified() && isClinic)
            throw new HttpCustomException(HttpStatus.BAD_REQUEST.value(), ErrorCode.EMAIL_NOT_VERIFIED.getMessage());

        // in case not verified
        if (!Boolean.parseBoolean(getAtt(kUser, Helper.IS_VERIFIED)))
            return resp;

        AccessTokenResponse accessToken = keycloakService.getAccessToken(tenant, request);
        var resp1 = accessTokenMapper.toDto(accessToken);
        resp1.setUserInfo(userRepresentationMapper.toDto(kUser));
        return resp1;
    }


    /**
     * 1. Generate the userName <br>
     * 2. Generate the GSM <br>
     * 3. Get the roles <br>
     * 4. Create the user on keycloak <br>
     * 5. Get the Creation ID <br>
     * 6. Create the user on local db <br>
     * 7. Send the verification code according to the passed channel <br>
     *
     * @param userDto Represent a user date
     * @param tenant  Represent the tenant 'patient, provider, clinic,...'
     * @param channel Represent the channel used to verify the user
     */
    @SneakyThrows
    @Transactional
    public Response register(UserDto userDto, Tenant tenant, Channel channel) {
        Response response = null;
        String userId = null;
        try {

            String username = userDto.getFirstName() + "_" + userDto.getLastName() + Helper.generateCode();
            userDto.setUserName(username.toLowerCase());
            String phone = userDto.getCountryCode() + Helper.removeLeadingZeros(userDto.getPhone());
            List<Role> roles = List.of(Role.valueOf(tenant.getTenant().toUpperCase()), Role.valueOf(tenant.getType().toUpperCase()));
            response = keycloakService.createKeycloakUser(tenant.getTenant(), userDto, roles, Map.of(Helper.GSM, List.of(phone),
                    Helper.TENANT_ID, List.of(tenant.getTenant()),
                    Helper.IS_VERIFIED, List.of("false")));
            userId = keycloakService.getId(response);

            if (userId != null) {
                //create the local userAccount
                UserAccount userAccount = UserAccount.builder().id(UUID.fromString(userId)).build();
                userAccountService.save(userAccount);
            }

            log.info("A new user has been add to Sys DB");


            // invoke callbacks
//            if (userDto.getCallBackUrl() != null && !userDto.getCallBackUrl().isEmpty()) {
//                JsonNode callBackResponse = callBack(userDto, userId);
//                UserRepresentation kUser = config.getRealmResource(tenant.getTenant()).users().get(userId).toRepresentation();
//                var updatedUser = setAtt(kUser, Helper.ACCOUNT_ID, callBackResponse.get("id").toString());
//                keycloakService.updateUser(tenant.getTenant(), updatedUser);
//            }

            if (channel == Channel.none)
                return response;

            if (channel == Channel.phone)
                sendVerificationSms(tenant.getTenant(), phone);
            else if (channel == Channel.email)
                sendVerificationMail(tenant.getTenant(), userDto.getEmailId());
        } catch (Exception e) {
            if (userId != null)
                keycloakService.deleteKeycloakUser(tenant.getTenant(), userId);
            throw e;
        }

        return response;
    }


    /**
     * 1. get the user from db <br>
     * 2. check otp equality <br>
     * 3. in case true -> Enable user <br>
     * 4. in case false -> threw WrongOTPException <br>
     * @param verifyRequest  Represent the needed data to verify the phone
     *                      
     *
     *
     */
    public Boolean verifyPhone(String tenant, VerifyRequest verifyRequest) {

        String gsm = verifyRequest.getCountryCode() + Helper.removeLeadingZeros(verifyRequest.getPhone());
        var res = twilioService.verifyOTP(gsm, verifyRequest.getVerificationCode());
        var keycloakUser = keycloakService.getUserByPhoneNum(tenant, gsm);

        if (!res)
            throw new BadRequestException(ErrorCode.WRONG_OTP.getMessage());
        if (keycloakUser != null) {
            // need to enable the user on keycloak
            keycloakService.EnableUser(tenant, gsm, Channel.phone);
            return true;
        }
        return false;
    }


    /**
     * 1. get the user from db <br>
     * 2. verify otp <br>
     * 3. in case true -> Enable user <br>
     * 4. in case false -> threw WrongOTPException <br>
     * @param request Represent the needed data to verify the mail
     *
     *
     */
    public Boolean verifyMail(String tenant, VerifyRequest request) {

        UserRepresentation keycloakUser = keycloakService.getUserByEmail(tenant, request.getEmail());
//        var user = userLoginService.getUserLoginRepo().findByKeycloakId(UUID.fromString(keycloakUser.getId()));
//        if (user.isEmpty())
//            throw new UserNotFoundException();

        if (keycloakUser == null)
            throw new UserNotFoundException();
        boolean isIdentical = request.getVerificationCode().equalsIgnoreCase(getAtt(keycloakUser, Helper.VERIFICATION_CODE));
        if (!isIdentical)
            throw new BadRequestException(ErrorCode.WRONG_OTP.getMessage());

        // need to enable the user on keycloak
//        keycloakUser.setEmailVerified(true); // to verify the email
//        keycloakUser.setEnabled(true); // to enable the user

//        keycloakService.updateUser(tenant, keycloakUser);
//        user.get().setIsVerified(true);
        keycloakService.EnableUser(tenant, request.getEmail(), Channel.email);
        // __________ TODO: set is verified
        return true; //TODO: what should be returned
    }


    /**
     * 1. generate a code <br>
     * 2. set the code in the user field <br>
     * 3. send via email <br>
     *
     * @param tenant Represent the tenant
     */
    @SneakyThrows
    public Boolean sendVerificationMail(String tenant, String email) {
        String code = Helper.generateCode();
        UserRepresentation keycloakUser = keycloakService.getUserByEmail(tenant, email);
        if (keycloakUser == null) {
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        //update verification code
        var updatedUser = setAtt(keycloakUser, Helper.VERIFICATION_CODE, code);
        //update the user
        keycloakService.updateUser(tenant, updatedUser);
        // invoke SMS API
        gmailService.sendSimpleMail(new EmailDetails(email, code));
        return true; // TODO: what sould we return
    }


    @SneakyThrows
    public Boolean sendVerificationSms(String tenant, String gsm) {
        String code = Helper.generateCode();
        UserRepresentation keycloakUser = keycloakService.getUserByPhoneNum(tenant, gsm);
        if (keycloakUser == null) {
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
        }


        //update verification code
        var updatedUser = setAtt(keycloakUser, Helper.VERIFICATION_CODE, code);
        //update the user
        keycloakService.updateUser(tenant, updatedUser);
        // invoke SMS API
        twilioService.sendCode(gsm); //sendSms(gsm);
        return true;
    }


    /**
     * 1. Generate a verification code <br>
     * 2. Store the code in DB <br>
     * 3. Send the code regarding the required channel <br>
     * TODO: check for the isVerified field
     *
     * @param request Represent the data needed to send a verification request
     * @param channel Define the channel to send the request through
     */
    public Boolean verifyingRequest(String tenant, VerifyRequest request, Channel channel) {
        //Generate code
        String code = Helper.generateCode();
        String gsm = request.getCountryCode() + Helper.removeLeadingZeros(request.getPhone());

        UserRepresentation keycloakUser = channel == Channel.email
                ? keycloakService.getUserByEmail(tenant, request.getEmail())
                : keycloakService.getUserByPhoneNum(tenant, request.getPhone());

        if (keycloakUser == null)
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());


        //update verifi code
        var updatedUser = setAtt(keycloakUser, Helper.VERIFICATION_CODE, code);
        //update the user
        keycloakService.updateUser(tenant, updatedUser);


        // Send via designated channel
        if (channel == Channel.email)
            gmailService.sendSimpleMail(new EmailDetails(request.getEmail(), code));
        else if (channel == Channel.phone)
            twilioService.sendSms(gsm, appConfig.getVerify_message() + code);


        return true;
    }


    /**
     * 1. retrieve the user by id <br>
     * 2. validate the old password <br>
     * 3. in case valid -> reset with the new one <br>
     * 4. in case not -> raise NotValidPassword <br>
     *
     * @param userId  represent the id of referred user
     * @param request represent the needed data to change the password
     */
    public Boolean changePassword(String tenant, String userId, ChangePasswordRequest request) {

        // Get the user from sys db
        UserRepresentation keycloakUser = keycloakService.getUserById(tenant, userId);

        if (keycloakUser == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
        }
        //Send keycloak is with old password to validation
        keycloakService.validateOldPassword(tenant, userId, request.getOldPassword());
        //change the password
        keycloakService.resetPassword(tenant, userId, request.getNewPassword());

        return true;
    }

    public Boolean changePhone(String tenant, String userId, ChangePhoneRequest request) {

        String gsm = request.getNewCountryCode() + Helper.removeLeadingZeros(request.getNewPhone());
        // Get the user from sys db
        UserRepresentation user = keycloakService.getRealmResource(tenant).users().get(userId).toRepresentation(); //userLoginService.getByKeycloakId(UUID.fromString(userId));

        if (user == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        var att = user.getAttributes();
        att.put(Helper.GSM, List.of(gsm));
        user.setAttributes(att);
        keycloakService.updateUser(tenant, user);
        return true;
    }

    public UserDto updateUserInfo(String tenant, String userId, UserDto userDTO) {
        // get the user
        var kUser = config.getRealmResource(tenant).users().get(userId).toRepresentation();
        if (kUser == null)
            throw new UserNotFoundException();


        kUser.setFirstName(userDTO.getFirstName());
        kUser.setLastName(userDTO.getLastName());

        keycloakService.updateUser(tenant, kUser);

        return userRepresentationMapper.toDto(kUser);

    }

    @SneakyThrows
    public void resetPassword(String tenant, String phone, ResetPasswordRequest request) {

        UserRepresentation keycloakUser = keycloakService.getUserByPhoneNum(tenant, phone);
        if (keycloakUser == null) {
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        // check the temp code
        boolean isIdentical = request.getVerificationCode().equalsIgnoreCase(getAtt(keycloakUser, Helper.VERIFICATION_CODE));
        if (!isIdentical)
            throw new UnauthorisedException("the temp Code is missing ");

        keycloakService.resetPassword(tenant, keycloakUser.getId(), request.getNewPassword());

    }

    @Deprecated
    @SneakyThrows
    public String verifyOTp(String tenant, VerifyRequest verifyRequest) {


        String gsm = verifyRequest.getCountryCode() + Helper.removeLeadingZeros(verifyRequest.getPhone());
        String code = Helper.generateCode();
        UserRepresentation keycloakUser = keycloakService.getUserByPhoneNum(tenant, gsm);
        if (keycloakUser == null)
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());

        //update verifi code
        var updatedUser = setAtt(keycloakUser, Helper.VERIFICATION_CODE, code);
        //update the user
        keycloakService.updateUser(tenant, updatedUser);


        // in case result true then generate a temp code and return it to client
        Boolean twilioResult = twilioService.verifyOTP(gsm, verifyRequest.getVerificationCode());
        if (!twilioResult)
            throw new BadRequestException("wrong verification code");

        return code;
    }

    @SneakyThrows
    public String verifyOTp(String tenant, VerifyRequest request, Channel channel) {

        if (channel == Channel.phone) {
            String gsm = request.getCountryCode() + Helper.removeLeadingZeros(request.getPhone());
            // generate a temporary code and use it as a security manner
            String code = Helper.generateCode();
            UserRepresentation keycloakUser = keycloakService.getUserByPhoneNum(tenant, gsm);

            if (keycloakUser == null)
                throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());

            //update verifi code
            var updatedUser = setAtt(keycloakUser, Helper.VERIFICATION_CODE, code);
            //update the user
            keycloakService.updateUser(tenant, updatedUser);

            // in case result true then generate a temp code and return it to client
            Boolean twilioResult = twilioService.verifyOTP(gsm, request.getVerificationCode());
            if (!twilioResult)
                throw new BadRequestException(ErrorCode.WRONG_OTP.getMessage());
            return code;
        } else {
            UserRepresentation keycloakUser = keycloakService.getUserByEmail(tenant, request.getEmail());
            if (keycloakUser == null)
                throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
            boolean isIdentical = request.getVerificationCode().equalsIgnoreCase(getAtt(keycloakUser, Helper.VERIFICATION_CODE));
            if (!isIdentical)
                throw new BadRequestException(ErrorCode.WRONG_OTP.getMessage());
            return getAtt(keycloakUser, Helper.VERIFICATION_CODE);
        }


    }

    public void logout(String tenant, String userId) {
        keycloakService.logout(tenant, userId);
    }


    //******************** private functionality *****************************//


//    private Boolean isRightTenant(String accessToken, Tenant tenant) {
//        var requiredAuthority = KeycloakJwtRolesConverter.PREFIX_RESOURCE_ROLE + config.getCache().get(tenant).getResourceName() + "_" + tenant;
//
//        Map<String, String> map = new Hashtable<String, String>();
//
//        Jwt principal = jwtDecoder.decode(accessToken);
//        Collection<GrantedAuthority> clientRoles = converter.getClientRoles(principal);
//        return clientRoles.stream()
//                .anyMatch(r -> r.getAuthority().equalsIgnoreCase(requiredAuthority));
//
//
//    }


    private JsonNode callBack(UserDto userDTO, String userId) throws Exception {
        //add keycloak id to as subject in the sent body
        ObjectNode requestBody = ((ObjectNode) userDTO.getCallBackBody()).put("subject", userId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", "application/json;charset=UTF-8");
        HttpEntity<JsonNode> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(userDTO.getCallBackUrl(), request, String.class);
        if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
            return mapper.readTree(response.getBody());
        }
        throw new Exception();


    }

    private UserRepresentation setAtt(UserRepresentation user, String key, String value) {

        var att = user.getAttributes();
        att.put(key, List.of(value));
        user.setAttributes(att);
        return user;
    }

    private String getAtt(UserRepresentation user, String key) {
        var att = user.getAttributes();
        return att.containsKey(key) ? att.get(key).get(0) : "";
    }


    /**
     * 1. Find the user by id <br>
     * 2. Set disable to the user <br>
     * 3. Update the user <br>
     * <p>
     * * @param tenant Represent the related tenant of the user
     * * @param id Represent the id of the user
     */
    public Object diActivateUser(String tenant, String id) {
        var kUser = keycloakService.getUserById(tenant, id);

        if (kUser == null)
            throw new HttpCustomException(HttpStatus.NOT_FOUND.value(), ErrorCode.USER_NOT_FOUND.getMessage());

        kUser.setEnabled(false);
        keycloakService.updateUser(tenant, kUser);

        return null;
    }

    /**
     * 1. Delete the user from keycloak <br>
     * 2. Delete the user from db <br>
     *
     * @param tenant Represent the related tenant of the user
     * @param id     Represent the id of the user
     */
    public Object delete(String tenant, String id) {
        var response = keycloakService.deleteUserById(tenant, id);
        userAccountService.deleteAndPublishEvent(UUID.fromString(id));
        return response;
    }
}
