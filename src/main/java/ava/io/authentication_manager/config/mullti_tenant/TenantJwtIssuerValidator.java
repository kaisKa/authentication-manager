
package ava.io.authentication_manager.config.mullti_tenant;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
public class TenantJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {
    private final Map<String, JwtIssuerValidator> validators = new HashMap<>();

    private TenantResolver tenantResolver;

    public TenantJwtIssuerValidator(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        return this.validators.computeIfAbsent(toTenant(token), this::fromTenant)
                .validate(token);
    }

    private String toTenant(Jwt jwt) {
        return jwt.getClaim("tenant_id");
    }

    private JwtIssuerValidator fromTenant(String tenant) {
        return Optional.ofNullable(tenantResolver.getIssuer(tenant))
                .map(JwtIssuerValidator::new)
                .orElseThrow(() -> new IllegalArgumentException("unknown tenant"));
    }


}