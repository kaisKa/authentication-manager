package ava.io.authentication_manager.services;

import ava.io.authentication_manager.config.mullti_tenant.TenantResolver;
import ava.io.authentication_manager.dtos.UserDto;
import ava.io.authentication_manager.dtos.mappers.UserRepresentationMapper;
import ava.io.authentication_manager.config.PathBasedConfigResolver1;
import ava.io.authentication_manager.model.Credentials;
import ava.io.authentication_manager.utils.ErrorCode;
import ava.io.authentication_manager.utils.Helper;
import ava.io.authentication_manager.utils.custom_excpeption.HttpCustomException;
import ava.io.authentication_manager.utils.custom_excpeption.UserAlreadyExistedException;
import ava.io.authentication_manager.utils.custom_excpeption.UserNotFoundException;
import lombok.Getter;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Service
public class UserService {
    @Autowired
    private PathBasedConfigResolver1 config;
    @Autowired
    private UserRepresentationMapper userRepresentationMapper;
    @Autowired
    private RoleGroupRealmManagerService service;
    @Autowired
    private TenantResolver tenantResolver;


    public static final List<String> searchKeys = List.of("email", "gsm");


    public UserService() {
    }


    public List<UserDto> getUsers(String tenant) {

        return userRepresentationMapper.toDtoList(config.getRealmResource(tenant).users().list());
    }

    public UserDto getUser(String tenant, UUID userId) {

        return userRepresentationMapper.toDto(config.getRealmResource(tenant).users().get(userId.toString()).toRepresentation());
    }

    public UserDto getUserByPhoneNum(String tenant, String phone) {
        return userRepresentationMapper.toDto(config.getRealmResource(tenant).users().searchByAttributes("gsm:" + phone).stream().findFirst().orElse(null));
    }


    public List<UserDto> getUsersByRole(String tenant, String roleName) {
        return userRepresentationMapper.toDtoList(new ArrayList<>(service.getRole(tenant, roleName).getRoleUserMembers())); //getUseruMembers() TODO: check this
    }

    /**
     * Search by attribute  from keycloak
     *
     * @param keyword
     * @return A list of userDto
     */
    @Deprecated
    public List<UserDto> searchByAttribute(String tenant, String keyword) {
        StringBuilder searchQuery = new StringBuilder();
        searchKeys.stream().forEach(k -> {
            if (searchQuery.length() > 0) {
                searchQuery.append(" ");
            }
            searchQuery.append(k).append(":").append(keyword);
        });
//        build the search query regarding the passed attributes

        return userRepresentationMapper.toDtoList(config.getRealmResource(tenant).users().searchByAttributes(searchQuery.toString()));
    }

//    public List<UserDTO> searchByCriteria(String keyword) {
//        UserSpecification spec1 = new UserSpecification(new SearchCriteria("firstname", ":", keyword));
//        UserSpecification spec2 = new UserSpecification(new SearchCriteria("lastName", ":", keyword));
//        UserSpecification spec3 = new UserSpecification(new SearchCriteria("emailAddress", ":", keyword));
//        UserSpecification spec4 = new UserSpecification(new SearchCriteria("userName", ":", keyword));
//
//        List<UserLoginData> users = loginDataService.getUserLoginRepo().findAll(Specification.where(spec1).or(spec2).or(spec3).or(spec4));
//        return users.size() > 0 ? loginDataMapper.toDtoList(users) : null;
//    }

    public UserDto getUserByEmail(String tenant, String email) {
        return userRepresentationMapper.toDto(config.getRealmResource(tenant).users().searchByAttributes("email:" + email).stream().findFirst().orElseThrow(UserNotFoundException::new));
    }

    public List<RoleRepresentation> getUserRoles(String tenant, String userId) {
        ClientRepresentation clientRepresentation = config.getRealmResource(tenant).clients().findAll()
                .stream().filter(client -> client.getClientId().equals(tenantResolver.getTenant(tenant).getResource())).collect(Collectors.toList())
                .get(0);
        return config.getRealmResource(tenant).users().get(userId).roles().clientLevel(clientRepresentation.getId()).listAll();
    }


