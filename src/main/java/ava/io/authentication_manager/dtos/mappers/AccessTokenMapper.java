package ava.io.authentication_manager.dtos.mappers;

import ava.io.authentication_manager.model.LoginResponse;
import org.keycloak.representations.AccessTokenResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccessTokenMapper extends BaseMapper<AccessTokenResponse, LoginResponse> {

    @Override
    LoginResponse toDto(AccessTokenResponse accessTokenResponse);

    @Override
    AccessTokenResponse toEntity(LoginResponse loginResponse);
}
