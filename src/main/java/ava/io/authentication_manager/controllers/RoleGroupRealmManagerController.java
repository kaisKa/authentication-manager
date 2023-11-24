package ava.io.authentication_manager.controllers;


import ava.io.authentication_manager.dtos.UserDto;
import ava.io.authentication_manager.model.GeneralResponse;
import ava.io.authentication_manager.services.KeycloakService;
import ava.io.authentication_manager.services.RoleGroupRealmManagerService;
import ava.io.authentication_manager.services.TenantService;
import ava.io.authentication_manager.utils.ErrorCode;
import io.swagger.v3.oas.annotations.Hidden;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
@RestController
public class RoleGroupRealmManagerController implements RoleGroupManager {


    private RoleGroupRealmManagerService service;

    private TenantService tenantService;

    private KeycloakService keycloakService;

    public RoleGroupRealmManagerController(RoleGroupRealmManagerService service, TenantService tenantService, KeycloakService keycloakService) {
        this.service = service;
        this.tenantService = tenantService;
        this.keycloakService = keycloakService;
    }

    @Override
    public ResponseEntity<GeneralResponse<List<GroupRepresentation>>> getGroups(String tenant) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.getGroups(tenant)));
    }

    @Override
    public ResponseEntity<GeneralResponse<GroupRepresentation>> getGroup(String tenant, String groupId) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.getGroup(tenant, groupId)));
    }

    @Override
    public ResponseEntity<GeneralResponse<List<RoleRepresentation>>> getGroupRoles(String tenant, String groupId) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.getGroupRoles(tenant, groupId)));
    }

    @Override
    public ResponseEntity<GeneralResponse<List<UserDto>>> getGroupUsers(String tenant, String groupId) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.getGroupUsers(tenant, groupId)));
    }

    @Override
    public ResponseEntity<GeneralResponse<List<String>>> getRoles(String tenant) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.getRoles(tenant)));
    }

    @Override
    @PostMapping("${spring.base_url}" + "/manage/realm")
    public ResponseEntity<GeneralResponse<String>> addRealm(@RequestParam String realmName) {
        keycloakService.dynamicCreateRealm(realmName);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(),null ));
    }

    @Override
    public ResponseEntity<GeneralResponse<String>> getIssuerUrl(String tenant) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), tenantService.getIssuer(tenant)));
    }

    @Override
    public ResponseEntity<GeneralResponse<Object>> getAllIssuerUrl(String type) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(),
                "list".equalsIgnoreCase(type) ? tenantService.getAllIssuer()
                        : tenantService.mapAllIssuer()));
    }

    @Override
    public ResponseEntity<GeneralResponse<Object>> createGroup(String tenant, String name) {
        return ResponseEntity.created(null).body(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(),
                service.createGroup(tenant, name)));
    }


}
