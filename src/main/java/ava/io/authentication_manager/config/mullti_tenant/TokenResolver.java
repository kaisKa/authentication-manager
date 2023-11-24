package ava.io.authentication_manager.config.mullti_tenant;

import com.nimbusds.jwt.JWTParser;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import java.util.Map;

import static org.springframework.security.oauth2.jwt.JwtClaimNames.SUB;

public class TokenResolver {

    private static final String TENANT = "tenant_id";

    public static Map<String, String> resolve() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AbstractOAuth2TokenAuthenticationToken) {
            AbstractOAuth2TokenAuthenticationToken bearer = (AbstractOAuth2TokenAuthenticationToken) authentication;
            return bearer.getTokenAttributes();
        }
        return Map.of();

    }

    @SneakyThrows
    public static String resolveTenant(Jwt jwt) {
        String tenant = jwt.getClaim(TENANT);
//        String tenant  = JWTParser.parse(jwt.getTokenValue()).getJWTClaimsSet().getStringClaim(TENANT);
        return !tenant.isEmpty() ? tenant : "all";
    }

//    public static String resolveTenant() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication instanceof AbstractOAuth2TokenAuthenticationToken) {
//            AbstractOAuth2TokenAuthenticationToken bearer = (AbstractOAuth2TokenAuthenticationToken) authentication;
//            return (String) bearer.getTokenAttributes().get("tenant_id");
//        }
//        return "all";
////        String tenant  = JWTParser.parse(jwt.getTokenValue()).getJWTClaimsSet().getStringClaim(TENANT);
////        return !tenant.isEmpty() ? tenant : "all";
//    }

    public static String resolveId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AbstractOAuth2TokenAuthenticationToken) {
            AbstractOAuth2TokenAuthenticationToken bearer = (AbstractOAuth2TokenAuthenticationToken) authentication;
            return (String) bearer.getTokenAttributes().get("sub");
        }
        return "";
    }

    @SneakyThrows
    public static String resolveId(Jwt jwt) {
        String id  = JWTParser.parse(jwt.getTokenValue()).getJWTClaimsSet().getStringClaim(SUB);
        return !id.isEmpty() ? id : "";
    }

//    public String getTenant(AuthenticationPrincipal principal) {
//        principa
//        JWTParser.parse(principal.resolve(request))
//                .getJWTClaimsSet()
//                .getStringClaim(ISS)
//    }
}