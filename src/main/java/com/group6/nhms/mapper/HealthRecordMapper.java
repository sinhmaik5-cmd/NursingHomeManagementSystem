package com.group6.nhms.mapper;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.entity.HealthRecord;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

@Component
public class HealthRecordMapper {

    private final ModelMapper modelMapper;

    public HealthRecordMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public HealthRecordResponse toResponse(HealthRecord entity) {
        if (entity == null) return null;

        HealthRecordResponse response = modelMapper.map(entity, HealthRecordResponse.class);

        // Chuyển Instant sang LocalDateTime để hiển thị trên DTO Response
        if (entity.getRecordDate() != null) {
            response.setRecordDate(LocalDateTime.ofInstant(entity.getRecordDate(), ZoneId.systemDefault()));
        }

        if (entity.getResident() != null) {
            response.setResidentId(entity.getResident().getId());
            response.setResidentName(entity.getResident().getFullName());
        }

        if (entity.getStaff() != null) {
            response.setStaffName(entity.getStaff().getFullName());
        }

        if (response.getMedications() == null) {
            response.setMedications(new ArrayList<>());
        }

        return response;
    }

    public HealthRecord toEntity(HealthRecordCreateRequest request) {
        if (request == null) return null;
        return modelMapper.map(request, HealthRecord.class);
    }
}