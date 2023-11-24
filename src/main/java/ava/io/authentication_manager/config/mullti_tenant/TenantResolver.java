package ava.io.authentication_manager.config.mullti_tenant;

import ava.io.authentication_manager.db.entities.Tenant;
import ava.io.authentication_manager.services.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@DependsOn("tenantService")
public class TenantResolver {

    public static Map<String, Tenant> tenantsCache = null;


    private TenantService tenantService;


    public TenantResolver(TenantService tenantService) {
        this.tenantService = tenantService;
        TenantResolver.tenantsCache = tenantService.mapAllTenant();
        log.info("all tenant have been loaded to tha cache map");
    }

    public String getIssuer(String tenant) {
        if (!tenantsCache.containsKey(tenant)) {
            Tenant t = tenantService.getTenantByName(tenant);
            tenantsCache.put(tenant, t); //TODO here is the last change
        }
        return tenantsCache.get(tenant).getIssuerUrl();

    }


    public Tenant getTenant(String tenant) {
        if (!tenantsCache.containsKey(tenant)) {
            Tenant t = tenantService.getTenantByName(tenant);
            tenantsCache.put(tenant, t); //TODO here is the last change
        }
        return tenantsCache.get(tenant);

    }

    public Map<String, Tenant> getAllIssuer(String typ) {
        return tenantService.mapAllTenant();//tenantFiegn.getAllIssuer(typ).getBody();
    }


}
