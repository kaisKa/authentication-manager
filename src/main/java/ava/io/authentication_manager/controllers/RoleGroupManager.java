package ava.io.authentication_manager.controllers;

import ava.io.authentication_manager.dtos.UserDto;
import ava.io.authentication_manager.model.GeneralResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("${spring.base_url}" + "/manage")
public interface RoleGroupManager {

    @GetMapping("/tenant/{tenant}/groups")
    @Operation(summary = "Get all groups")
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<GeneralResponse<List<GroupRepresentation>>> getGroups(@PathVariable String tenant);

    @GetMapping("/tenant/{tenant}/groups/{groupId}")
    @Operation(summary = "Get a group by its id")
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<GeneralResponse<GroupRepresentation>> getGroup(@PathVariable String tenant,@PathVariable String groupId);


    @GetMapping("/tenant/{tenant}/groups/roles/{groupId}")
    @Operation(summary = "Get the roles of a certain group, providing its id")
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<GeneralResponse<List<RoleRepresentation>>> getGroupRoles(@PathVariable String tenant, @PathVariable String groupId);

    @GetMapping("/tenant/{tenant}/groups/users/{groupId}")
    @Operation(summary = "Get all user in a certain group, providing its id")
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<GeneralResponse<List<UserDto>>> getGroupUsers(@PathVariable String tenant, @PathVariable String groupId);


    @GetMapping("/tenant/{tenant}/roles")
    @Operation(summary = "Get all roles")
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<GeneralResponse<List<String>>> getRoles(@PathVariable String tenant);


    @Operation(summary = "Add new realm to keycloak")
//    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<GeneralResponse<String>> addRealm(@RequestBody String realmName);

    @GetMapping("/tenant/{tenant}")
    @Operation(summary = "Get an issuer tenant ")
    ResponseEntity<GeneralResponse<String>> getIssuerUrl(@PathVariable String tenant);

    @GetMapping("/tenant")
    @Operation(summary = "Get All issuer tenant ")
    ResponseEntity<GeneralResponse<Object>> getAllIssuerUrl(@RequestParam String map);


    @PostMapping("/tenant/{tenant}/groups")
    @Operation(summary = "create new group in a tenant realm")
    ResponseEntity<GeneralResponse<Object>> createGroup(@PathVariable String tenant,@RequestParam String name);







}
