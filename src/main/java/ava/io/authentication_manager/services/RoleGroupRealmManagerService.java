package ava.io.authentication_manager.services;

import ava.io.authentication_manager.config.PathBasedConfigResolver1;
import ava.io.authentication_manager.config.mullti_tenant.TenantResolver;
import ava.io.authentication_manager.dtos.Realm;
import ava.io.authentication_manager.dtos.UserDto;

import ava.io.authentication_manager.config.KeyCloakConfig;
import ava.io.authentication_manager.dtos.mappers.UserRepresentationMapper;
import ava.io.authentication_manager.utils.ErrorCode;
import ava.io.authentication_manager.utils.custom_excpeption.HttpCustomException;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleGroupRealmManagerService {

    private PathBasedConfigResolver1 config;
    private KeyCloakConfig keyCloakConfig;
    @Autowired
    private TenantResolver tenantResolver;
    private UserRepresentationMapper userRepresentationMapper;
    @Autowired
    @Qualifier("notSecureRestTemplate")
    private RestTemplate restTemplate;


    public RoleGroupRealmManagerService(PathBasedConfigResolver1 config, UserRepresentationMapper userRepresentationMapper) {
        this.config = config;
        this.userRepresentationMapper = userRepresentationMapper;
    }


    public List<GroupRepresentation> getGroups(String tenant) {

        return config.getRealmResource(tenant).groups().groups();

    }

    public GroupRepresentation getGroup(String tenant, String groupId) {
        return config.getRealmResource(tenant).groups().group(groupId).toRepresentation();
    }

    public List<RoleRepresentation> getGroupRoles(String tenant, String groupId) {
        ClientRepresentation clientRepresentation = config.getRealmResource(tenant).clients().findAll()
                .stream().filter(client -> client.getClientId().equals(tenantResolver.getTenant(tenant).getResource())).collect(Collectors.toList())
                .get(0);
        return config.getRealmResource(tenant).groups().group(groupId).roles().clientLevel(clientRepresentation.getId()).listAll();
    }

    public List<UserDto> getGroupUsers(String tenant, String groupId) {
        return userRepresentationMapper.toDtoList(config.getRealmResource(tenant).groups().group(groupId).members());
    }

    public List<String> getRoles(String tenant) {
        ClientRepresentation clientRepresentation = config.getRealmResource(tenant).clients().findAll()
                .stream().filter(client -> client.getClientId().equals(tenantResolver.getTenant(tenant).getResource())).collect(Collectors.toList())
                .get(0);
        return config.getRealmResource(tenant).clients().get(clientRepresentation.getId()).roles().list().stream().map(r -> r.getName()).collect(Collectors.toList());
    }

    public RoleResource getRole(String tenant, String roleName) {
        ClientRepresentation clientRepresentation = config.getRealmResource(tenant).clients().findAll()
                .stream().filter(client -> client.getClientId().equals(tenantResolver.getTenant(tenant).getResource())).collect(Collectors.toList())
                .get(0);
        return config.getRealmResource(tenant).clients().get(clientRepresentation.getId()).roles().get(roleName);
    }


    public String addRealm(Realm realm) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        // append  query param to the url
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(keyCloakConfig.getRoutes().realm);
        var entity = new HttpEntity<>(realm, headers);
        return restTemplate.exchange(uriBuilder.toUriString(),
                HttpMethod.POST,
                entity,
                String.class
        ).getBody();
    }


    /**
     * 1. Check if the group exist --> if not create one
     * 2. in case the group exists already --> throw ex
     *
     * @param tenant
     * @param name
     * @return
     */
    public GroupRepresentation createGroup(String tenant, String name) {
        GroupsResource groupsResource = config.getRealmResource(tenant).groups();
        List<GroupRepresentation> groupRe = groupsResource.groups();

        boolean isCreated = groupRe.stream().anyMatch(g -> g.getName().equalsIgnoreCase(name));
        if (isCreated)
            throw new HttpCustomException(HttpStatus.CONFLICT.value(), ErrorCode.EXISTED_GROUP.getMessage());

        GroupRepresentation newGroup = new GroupRepresentation();
        newGroup.setName(name);
        var res = groupsResource.add(newGroup);
        return res.readEntity(GroupRepresentation.class);
    }

    public void joinGroup(String tenant, String userId, String groupId) {
        config.getRealmResource(tenant).users().get(userId).joinGroup(groupId);
    }

    /**
     * 1. Create group
     * 2. Create super admin user
     * 3. Invoke cma api
     * 4. add the user to the group
     * 4. send mail to the user ??
     *
     * @param tenant refer to the org
     * @param name name of the sub org
     */
    private void CreateClinic(String tenant, String name,String superAdminId) {

        var res = createGroup(tenant, name);
        if (res != null)
            joinGroup(tenant,superAdminId,res.getId());


    }

}
