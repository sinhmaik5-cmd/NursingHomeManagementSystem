package com.group6.nhms.mapper;

import com.group6.nhms.dto.request.MedicationCreateRequest;
import com.group6.nhms.dto.response.MedicationResponse;
import com.group6.nhms.entity.Medication;
import org.springframework.stereotype.Component;

@Component
public class MedicationMapper {

    public Medication toEntity(MedicationCreateRequest request) {
        if (request == null) return null;
        Medication medication = new Medication();
        medication.setMedName(request.getMedName());
        medication.setQuantityInStock(request.getQuantityInStock());
        medication.setExpiryDate(request.getExpiryDate());
        medication.setSupplier(request.getSupplier());
        medication.setUnitPrice(request.getUnitPrice());
        return medication;
    }

    public MedicationResponse toResponse(Medication entity) {
        if (entity == null) return null;
        MedicationResponse response = new MedicationResponse();
        response.setId(entity.getId());
        response.setMedName(entity.getMedName());
        response.setQuantityInStock(entity.getQuantityInStock());
        response.setExpiryDate(entity.getExpiryDate());
        response.setSupplier(entity.getSupplier());
        response.setUnitPrice(entity.getUnitPrice());
        return response;
    }
}
