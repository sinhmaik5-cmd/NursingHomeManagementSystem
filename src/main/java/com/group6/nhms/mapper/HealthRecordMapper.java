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

        HealthRecordResponse response = new HealthRecordResponse();
        response.setId(entity.getId());
        response.setBloodPressure(entity.getBloodPressure());
        response.setHeartRate(entity.getHeartRate());
        response.setBloodSugar(entity.getBloodSugar());
        response.setTemperature(entity.getTemperature());
        response.setDiagnosis(entity.getNotes()); // Map cột Notes sang Diagnosis

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

        response.setMedications(new ArrayList<>());
        return response;
    }

    public HealthRecord toEntity(HealthRecordCreateRequest request) {
        if (request == null) return null;
        return modelMapper.map(request, HealthRecord.class);
    }
}
