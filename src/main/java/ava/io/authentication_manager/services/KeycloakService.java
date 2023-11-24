package ava.io.authentication_manager.services;

import ava.io.authentication_manager.config.PathBasedConfigResolver1;
import ava.io.authentication_manager.config.mullti_tenant.TenantResolver;
import ava.io.authentication_manager.dtos.UserCredentials;
import ava.io.authentication_manager.dtos.UserDto;
import ava.io.authentication_manager.enums.Channel;
import ava.io.authentication_manager.enums.Role;
import ava.io.authentication_manager.model.Credentials;
import ava.io.authentication_manager.utils.ErrorCode;
import ava.io.authentication_manager.utils.Helper;
import ava.io.authentication_manager.utils.custom_excpeption.UserAlreadyExistedException;
import ava.io.authentication_manager.utils.custom_excpeption.UserNotFoundException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import javax.security.auth.login.CredentialException;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KeycloakService {

    @Autowired
    private PathBasedConfigResolver1 config;

    @Autowired
    private TenantResolver tenantResolver;


    @Autowired
    @Qualifier("notSecureRestTemplate")
    private RestTemplate restTemplate;

    @Value("${kcloak.config.token}")
    private String TOKEN_URL;


    //********************** keycloak functionalities **************************//

    public RealmResource getRealmResource(String tenant) {
        return config.getRealmResource(tenant);
    }


    /**
     * Create A user on keycloak
     * assign the passed role
     *
     * @param userDto contains all user needed fields
     * @return Response
     */
    @SneakyThrows
    @Transactional()
    public Response createKeycloakUser(String tenant, UserDto userDto, List<Role> roles, Map<String, List<String>> attributes) {
        CredentialRepresentation credential = Credentials.createPasswordCredentials(userDto.getPassword());
        UserRepresentation user = new UserRepresentation();
        user.setFirstName(userDto.getFirstName());
        user.setEmail(userDto.getEmailId());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUserName());
        user.setAttributes(attributes);
        user.setCredentials(Collections.singletonList(credential));


        var realmResource = getRealmResource(tenant);

        //check if this phone already registered
        var alreadyExist = getUserByPhoneNum(tenant, attributes.get("gsm").get(0));
        if (alreadyExist != null) {
            throw new UserAlreadyExistedException(ErrorCode.EXITED_USER.getMessage());
        }

        var response = realmResource.users().create(user);
//        if (response.getStatus() != HttpStatus.CREATED.value()) {
//            throw new HttpCustomException(response.getStatus(), response.getStatusInfo().getReasonPhrase());
//        }
//        log.info("{} User has been add to keycloak ", role.getValue());
        var userId = getId(response);
        var userResource = realmResource.users().get(userId);

        ClientRepresentation clientRepresentation = realmResource.clients().findAll()
                .stream().filter(client -> client.getClientId().equals(tenantResolver.getTenant(tenant).getResource())).collect(Collectors.toList())
                .get(0);
        ClientResource clientResource = realmResource.clients().get(clientRepresentation.getId());

        //get the client roles that map the roles in passed array
        List<RoleRepresentation> clientRole = clientResource.roles().list()
                .stream().filter(roleRepresentation ->
                        roles.stream().anyMatch(r -> r.getValue().equalsIgnoreCase(roleRepresentation.getName())))
                .collect(Collectors.toList());


        //assign the user to a group


        userResource.roles()
                .clientLevel(clientRepresentation.getId())
                .add(clientRole);//Collections.singletonList(clientRole));

//        log.info("Role {}  has been assigned to user with the id: {} ", role.getValue(), userId);

        return response;

    }


    public void updateUser(String tenant, UserRepresentation user) {
        getRealmResource(tenant).users().get(user.getId()).update(user);
    }

    public void EnableUser(String tenant, String identifier, Channel channel) {

        List<UserRepresentation> users = channel == Channel.phone ? getRealmResource(tenant).users().searchByAttributes(Helper.GSM + ":" + identifier)
                : getRealmResource(tenant).users().searchByAttributes(Helper.EMAIL + ":" + identifier); //searchByEmail(identifier, true);

        if (users.size() > 0) {
            UserResource userResource = getRealmResource(tenant).users().get(users.get(0).getId());
            UserRepresentation user = userResource.toRepresentation();
            user.setEnabled(true);

            if (channel == Channel.email)
                user.setEmailVerified(true);
//            userResource.update(user);

            //update is verified
            var updatedUser = setAtt(user, Helper.IS_VERIFIED, "true");
            //update the user
            updateUser(tenant, updatedUser);
        }
    }


    public void deleteKeycloakUser(String tenant, String userId) {
        getRealmResource(tenant).users().get(userId).remove();
    }

