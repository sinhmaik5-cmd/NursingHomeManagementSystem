package com.group6.nhms.mapper;

import com.group6.nhms.dto.request.StaffCreateRequest;
import com.group6.nhms.dto.response.StaffResponse;
import com.group6.nhms.entity.Staff;
import org.springframework.stereotype.Component;

@Component
public class StaffMapper {

    public Staff toEntity(StaffCreateRequest request) {
        if (request == null) return null;
        Staff staff = new Staff();
        staff.setFullName(request.getFullName());
        staff.setJobTitle(request.getJobTitle());
        staff.setPhone(request.getPhone());
        staff.setSpecialization(request.getSpecialization());
        return staff;
    }

    public StaffResponse toResponse(Staff entity) {
        if (entity == null) return null;
        StaffResponse response = new StaffResponse();
        response.setId(entity.getId());
        response.setFullName(entity.getFullName());
        response.setJobTitle(entity.getJobTitle());
        response.setPhone(entity.getPhone());
        response.setSpecialization(entity.getSpecialization());
        return response;
    }
}