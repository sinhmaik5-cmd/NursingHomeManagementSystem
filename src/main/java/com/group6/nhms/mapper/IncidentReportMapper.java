package com.group6.nhms.mapper;

import com.group6.nhms.dto.request.IncidentReportCreateRequest;
import com.group6.nhms.dto.response.IncidentReportResponse;
import com.group6.nhms.entity.IncidentReport;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class IncidentReportMapper {

    private final ModelMapper modelMapper;

    public IncidentReportMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public IncidentReportResponse toResponse(IncidentReport entity) {
        if (entity == null) return null;

        IncidentReportResponse response = modelMapper.map(entity, IncidentReportResponse.class);

        // Ánh xạ thời gian IncidentTime (Instant) sang LocalDateTime
        if (entity.getIncidentTime() != null) {
            response.setCreatedDate(LocalDateTime.ofInstant(entity.getIncidentTime(), ZoneId.systemDefault()));
        }

        if (entity.getResident() != null) {
            response.setResidentId(entity.getResident().getId());
            response.setResidentName(entity.getResident().getFullName());
            if (entity.getResident().getBed() != null && entity.getResident().getBed().getRoom() != null) {
                response.setRoomNumber(entity.getResident().getBed().getRoom().getRoomNumber());
            }
        }

        // Lấy tên Y tế/Nhân viên báo cáo sự cố qua reporterStaff
        if (entity.getReporterStaff() != null) {
            response.setReportedByStaffName(entity.getReporterStaff().getFullName());
        }

        response.setSeverityLevel(entity.getSeverityLevel());
        response.setStatus(entity.getStatus());

        return response;
    }

    public IncidentReport toEntity(IncidentReportCreateRequest request) {
        if (request == null) return null;
        return modelMapper.map(request, IncidentReport.class);
    }
}