//    @SneakyThrows
//    public String getAuthorizationCode(String tenant) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
//        queryParams.add("client_id", config.getCache().get(tenant).getResourceName());
//        queryParams.add("redirect_uri", config.getCache().get(tenant).getAuthServerBaseUrl() + "/*");
//        queryParams.add("response_type", "code");
//        queryParams.add("scope", "openid");
//
//        // append  query param to the url
//        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(con.getRoutes().authorization_code)
//                .queryParams(queryParams);
//
//        val entity = new HttpEntity<>(headers);
//
//
//        var res = restTemplate.exchange(uriBuilder.toUriString(),
//                HttpMethod.GET,
//                entity,
//                String.class
//        ).getBody();
//        return extractAuthorizationCode(res);
//
//
//    }


    private String extractAuthorizationCode(String response) {
        String codeParam = response.substring(response.indexOf("code=") + 5);
        return codeParam.split("&")[0];
    }


    @SneakyThrows
    public AccessTokenResponse getAccessToken(String tenant, UserCredentials userCredentials) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", userCredentials.getUsername());
        map.add("password", userCredentials.getPassword());
        map.add("grant_type", "password");
        map.add("client_id", tenantResolver.getTenant(tenant).getResource());
        map.add("scope", "openid");
        map.add("client_secret", tenantResolver.getTenant(tenant).getResourceSecret());

        val entity = new HttpEntity<>(map, headers);

        var res = restTemplate.exchange(new URI(TOKEN_URL.replace("####", tenant)),
                HttpMethod.POST,
                entity,
                AccessTokenResponse.class
        );
        return res.getBody();


    }


    public AccessTokenResponse refreshAccessToken(String tenant, String refreshToken) throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("refresh_token", refreshToken);
        map.add("grant_type", "refresh_token");
        map.add("client_id", tenantResolver.getTenant(tenant).getResource());
        map.add("client_secret", tenantResolver.getTenant(tenant).getResourceSecret());


        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        return restTemplate.exchange(new URI(TOKEN_URL.replace("####", tenant)),
                HttpMethod.POST,
                entity,
                AccessTokenResponse.class
        ).getBody();
    }

//    public void logout(String tenant, String refreshToken) {
//
//        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
//        map.add("client_id", config.getCache().get(tenant).getResourceName());
//        map.add("client_secret", config.getCache().get(tenant).getResourceCredentials().get("secret").toString());
//        map.add("refresh_token", refreshToken);
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, null);
//        restTemplate.postForObject(keyCloakConfig.getRoutes().LOGOUT, request, String.class);
//
//    }


