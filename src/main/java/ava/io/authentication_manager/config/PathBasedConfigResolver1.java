package ava.io.authentication_manager.config;


import ava.io.authentication_manager.config.mullti_tenant.TenantResolver;
import ava.io.authentication_manager.db.entities.Tenant;
import lombok.Getter;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@ConditionalOnProperty(prefix = "kcloak.config", name = "resolver", havingValue = "path")
public class PathBasedConfigResolver1 {


    //TODO check if it is better to add filter
    @Autowired
    private TenantResolver tenantResolver;
    public final Map<String, Keycloak> keycloakCache = new ConcurrentHashMap<>();


    @Value("${kcloak.auth-server-url}")
    private String serverUrl;
    @Value("${kcloak.config.userName}")
    private String userName;
    @Value("${kcloak.config.password}")
    private String password;
    @Value("${kcloak.config.resteasy_pool_size}")
    private int resteasy_pool_size;


    public Keycloak getKeycloak(String tenant){
        Tenant tenant1 = tenantResolver.getTenant(tenant);
        if (keycloakCache.get(tenant) == null) {
            keycloakCache.put(tenant1.getName(), KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(tenant1.getName())
                    .grantType(OAuth2Constants.PASSWORD)
                    .clientId(tenant1.getResource())
                    .clientSecret(tenant1.getResourceSecret())
                    .username(userName)
                    .password(password)
                    .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(resteasy_pool_size).build())
                    .build());
        }
        return keycloakCache.get(tenant);
    }

    public RealmResource getRealmResource(String tenant) {
        return getKeycloak(tenant).realm(tenant);
    }





}
