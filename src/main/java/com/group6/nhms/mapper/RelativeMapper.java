package com.group6.nhms.mapper;

import com.group6.nhms.dto.common.RelativeDto;
import com.group6.nhms.entity.Relative;
import org.springframework.stereotype.Component;

@Component
public class RelativeMapper {

    public Relative toEntity(RelativeDto dto) {
        if (dto == null) return null;
        Relative relative = new Relative();
        relative.setFullName(dto.getFullName());
        relative.setPhoneNumber(dto.getPhoneNumber());
        relative.setEmail(dto.getEmail());
        relative.setAddress(dto.getAddress());
        relative.setRelationship(dto.getRelationship());
        return relative;
    }

    public RelativeDto toDto(Relative entity) {
        if (entity == null) return null;
        RelativeDto dto = new RelativeDto();
        dto.setId(entity.getId()); // Giả sử Entity có trường id
        dto.setFullName(entity.getFullName());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setEmail(entity.getEmail());
        dto.setAddress(entity.getAddress());
        dto.setRelationship(entity.getRelationship());
        return dto;
    }
}