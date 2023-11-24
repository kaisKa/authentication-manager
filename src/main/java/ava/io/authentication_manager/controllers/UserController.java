package ava.io.authentication_manager.controllers;

import ava.io.authentication_manager.dtos.UserDto;
import ava.io.authentication_manager.config.mullti_tenant.TokenResolver;
import ava.io.authentication_manager.model.GeneralResponse;
import ava.io.authentication_manager.services.UserService;
import ava.io.authentication_manager.utils.ErrorCode;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Hidden
@RestController
@RequestMapping("${spring.base_url}" + "/users")
//@Tag(name = "ava.io.authentication_manager.controllers", description = "keycloak-users")
public class UserController {

    @Autowired
    private UserService service;


    @GetMapping
    @Operation(summary = "Get all users")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GeneralResponse<List<UserDto>>> getUsers(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.getUsers(TokenResolver.resolveTenant(authentication))));
    }


    @GetMapping("{tenant}/{userId}")
    @Operation(summary = "Get user by its id")
    public ResponseEntity<GeneralResponse<UserDto>> getUser(@PathVariable String tenant, @PathVariable UUID userId) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.getUser(tenant, userId)));
    }

    @GetMapping("/by-phone/{gsm}")
    @Operation(summary = "Retrieve user by its phone")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GeneralResponse<UserDto>> getUserByPhoneNum(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication, @PathVariable String gsm) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.getUserByPhoneNum(TokenResolver.resolveTenant(authentication), gsm)));
    }

    @GetMapping("/by-email/{email}")
    @Operation(summary = "Retrieve user by its email")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GeneralResponse<UserDto>> getUserByEmail(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication, @PathVariable String email) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.getUserByEmail(TokenResolver.resolveTenant(authentication), email)));
    }

    @GetMapping("/role/{roleName}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Retrieve all users of a specific role")
    public ResponseEntity<GeneralResponse<List<UserDto>>> getUsersByRole(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication, @PathVariable String roleName) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.getUsersByRole(TokenResolver.resolveTenant(authentication), roleName)));
    }

    //    @GetMapping("/search/{keyword}")
//    @Operation(summary = "Search for user by keyword, this keyword will attempt to match phone and ref ")
//    public List<UserDTO> searchByAttribute(@PathVariable String keyword){
//        return service.searchByAttribute(keyword);
//    }
//    @GetMapping("/search")
//    @Operation(summary = "Search for user by keyword, this keyword could by any thing, that will be fetched from the system db not keycloak")
//    public ResponseEntity<GeneralResponse<List<UserDTO>>> searchByAttribute(@RequestParam(required = false) String keyword) {
//        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.searchByCriteria(keyword)));
//    }

    @GetMapping("/roles/{userId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get the roles of a user by its id")
    public ResponseEntity<GeneralResponse<List<RoleRepresentation>>> getUserRoles(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication,@PathVariable UUID userId) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.getUserRoles(TokenResolver.resolveTenant(authentication), userId.toString())));
    }

    @GetMapping("/groups/{userId}")
//    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get the group of a user by its id")
    public ResponseEntity<GeneralResponse<Object>> getUserGroups(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication, @PathVariable UUID userId) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.getUserGroup("provider", userId.toString())));
    }


    @PostMapping()
    @Operation(summary = "Create user with a specific role, add reference to the parent, for auto generated password, provide 'auto_generated' as the password value")
    public ResponseEntity<GeneralResponse<UserDto>> createAndAssignRoles(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication, @RequestBody UserDto userDTO) {
        return ResponseEntity.created(null).body(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.createAndAssignRoles(TokenResolver.resolveTenant(authentication), userDTO)));
    }

    @PostMapping("/{groupId}")
    @Operation(summary = " Create a user, Add the user to a group, Assign group roles to the user ")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GeneralResponse<UserDto>> createAndJoinGroup(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication, @PathVariable UUID groupId, @RequestBody UserDto userDTO) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.createAndJoinGroup(TokenResolver.resolveTenant(authentication), groupId.toString(), userDTO)));

    }


    @PostMapping("/{userId}/assign-role/{roleName}")
    @Operation(summary = "Assign a role to a certain user, providing the user & role ids")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GeneralResponse<RoleRepresentation>> assignRoleToUser(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication,@PathVariable UUID userId, @PathVariable String roleName) {
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), service.assignRoleToUser(TokenResolver.resolveTenant(authentication), userId.toString(), roleName)));

    }


    @PostMapping("/{userId}/join-group/{groupId}")
    @Operation(summary = "Add a user to a certain group, providing the user & group ids")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GeneralResponse<Object>> joinGroup(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication,@PathVariable UUID userId, @PathVariable UUID groupId) {
        service.joinGroup(TokenResolver.resolveTenant(authentication), userId.toString(), groupId.toString());
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.SUCCESSFUL.getMessage(), null));

    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by its id")
//    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GeneralResponse<UserDto>> delete(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication, @PathVariable String id) {
        var res = service.delete(TokenResolver.resolveTenant(authentication), id);
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.DELETE_USER.getMessage(), res));
    }



    @DeleteMapping("/tenant/{tenant}/{id}")
    @Operation(summary = "Delete user by its id")
    //@SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GeneralResponse<UserDto>> delete(@PathVariable String tenant, @PathVariable String id) {
        var res = service.delete(tenant, id);
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.DELETE_USER.getMessage(), res));
    }

    @DeleteMapping("phone/{phone}")
    @Operation(summary = "Delete user by phone")
//    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GeneralResponse<UserDto>> deleteByPhone(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication, @PathVariable String phone) {
        var res = service.deleteByPhone(TokenResolver.resolveTenant(authentication), phone);
        return ResponseEntity.ok(new GeneralResponse<>(false, ErrorCode.DELETE_USER.getMessage(), res));
    }

    @PostMapping("/{userId}/un-assign-role/{roleName}")
    @Operation(summary = "Un assign a role of a certain user, providing the user & role ids")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GeneralResponse<Object>> unAssignRoleOfUser(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication,@PathVariable UUID userId, @PathVariable String roleName) {
        service.unAssignRoleToUser(TokenResolver.resolveTenant(authentication),userId.toString(), roleName);
        return ResponseEntity.ok(new GeneralResponse<>(false, roleName + ErrorCode.ROLE_REMOVED.getMessage(),roleName));

    }


    /**
     * This will add a doctor to a clinic -> that is mean a super admin will be authenticated upon its tenant
     * And we need the user id from provider to do so
     * @return
     */
    @PostMapping("add-to-organization")
    @Operation(summary = "Authenticated super admin will users to the organization 'clinic")
    @SecurityRequirement(name = "Bearer Authentication")
    ResponseEntity<GeneralResponse<Object>> addUserToOrganization(@Parameter(hidden = true) @AuthenticationPrincipal Jwt authentication,@RequestParam String userId) {
        return ResponseEntity.ok(new GeneralResponse<>(false,ErrorCode.ADDED_USER_TO_ORG.getMessage(), service.addUserToOrganization(TokenResolver.resolveTenant(authentication),TokenResolver.resolveId(authentication),userId )));
    }
}
