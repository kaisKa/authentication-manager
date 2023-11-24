package ava.io.authentication_manager.db.repositories;

import ava.io.authentication_manager.db.entities.Tenant;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepo extends BaseRepository<Tenant> {
    Optional<Tenant> findByName(String name);


}
