package com.group6.nhms.mapper;


import com.group6.nhms.dto.response.StaffResponse;
import com.group6.nhms.entity.Staff;
import org.springframework.stereotype.Component;


@Component
public class StaffMapper {


   public StaffResponse toResponse(Staff entity) {
       if (entity == null) return null;
       StaffResponse response = new StaffResponse();
       response.setId(entity.getId());
       response.setFullName(entity.getFullName());
       response.setJobTitle(entity.getJobTitle());
       response.setPhone(entity.getPhone());
       response.setEmail(entity.getEmail());
       response.setCitizenId(entity.getCitizenId());
       response.setSpecialization(entity.getSpecialization());
       return response;
   }
}