//    public List<UserRepresentation> getUserInfo(String username) {
//        List<UserRepresentation> res = getUserResource().searchByUsername(username, true);
//        return res;
//    }

    public UserRepresentation getUserInfo(String tenant, String id) {
        UserRepresentation res;
        try {
            res = getRealmResource(tenant).users().get(id).toRepresentation();
        } catch (NotFoundException e) {
            log.error("\n**************** CATCHING ERROR ******************\n");
            e.printStackTrace();
            log.error("\n**************** END OF ERROR STACKTRACE ******************\n");
            throw e;
        } catch (Exception e) {

            log.error("\n**************** CATCHING ERROR FROM GENERAL ******************\n");
            e.printStackTrace();
            log.error("\n**************** END OF ERROR STACKTRACE ******************\n");
            throw e;
        }
        return res;
    }

    public Map<String, String> getUsernameByPhoneNum(String tenant, String phone) {
        RealmResource realmResource = getRealmResource(tenant);//get realm source
        UsersResource usersResource = realmResource.users();//get users resource

        List<UserRepresentation> users = usersResource.searchByAttributes(Helper.GSM + ":" + phone);


        if (users.isEmpty())
            throw new UserNotFoundException();

        return Map.of("username", users.get(0).getUsername());

    }

    @SneakyThrows
    public UserRepresentation getUserByPhoneNum(String tenant, String phone) {
        RealmResource realmResource = getRealmResource(tenant);//get realm source
        var usersResource = realmResource.users();//get users resource
        var users = usersResource.searchByAttributes(Helper.GSM + ":" + phone);//Map.of("gsm", phone).toString()
        return !users.isEmpty() ? users.get(0) : null;
    }


    @SneakyThrows
    public UserRepresentation getUserByEmail(String tenant, String email) {
        var realmResource = getRealmResource(tenant);//get realm source
        var usersResource = realmResource.users();//get users resource
        var users = usersResource.searchByAttributes("email:" + email); //.searchByEmail(email, true);
        if (!users.isEmpty()) {
            return users.get(0);
        }
        throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @SneakyThrows
    public UserRepresentation getUserByUserName(String tenant, String userName) {
        UsersResource usersResource = getRealmResource(tenant).users();//get users resource
        var users = usersResource.searchByAttributes("username:" + userName); //.searchByUsername(userName, true);
        if (users.isEmpty())
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());

        return users.get(0);


    }

    @SneakyThrows
    public Map<String, String> getUserNameByEmail(String tenant, String email) {
        var realmResource = getRealmResource(tenant);//get realm source
        var usersResource = realmResource.users();//get users resource
        var users = usersResource.searchByAttributes(Helper.EMAIL + ":" + email); //.searchByEmail(email, true);
        if (!users.isEmpty()) {
            return Map.of("username", users.get(0).getUsername());
        }
        throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    public UserRepresentation getUserById(String tenant, String id) {
        return config.getRealmResource(tenant).users().get(id).toRepresentation();
    }


//    @SneakyThrows
//    public void changePassword(String bearerToken, UUID userId, ResetPasswordRequest resetPasswordRequest) {
//
//        getUserResource()
//                .get(userId.toString())
//                .resetPassword(Credentials.createPasswordCredentials(resetPasswordRequest.getNewPassword()));
//
//
//    }

    /**
     * 1. get keyclaok user by id
     * 2. use its username and the passed password to be validated
     * 3. use get access token api trying to authenticate i
     *
     * @param keycloakId keycloak user  id
     * @param password   user password
     * @return Boolean
     */
    @SneakyThrows
    public Boolean validateOldPassword(String tenent, String keycloakId, String password) {
        var user = getRealmResource(tenent).users().get(keycloakId).toRepresentation();
        if (user == null)
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
        try {
            AccessTokenResponse res = getAccessToken(tenent, new UserCredentials(user.getUsername(), password));
            if (res.getError() == null)
                return true;

        } catch (Exception e) {
            throw new CredentialException(ErrorCode.WRONG_PASSWORD.getMessage());
        }
        return false;
    }

    @SneakyThrows
    public void resetPassword(String tenant, String keycloakId, String newPassword) {
        getRealmResource(tenant)
                .users()
                .get(keycloakId)
                .resetPassword(Credentials.createPasswordCredentials(newPassword));
    }


//    public boolean validateToken(String tenant, String accessToken) {
//        try {
//            // Verify the token and retrieve the security context
//            AccessToken context = AdapterTokenVerifier.verifyToken(accessToken, config.getKeycloakdeployment(tenant));
//
//            // You can perform additional checks or retrieve token information as needed
//            // For example, get the user's subject (ID) from the token:
//            String userId = context.getSubject();
//
//            return true; // Token is valid
//        } catch (VerificationException e) {
//            // Token validation failed
//            e.printStackTrace();
//            return false;
//        }
//    }

    public void logout(String tenant, String userId) {

        UserResource kUser = getRealmResource(tenant).users().get(userId);

        if (kUser == null)
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());

        kUser.logout();
    }


    //********************** Realm fun ***********************//

    /**
     * 1. create a realm rep
     * 2. create a client rep
     * 3. set client to that realm
     * 4.
     *
     * @param realmName
     * @return
     */
    @SneakyThrows
    public Object dynamicCreateRealm(String realmName) {

        Keycloak keycloak = config.getKeycloak("master");

        RealmRepresentation realm = new RealmRepresentation();
        //realm rep
        realm.setId(realmName);
        realm.setRealm(realmName);
        realm.setAccessTokenLifespan(600);
        realm.setEnabled(true);
        realm.setSslRequired("external");
        realm.setBruteForceProtected(true);
        realm.setEventsEnabled(false);
        realm.setAdminEventsEnabled(false);

        keycloak.realms().create(realm);

        //get the created realm
        RealmResource newRealm = keycloak.realms().realm(realmName);
        // create a role a admin

        //get the admin role from the admin realm
        RoleResource adminRole = config.getRealmResource("master").roles().get("admin");
        RoleRepresentation adminRoleRep = adminRole.toRepresentation();
        Set<RoleRepresentation> composite = adminRole.getRealmRoleComposites();

        adminRoleRep.setComposite(true);
        adminRoleRep.setComposites(new RoleRepresentation.Composites());

        newRealm.roles().create(adminRoleRep);

        var newRoleRes = newRealm.roles().get("admin");
        newRoleRes.addComposites(composite.stream().collect(Collectors.toList()));
        // create client

        ClientRepresentation client = new ClientRepresentation();
        client.setId(realmName + "-app");
        client.setClientId(realmName + "-app");
        client.setRedirectUris(Arrays.asList("http://localhost:8080/*"));
        client.setPublicClient(false);
        client.setSecret(Helper.generateSecret());
//        client.setAuthorizationServicesEnabled(true);
        client.setEnabled(true);
//        client.setDirectAccessGrantsEnabled(false);

        //set roles

        // enable client authentication

        newRealm.clients().create(client);


        // set the realm user
        var admin = new UserRepresentation();
        admin.setUsername("admin");
        admin.setCredentials(Collections.singletonList(Credentials.createPasswordCredentials("admin")));
        newRealm.users().create(admin); //TODO: u may need to check the response

//        admin.setClientRoles(client.getClientId());

        return "A new realm has been created";


        /*
        AccessTokenResponse admin = getAccessToken("master", new UserCredentials("admin", "admin"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(admin.getToken());

        RealmRepresentation newRealm = new Realm();
        newRealm.setId(realmName);
        newRealm.setRealm(realmName);
        newRealm.setAccessTokenLifespan(600);
        newRealm.setEnabled(true);
        newRealm.setSslRequired("external");
        newRealm.setBruteForceProtected(true);
        newRealm.setEventsEnabled(false);
        newRealm.setAdminEventsEnabled(false);
        newRealm.setOtp


        val entity = new HttpEntity<>(newRealm, headers);

        var res = restTemplate.exchange(new URI(config.getServerUrl()+"/admin/realms"),
                HttpMethod.POST,
                entity,
                Object.class
        );
        return res.getBody();*/


    }


    public void getRealm(String realmName) {

    }

    public void deleteRealm(String realmName) {

    }


    //********************** private fun ***********************//

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

    public String getId(Response response){
        return CreatedResponseUtil.getCreatedId(response);
    }

    public Object deleteUserById(String tenant, String id) {
        return  config.getRealmResource(tenant).users().delete(id);
    }


}


