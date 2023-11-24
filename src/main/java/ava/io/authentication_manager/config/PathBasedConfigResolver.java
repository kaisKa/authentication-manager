//package ava.io.authentication_manager.config;
//
//import ava.io.authentication_manager.config.mullti_tenant.TenantResolver;
//import ava.io.authentication_manager.db.entities.Tenant;
//import lombok.Getter;
//import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
//import org.keycloak.OAuth2Constants;
//import org.keycloak.adapters.KeycloakConfigResolver;
//import org.keycloak.adapters.KeycloakDeployment;
//import org.keycloak.adapters.spi.HttpFacade;
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.admin.client.KeycloakBuilder;
//import org.keycloak.admin.client.resource.RealmResource;
//import org.keycloak.representations.adapters.config.AdapterConfig;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.DependsOn;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Getter
//@ConditionalOnProperty(prefix = "kcloak.config", name = "resolver", havingValue = "path")
//@DependsOn("tenantService")
//public class PathBasedConfigResolver implements KeycloakConfigResolver {
//    @Autowired
//    private TenantResolver tenantResolver;
//    private final Map<String, KeycloakDeployment> cache = new ConcurrentHashMap<>();
//
//    public  final Map<String, Keycloak> keycloakCache = new ConcurrentHashMap<>();
//    private final static String TENANT = "tenant";
//
//    @Value("${kcloak.auth-server-url}")
//    private String serverUrl;
//    @Value("${kcloak.config.userName}")
//    private String userName;
//    @Value("${kcloak.config.password}")
//    private String password;
//    @Value("${kcloak.config.resteasy_pool_size}")
//    private int resteasy_pool_size;
//
//
//
//    @Override
//    public KeycloakDeployment resolve(HttpFacade.Request request) {
//        String path = request.getURI();
//        int multitenantIndex = path.indexOf("tenant/") ;
//
//        if (multitenantIndex == -1) {
////            throw new IllegalStateException("Not able to resolve realm from the request path!");
//            return new KeycloakDeployment();
//        }
//
//        String realm = path.substring(path.indexOf("tenant/")).split("/")[1];
//        if (realm.contains("?")) {
//            realm = realm.split("\\?")[0];
//        }
//
//        if (!cache.containsKey(realm)) {
//            // we can store this in the dataBase
//            //_________________--//
////            InputStream is = getClass().getResourceAsStream("/realms/" + realm + "-keycloak.json");
//            KeycloakDeployment kd = new KeycloakDeployment(); //KeycloakDeploymentBuilder.build(is);
//            var  c = new AdapterConfig();
//            Tenant tenant = tenantResolver.getTenant(realm);
//            c.setAuthServerUrl(serverUrl);
//            kd.setAuthServerBaseUrl(c);
//            kd.setRealm(tenant.getName());
//            kd.setResourceName(tenant.getResource());
//            kd.setResourceCredentials(Map.of("secret",tenant.getResourceSecret()));
//            kd.setBearerOnly(true);
//              //________________-//
//
//
//            // get the tenant object from db or cash
//
//
//
//            cache.put(realm,kd);
//
//            // create the keycloak instance
//            if (keycloakCache.get(TENANT) == null) {
//
//                keycloakCache.put(tenant.getName(), KeycloakBuilder.builder()
//                        .serverUrl(serverUrl)
//                        .realm( tenant.getName())
//                        .grantType(OAuth2Constants.PASSWORD)
//                        .clientId(tenant.getResource())
//                        .clientSecret(tenant.getResourceSecret())
//                        .username(userName)
//                        .password(password)
//                        .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(resteasy_pool_size).build())
//                        .build());
//            }
//        }
//
//        return cache.get(realm);
//    }
//
//
//    public RealmResource getRealmResource(String tenant) {
//        Tenant tenant1 = tenantResolver.getTenant(tenant);
////        if(cache.containsKey(tenant)){
//            if (keycloakCache.get(tenant) == null) {
//                keycloakCache.put(tenant1.getName(), KeycloakBuilder.builder()
//                        .serverUrl(serverUrl)
//                        .realm(tenant1.getName())
//                        .grantType(OAuth2Constants.PASSWORD)
//                        .clientId(tenant1.getResource())
//                        .clientSecret(tenant1.getResourceSecret())
//                        .username(userName)
//                        .password(password)
//                        .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(resteasy_pool_size).build())
//                        .build());
//            }
//            return keycloakCache.get(tenant).realm(tenant);
////        }
////       throw new BadRequestException(ErrorCode.UN_TRUSTED_ISSUER.getMessage());
//    }
//
//    public KeycloakDeployment getKeycloakdeployment(String tenant){
//        return cache.get(tenant);
//    }
//}
