package ava.io.authentication_manager.dtos.mappers;

import ava.io.authentication_manager.dtos.PatientDTO;
import ava.io.authentication_manager.dtos.ProviderDTO;
import ava.io.authentication_manager.dtos.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    UserDto toDto(ProviderDTO providerDTO);


    UserDto toDto(PatientDTO patientDTO);
}
