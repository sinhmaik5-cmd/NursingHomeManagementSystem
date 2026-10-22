package com.group6.nhms.mapper;

import com.group6.nhms.dto.request.ResidentCheckInRequest;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Resident;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ResidentMapper {

    private final ModelMapper modelMapper;

    public ResidentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ResidentResponse toResponse(Resident entity) {
        if (entity == null) return null;

        ResidentResponse response = modelMapper.map(entity, ResidentResponse.class);

        // Map DOB từ Entity sang ResidentResponse
        response.setDateOfBirth(entity.getDob());

        // Map thông tin Người thân
        if (entity.getRelative() != null) {
            response.setRelativeId(entity.getRelative().getId());
            response.setRelativeName(entity.getRelative().getFullName());
            response.setRelativePhone(entity.getRelative().getPhone());
        }

        // Map thông tin Giường & Phòng
        if (entity.getBed() != null) {
            response.setBedId(entity.getBed().getId());
            response.setBedNumber(entity.getBed().getBedNumber());
            if (entity.getBed().getRoom() != null) {
                response.setRoomNumber(entity.getBed().getRoom().getRoomNumber());
            }
        }

        response.setStatus(entity.getStatus());

        return response;
    }

    public Resident toEntity(ResidentCheckInRequest request) {
        if (request == null) return null;
        Resident entity = modelMapper.map(request, Resident.class);
        entity.setDob(request.getDateOfBirth());
        entity.setInitialHealthStatus(request.getMedicalHistory());
        return entity;
    }
}