package ava.io.authentication_manager.config.mullti_tenant;

import java.net.URL;
import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;


@NoArgsConstructor
@Component
public class TenantJWSKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {
    private final Map<String, JWSKeySelector<SecurityContext>> selectors = new HashMap<>();

    private TenantResolver tenantResolver;

    public TenantJWSKeySelector(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public List<? extends Key> selectKeys(JWSHeader jwsHeader, JWTClaimsSet jwtClaimsSet, SecurityContext securityContext)
            throws KeySourceException {
        return this.selectors.computeIfAbsent(toTenant(jwtClaimsSet), this::fromTenant)
                .selectJWSKeys(jwsHeader, securityContext);
    }

    private String toTenant(JWTClaimsSet claimSet) {
        return (String) claimSet.getClaim("tenant_id");
    }

    private JWSKeySelector<SecurityContext> fromTenant(String tenant) {
        return Optional.ofNullable(tenantResolver.getIssuer(tenant))
                .map(this::fromUri)
                .orElseThrow(() -> new IllegalArgumentException("unknown tenant"));
    }

    private JWSKeySelector<SecurityContext> fromUri(String uri) {
        try {
            return JWSAlgorithmFamilyJWSKeySelector.fromJWKSetURL(new URL(uri));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}