package ava.io.authentication_manager.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Realm {

    private String id = "heroes";
    private String realm = "heroes";
    private int notBefore = 0;
    private boolean revokeRefreshToken = false;
    private int refreshTokenMaxReuse = 0;
    private int accessTokenLifespan = 300;
    private int accessTokenLifespanForImplicitFlow = 900;
    private int ssoSessionIdleTimeout = 1800;
    private int ssoSessionMaxLifespan = 36000;
    private int ssoSessionIdleTimeoutRememberMe = 0;
    private int ssoSessionMaxLifespanRememberMe = 0;
    private int offlineSessionIdleTimeout = 2592000;
    private boolean offlineSessionMaxLifespanEnabled = false;
    private int offlineSessionMaxLifespan = 5184000;
    private int clientSessionIdleTimeout = 0;
    private int clientSessionMaxLifespan = 0;
    private int accessCodeLifespan = 60;
    private int accessCodeLifespanUserAction = 300;
    private int accessCodeLifespanLogin = 1800;
    private int actionTokenGeneratedByAdminLifespan = 43200;
    private int actionTokenGeneratedByUserLifespan = 300;
    private boolean enabled = true;
    private String sslRequired = "external";
    private boolean registrationAllowed = false;
    private boolean registrationEmailAsUsername = false;
    private boolean rememberMe = false;
    private boolean verifyEmail = false;
    private boolean loginWithEmailAllowed = true;
    private boolean duplicateEmailsAllowed = false;
    private boolean resetPasswordAllowed = false;
    private boolean editUsernameAllowed = false;
    private boolean bruteForceProtected = false;
    private boolean permanentLockout = false;
    private int maxFailureWaitSeconds = 900;
    private int minimumQuickLoginWaitSeconds = 60;
    private int waitIncrementSeconds = 60;
    private int quickLoginCheckMilliSeconds = 1000;
    private int maxDeltaTimeSeconds = 43200;
    private int failureFactor = 30;

    private List<String> defaultRoles = List.of("offline_access","uma_authorization");
    private List<String> requiredCredentials = List.of("password");

    private String otpPolicyType = "totp";
    private String otpPolicyAlgorithm = "HmacSHA1";
    private int otpPolicyInitialCounter = 0;
    private int otpPolicyDigits = 6;
    private int otpPolicyLookAheadWindow = 1;
    private int otpPolicyPeriod = 30;

    private List<String> otpSupportedApplications = List.of("FreeOTP", "Google Authenticator");
    private String webAuthnPolicyRpEntityName = "keycloak";

    private List<String> webAuthnPolicySignatureAlgorithms = List.of("ES256");

    private String webAuthnPolicyRpId =  "";
    private String webAuthnPolicyAttestationConveyancePreference =  "not specified";
    private String webAuthnPolicyAuthenticatorAttachment =  "not specified";
    private String webAuthnPolicyRequireResidentKey =  "not specified";
    private String webAuthnPolicyUserVerificationRequirement =  "not specified";
    private int webAuthnPolicyCreateTimeout =  0;
    private boolean webAuthnPolicyAvoidSameAuthenticatorRegister =  false;
    private List<String> webAuthnPolicyAcceptableAaguids =  List.of();
    private String webAuthnPolicyPasswordlessRpEntityName =  "keycloak";

    private List<String> webAuthnPolicyPasswordlessSignatureAlgorithms = List.of( "ES256");

    private String webAuthnPolicyPasswordlessRpId =  "";
    private String webAuthnPolicyPasswordlessAttestationConveyancePreference =  "not specified";
    private String webAuthnPolicyPasswordlessAuthenticatorAttachment =  "not specified";
    private String webAuthnPolicyPasswordlessRequireResidentKey =  "not specified";
    private String webAuthnPolicyPasswordlessUserVerificationRequirement =  "not specified";
    private int webAuthnPolicyPasswordlessCreateTimeout =  0;
    private boolean webAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister =  false;

    private List<String> webAuthnPolicyPasswordlessAcceptableAaguids = List.of();

//    private BrowserSecurityHeaders browserSecurityHeaders = new BrowserSecurityHeaders();
    private JsonNode smtpServer =  null;


    private boolean eventsEnabled =  false;
    private List<String> eventsListeners =  List.of();
    private List<String> enabledEventTypes =  List.of();
    private boolean adminEventsEnabled =  false;
    private boolean adminEventsDetailsEnabled =  false;
    private List<IdentityProvider> identityProviders = List.of();


    private List<IdentityProviderMapper> identityProviderMappers = List.of();






}

class BrowserSecurityHeaders {
    private String contentSecurityPolicyReportOnly =  "";
    private String xContentTypeOptions =  "nosniff";
    private String xRobotsTag =  "none";
    private String xFrameOptions =  "SAMEORIGIN";
    private String contentSecurityPolicy =  "frame-src 'self'; frame-ancestors 'self'; object-src 'none';";
    private String xXSSProtection =  "1; mode=block";
    private String strictTransportSecurity =  "max-age=31536000; includeSubDomains";
}

class IdentityProvider{

    private String alias =  "keycloak-oidc";
    private String internalId =  "d79d0d65-8ee1-47f0-8611-f9e6eea71f20";
    private String providerId =  "keycloak-oidc";
    private boolean enabled =  true;
    private String updateProfileFirstLoginMode =  "on";
    private boolean trustEmail =  false;
    private boolean storeToken =  false;
    private boolean addReadTokenRoleOnCreate =  false;
    private boolean authenticateByDefault =  false;
    private boolean linkOnly =  false;
    private String firstBrokerLoginFlowAlias =  "first broker login";

    private Config config;

}

class Config{
    private String clientId = "ssss";
    private String tokenUrl = "http://localhost";
    private String authorizationUrl = "http://localhost";
    private String clientAuthMethod = "client_secret_basic";
    private String syncMode = "IMPORT";
    private String clientSecret = "assaasa";
    private String useJwksUrl = "true";
}

 class IdentityProviderMapper{
     private String id =  "42c7b62d-4383-42c9-a8a0-65519e2c2543";
     private String name =  "test-mapper";
     private String identityProviderAlias =  "keycloak-oidc2";
     private String identityProviderMapper =  "keycloak-oidc";
     private JsonNode config ;
 }
