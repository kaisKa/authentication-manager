package ava.io.authentication_manager.config.mullti_tenant;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jwt.JWTParser;

import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;


@Component
public class TenantAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {
    private final BearerTokenResolver resolver = new DefaultBearerTokenResolver();

    private final ConcurrentHashMap<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();

    private TenantResolver tenantResolver;

    public TenantAuthenticationManagerResolver(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        String issuer = toTenant(request);
        if (isTrustedIssuer(issuer)) {  // TODO:
            return this.authenticationManagers.computeIfAbsent(issuer, this::fromTenant);
        } else {
            throw new InvalidBearerTokenException(String.format("Untrusted issuer %s", issuer));
        }
    }

    private boolean isTrustedIssuer(String issuer) {
        return tenantResolver.tenantsCache.containsKey(issuer);
    }

    private String toTenant(HttpServletRequest request) {
        try {
            String token = this.resolver.resolve(request);
            var t = JWTParser.parse(token).getJWTClaimsSet();
            return t.getClaim("tenant_id").toString();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private AuthenticationManager fromTenant(String tenant) {
        return Optional.ofNullable(tenantResolver.getIssuer(tenant))
//                .map(iss -> new NimbusJwtDecoder(configureJwksCache(iss)))
                .map(JwtDecoders::fromIssuerLocation)
                .map(t -> new JwtAuthenticationProvider((JwtDecoder) t)).map(jwtAuthenticationProvider -> {
                    jwtAuthenticationProvider.setJwtAuthenticationConverter(getJwtAuthenticationConverter());
                    return jwtAuthenticationProvider;
                })
                .orElseThrow(() -> new IllegalArgumentException("unknown tenant"))::authenticate;
    }


    public Converter<Jwt, AbstractAuthenticationToken> getJwtAuthenticationConverter() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(new KeycloakJwtRolesConverter());
        return conv;
    }

    DefaultJWTProcessor configureJwksCache(String jwkSetUri) {
        var ttl = Duration.ofMinutes(30).toMinutes();
        var cache = Duration.ofMinutes(15).toMinutes();
        try {
            var jwkSetCache =
                    new DefaultJWKSetCache(
                            ttl,
                            cache,
                            TimeUnit.MINUTES);
            var jwsKeySelector =
                    JWSAlgorithmFamilyJWSKeySelector.fromJWKSource(
                            new RemoteJWKSet<>(new URL(jwkSetUri), null, jwkSetCache));

            var jwtProcessor = new DefaultJWTProcessor();
            jwtProcessor.setJWSKeySelector(jwsKeySelector);
            return jwtProcessor;
        } catch (KeySourceException | MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}