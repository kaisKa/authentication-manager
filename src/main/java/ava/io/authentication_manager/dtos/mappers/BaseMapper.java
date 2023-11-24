package ava.io.authentication_manager.dtos.mappers;

import java.util.List;

public interface BaseMapper<ENTITY,DTO> {
    DTO toDto(ENTITY entity);
    ENTITY toEntity(DTO dto);
    List<DTO> toDtoList(List<ENTITY> entityList);
    List<ENTITY> toEntityList(List<DTO> dtoList);
}
