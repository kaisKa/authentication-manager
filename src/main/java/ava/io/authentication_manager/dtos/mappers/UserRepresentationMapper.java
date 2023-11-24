package ava.io.authentication_manager.dtos.mappers;

import ava.io.authentication_manager.dtos.UserDto;
import ava.io.authentication_manager.utils.Helper;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserRepresentationMapper extends BaseMapper<UserRepresentation, UserDto> {

    @Override
    @Named("userDtoResponse")
    @Mapping(source = "username",target = "userName")
    @Mapping(source = "email",target = "emailId")
    @Mapping(source = "id",target = "userId")
    @Mapping(target = "phone", source = "attributes", qualifiedByName = "gsmToPhone")
    @Mapping(target = "isVerified", source = "attributes", qualifiedByName = "getIsVerified")
    @Mapping(target = "accountId", source = "attributes", qualifiedByName = "account_IdToAccountId")
    @Mapping(source = "firstName",target = "firstName")
    UserDto toDto(UserRepresentation userRepresentation);

    @Override
    @Mapping(target = "username",source = "userName")
    @Mapping(target = "email",source = "emailId")
    @Mapping(target = "id",source = "userId")
    @Mapping(source = "firstName",target = "firstName")
    UserRepresentation toEntity(UserDto userDTO);

    @Override
    @IterableMapping(qualifiedByName = "userDtoResponse")
    List<UserDto> toDtoList(List<UserRepresentation> userRepresentations);

    @Named("getIsVerified")
    default Boolean getIsVerified(Map<String, List<String>> attributes) {

        return Boolean.parseBoolean(attributes.containsKey(Helper.IS_VERIFIED) ? attributes.get(Helper.IS_VERIFIED).get(0) : "false");

    }

    @Named("gsmToPhone")
    default String gsmToPhone(Map<String, List<String>> attributes) {
        return attributes.containsKey("gsm") ? attributes.get("gsm").get(0) : null;
    }

    @Named("account_IdToAccountId")
    default String account_IdToAccountId(Map<String, List<String>> attributes) {
        return attributes.containsKey(Helper.ACCOUNT_ID) ? attributes.get(Helper.ACCOUNT_ID).get(0) : null;
    }



}
