package ava.io.authentication_manager.services;

import ava.io.authentication_manager.db.entities.Tenant;
import ava.io.authentication_manager.db.repositories.BaseRepository;
import ava.io.authentication_manager.db.repositories.TenantRepo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("tenantService")
public class TenantService extends BaseService<Tenant> {
    public Map<String, String> tenantsCache = new HashMap<>();

    private TenantRepo tenantRepo;

    public TenantService(BaseRepository<Tenant> baseRepository, TenantRepo tenantRepo) {
        super(baseRepository);
        this.tenantRepo = tenantRepo;
    }

    public List<Tenant> getAllTenant() {
        return tenantRepo.findAll();
    }

    public Tenant getTenantByName(String name) {
        Optional<Tenant> t = tenantRepo.findByName(name);
        return t.orElse(null);
    }


    public String getIssuerByName(String name) {
        var t = tenantRepo.findByName(name);
        return t.map(Tenant::getIssuerUrl).orElse(null);
    }


    public String getIssuer(String tenant) {
        if (!tenantsCache.containsKey(tenant)) {
            tenantsCache.put(tenant, getIssuerByName(tenant));
        }
        return tenantsCache.get(tenant);
    }


    public List<Tenant> getAllIssuer() {
        return tenantRepo.findAll();
    }


    public HashMap<String, String> mapAllIssuer() {
        return tenantRepo.findAll().stream()
                .collect(
                        Collectors
                                .toMap(
                                        Tenant::getName,
                                        Tenant::getIssuerUrl,
                                        (x, y)
                                                -> x + ", " + y,
                                        HashMap::new));
    }

    public Map<String, Tenant> mapAllTenant() {
        return tenantRepo.findAll().stream()
                .collect(
                        Collectors
                                .toMap(
                                        Tenant::getName,
                                        tenant -> tenant
                                ));
    }


}
