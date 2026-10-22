package com.group6.nhms.mapper;

import com.group6.nhms.dto.common.HealthRecordDetailDto;
import com.group6.nhms.entity.HealthRecordDetail;
import org.springframework.stereotype.Component;

@Component
public class HealthRecordDetailMapper {

    public HealthRecordDetailDto toDto(HealthRecordDetail entity) {
        if (entity == null) return null;
        HealthRecordDetailDto dto = new HealthRecordDetailDto();
        dto.setId(entity.getId());
        dto.setQuantity(entity.getQuantity());
        dto.setDosage(entity.getDosage());
        dto.setFrequency(entity.getFrequency());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setTotalPrice(entity.getTotalPrice());
        
        // Lấy tên thuốc từ quan hệ với bảng Medication
        if (entity.getMedication() != null) {
            dto.setMedicationId(entity.getMedication().getId());
            dto.setMedicationName(entity.getMedication().getMedName());
        }
        return dto;
    }
}