    public RoleRepresentation assignRoleToUser(String tenant, String userId, String roleName) {
        RoleResource roleRepresentation = service.getRole(tenant, roleName);
        ClientRepresentation clientRepresentation = config.getRealmResource(tenant).clients().findAll()
                .stream().filter(client -> client.getClientId().equals(tenantResolver.getTenant(tenant).getResource())).collect(Collectors.toList())
                .get(0);
        try {
            config.getRealmResource(tenant).users().get(userId).roles().clientLevel(clientRepresentation.getId())
                    .add(Collections.singletonList(roleRepresentation.toRepresentation()));
        } catch (NotFoundException e) {
            throw new HttpCustomException(HttpStatus.NOT_FOUND.value(), ErrorCode.ROLE_NOTE_EXIST.getMessage());
        }
        return roleRepresentation.toRepresentation();

    }

    public void assignRolesToUser(String tenant, String userId, List<String> roles) {
        if (roles != null)
            roles.stream().forEach(r -> assignRoleToUser(tenant, userId, r));
    }

    public void assignRolesRepToUser(String tenant, String userId, List<RoleRepresentation> rolesRep) {
        ClientRepresentation clientRepresentation = config.getRealmResource(tenant).clients().findAll()
                .stream().filter(client -> client.getClientId().equals(tenantResolver.getTenant(tenant).getResource())).collect(Collectors.toList())
                .get(0);
        config.getRealmResource(tenant).users().get(userId).roles().clientLevel(clientRepresentation.getId())
                .add(rolesRep);
    }

    public void joinGroup(String tenant, String userId, String groupId) {
        config.getRealmResource(tenant).users().get(userId).joinGroup(groupId);
    }

    @Transactional
    public UserDto create(String tenant, UserDto userDTO, Map<String, List<String>> attributes) {
        CredentialRepresentation credential = Credentials.createPasswordCredentials(userDTO.getPassword().equalsIgnoreCase("auto_generated")
                ? Helper.generatePassayPass() : userDTO.getPassword());
        UserRepresentation user = new UserRepresentation();
        user.setFirstName(userDTO.getFirstName());
        user.setEmail(userDTO.getEmailId());
        user.setLastName(userDTO.getLastName());
        user.setUsername(userDTO.getUserName());
        user.setAttributes(attributes);
        user.setCredentials(Collections.singletonList(credential));


        //check if this phone already registered
        var alreadyExist = getUserByPhoneNum(tenant, attributes.get("gsm").get(0));
        if (alreadyExist != null) {
            throw new UserAlreadyExistedException(ErrorCode.EXITED_USER.getMessage());
        }

        var response = config.getRealmResource(tenant).users().create(user);

        if (response.getStatus() != HttpStatus.CREATED.value()) {
            throw new HttpCustomException(response.getStatus(), response.getStatusInfo().getReasonPhrase());
        }

        var userId = CreatedResponseUtil.getCreatedId(response);

        return userRepresentationMapper.toDto(config.getRealmResource(tenant).users().get(userId).toRepresentation());

    }


    /**
     * 1. Create a user
     * 2. Add the user to a group
     * 3. Assign group roles to user
     *
     * @param groupId
     * @param userDTO
     * @param ref
     * @return
     */
    public UserDto createAndJoinGroup(String tenant, String groupId, UserDto userDTO, UUID... ref) {
        Map<String, List<String>> att = new HashMap<>();
        att.put("gsm", List.of(userDTO.getCountryCode() + Helper.removeLeadingZeros(userDTO.getPhone())));
        att.put(service.getGroup(tenant, groupId).getName(), List.of(userDTO.getCountryCode() + Helper.removeLeadingZeros(userDTO.getPhone())));
        //create the user
        String username = userDTO.getFirstName() + "_" + userDTO.getLastName() + Helper.generateCode();
        userDTO.setUserName(username.toLowerCase());
        UserDto createdUser = create(tenant, userDTO, att);


        joinGroup(tenant, createdUser.getUserId(), groupId);


        assignRolesRepToUser(tenant, createdUser.getUserId(), service.getGroupRoles(tenant, groupId));

        return createdUser;


    }


