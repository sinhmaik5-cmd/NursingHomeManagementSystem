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


       IncidentReportResponse response = new IncidentReportResponse();
       response.setId(entity.getId());
       response.setSeverityLevel(entity.getSeverityLevel());
       response.setStatus(entity.getStatus());
       response.setActionTaken(entity.getActionTaken());
       response.setLocation(entity.getLocation());


       // Bóc tách "[Title] Description" từ cột Description
       String rawDesc = entity.getDescription();
       if (rawDesc != null && rawDesc.startsWith("[") && rawDesc.contains("]")) {
           int closeIdx = rawDesc.indexOf("]");
           response.setTitle(rawDesc.substring(1, closeIdx).trim());
           response.setDescription(rawDesc.substring(closeIdx + 1).trim());
       } else {
           response.setTitle("Sự cố cơ sở vật chất / y tế");
           response.setDescription(rawDesc != null ? rawDesc : "");
       }


       // Ánh xạ thời gian IncidentTime (Instant) sang LocalDateTime
       if (entity.getIncidentTime() != null) {
           response.setCreatedDate(LocalDateTime.ofInstant(entity.getIncidentTime(), ZoneId.systemDefault()));
       }


       // Ánh xạ Cư dân
       if (entity.getResident() != null) {
           response.setResidentId(entity.getResident().getId());
           response.setResidentName(entity.getResident().getFullName());
           if (entity.getResident().getBed() != null) {
               response.setBedId(entity.getResident().getBed().getId());
               response.setBedNumber(entity.getResident().getBed().getBedNumber());
               if (entity.getResident().getBed().getRoom() != null) {
                   response.setRoomId(entity.getResident().getBed().getRoom().getId());
                   response.setRoomNumber(entity.getResident().getBed().getRoom().getRoomNumber());
               }
           }
       }


       // Ánh xạ Phòng trực tiếp (nếu có)
       if (entity.getRoom() != null) {
           response.setRoomId(entity.getRoom().getId());
           response.setRoomNumber(entity.getRoom().getRoomNumber());
       }


       // Ánh xạ Giường trực tiếp (nếu có)
       if (entity.getBed() != null) {
           response.setBedId(entity.getBed().getId());
           response.setBedNumber(entity.getBed().getBedNumber());
           if (entity.getBed().getRoom() != null && response.getRoomNumber() == null) {
               response.setRoomId(entity.getBed().getRoom().getId());
               response.setRoomNumber(entity.getBed().getRoom().getRoomNumber());
           }
       }


       // Lấy thông tin Y tế/Nhân viên báo cáo sự cố qua reporterStaff
       if (entity.getReporterStaff() != null) {
           response.setReportedByStaffId(entity.getReporterStaff().getId());
           response.setReportedByStaffName(entity.getReporterStaff().getFullName());
           response.setReporterStaffRole(entity.getReporterStaff().getJobTitle());
       }


       return response;
   }


   public IncidentReport toEntity(IncidentReportCreateRequest request) {
       if (request == null) return null;
       return modelMapper.map(request, IncidentReport.class);
   }
}

