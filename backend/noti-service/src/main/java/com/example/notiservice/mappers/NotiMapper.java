package com.example.notiservice.mappers;

import com.example.notiservice.dtos.NotiDto;
import com.example.notiservice.entities.Noti;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotiMapper {
    /**
     * Maps Noti entity to NotiDto
     */
    NotiDto toDto(Noti noti);

    /**
     * Maps NotiDto to Noti entity
     */
    Noti toEntity(NotiDto dto);
}