    /**
     * 1. Create user
     * 2. Add the passed ref to user attributes
     * 3. Assign the needed role to the user
     *
     * @param userDTO
     * @return
     */
    @Transactional()
    public UserDto createAndAssignRoles(String tenant, UserDto userDTO) {
//        UserDTO createdUser = null;
//        try {
//
//            Map<String, List<String>> att = new HashMap<>();
//            att.put("tenant_id", Objects.requireNonNull(List.of(tenant)));
//            att.put("gsm", List.of(userDTO.getCountryCode() + Helper.removeLeadingZeros(userDTO.getPhone())));
//            att.put("ref", Objects.isNull(userDTO.getReference()) ? Collections.singletonList(userDTO.getReference()) : null);
//
//            String username = userDTO.getFirstName() + "_" + userDTO.getLastName() + Helper.generateCode();
//            userDTO.setUserName(username.toLowerCase());
//            //create the user
//            createdUser = create(tenant, userDTO, att);
//            //Assign the roles
//            assignRolesToUser(tenant, createdUser.getUserId(), userDTO.getRoles());
//
//            //save  on system database
//            UserLoginData sys_user = loginDataMapper.toEntity(userDTO);
//            sys_user.setKeycloakId(UUID.fromString(Objects.requireNonNull(createdUser.getUserId())));
////        sys_user.setPhone(phone);
//            loginDataService.saveAndPublishEvent(sys_user);
//        } catch (Exception e) {
//            if (createdUser.getUserId() != null)
//                deleteKeycloakUser(tenant, createdUser.getUserId());
//            throw e;
//        }
//
//        return createdUser;
        // ______________ TODO: update the code
        return null;
    }

    /**
     * 1. get the user by id
     * 2. delete from keycloak
     * 3. soft delete from db
     *
     * @param kid
     * @return
     */
    public UserDto delete(String tenant, String kid) {
        var keycloakUser = config.getRealmResource(tenant).users().get(kid).toRepresentation();
        if (keycloakUser == null)
            throw new UserNotFoundException();
        deleteKeycloakUser(tenant, kid);
        return userRepresentationMapper.toDto(keycloakUser);
    }

    /**
     * 1. get the user by phone
     * 2. delete from keycloak
     * 3. soft delete from db
     *
     * @param phone
     * @return
     */
    public UserDto deleteByPhone(String tenant, String phone) {
//        UserDTO kUser = ke getUserByPhoneNum(tenant,phone);
//        if (kUser == null)
//            throw new UserNotFoundException();
//
//        deleteKeycloakUser(tenant, kUser.getUserId());
//
//        return loginDataMapper.toDto(user.get(0));
        // _________-- TODO: check for the user and update the code
        return null;
    }

    public void deleteKeycloakUser(String tenant, String userId) {
        config.getRealmResource(tenant).users().get(userId).remove();
    }

    public void unAssignRoleToUser(String tenant, String userId, String roleName) {

        ClientRepresentation clientRepresentation = config.getRealmResource(tenant).clients().findAll()
                .stream().filter(client -> client.getClientId().equals(tenantResolver.getTenant(tenant).getResource())).collect(Collectors.toList())
                .get(0);
        try {
            config.getRealmResource(tenant).users().get(userId).roles().clientLevel(clientRepresentation.getId()).remove(Collections.singletonList(service.getRole(tenant, roleName).toRepresentation()));
        } catch (NotFoundException e) {
            throw new NotFoundException(ErrorCode.ROLE_NOTE_EXIST.getMessage());
        }

    }

    /**
     * In order to add a user to an organization -> copy the user from the public entity tenant -> to the related organization tenant
     * 1. copy the user from the public entity
     * 2. get the resource for the related organization
     * 3. create the copied user
     *
     * @param tenant
     * @param orgId
     * @param userId
     * @return
     */
    public Object addUserToOrganization(String tenant, String orgId, String userId) {

        // check if  not already exist

        //get the user from public provider
        UserRepresentation userRep = config.getRealmResource(Helper.PROVIDER).users().get(userId).toRepresentation();

        if(userRep == null)
            throw new UserNotFoundException();

        //get list of old roles
        List<RoleRepresentation> roles =   getUserRoles(Helper.PROVIDER,userRep.getId());

        String copiedUserId = null;
        try {
            Response response = config.getRealmResource(tenant).users().create(userRep);
            copiedUserId = CreatedResponseUtil.getCreatedId(response);
            assignRolesToUser(tenant,copiedUserId,roles.stream().map(RoleRepresentation::getName).collect(Collectors.toList()));
            return userRep;
        }catch (Exception e){
            if (copiedUserId != null)
                deleteKeycloakUser(tenant, userId);
            throw e;
        }
    }
    public Object getUserGroup(String tenant,  String userId) {

        // check if  not already exist

        //get the user from public provider
        List<GroupRepresentation> groups = config.getRealmResource(Helper.PROVIDER).users().get(userId).groups().stream().collect(Collectors.toList());

        if(groups == null)
            throw new UserNotFoundException();

        //get list of old roles
//        List<RoleRepresentation> roles =   getUserRoles(Helper.PROVIDER,groups.getId());

        return groups;
    }


}


























