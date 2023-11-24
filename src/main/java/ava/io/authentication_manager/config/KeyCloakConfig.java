package ava.io.authentication_manager.config;


import ava.io.authentication_manager.config.mullti_tenant.KeycloakJwtRolesConverter;
import ava.io.authentication_manager.model.KeycloakRoutes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "kcloak")
public class KeyCloakConfig {
    private Map<String, Keycloak> instances = new HashMap<>();
    private String serverUrl;
    private KeycloakRoutes routes;



    @Bean
    public KeycloakJwtRolesConverter grantAuthoritiesConverter() {
        return new KeycloakJwtRolesConverter();
    }

    @Bean
    public PathBasedConfigResolver1 keycloakConfigResolver() {
        return new PathBasedConfigResolver1();
    }



//    @Bean
//    public NimbusJwtDecoder jwtDecoder() {
//        return NimbusJwtDecoder.withJwkSetUri(getRoutes().cert).build();
//    }



//public JwtDecoder getJwtDecoder() {
//    ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor<>();
//    jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(new KeycloakTenantJWSKeySelector(authenticationServerLocation));
//    NimbusJwtDecoder jwtDecoder = new NimbusJwtDecoder(jwtProcessor);
//    jwtDecoder.setClaimSetConverter(new KeycloakJwtClaimAdapter());
//    SetJwtDecoderValidators(jwtDecoder, audience);
//    return jwtDecoder;
//}
}


