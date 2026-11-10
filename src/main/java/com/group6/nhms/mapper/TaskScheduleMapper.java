package com.group6.nhms.mapper;


import com.group6.nhms.dto.request.TaskScheduleCreateRequest;
import com.group6.nhms.dto.response.TaskScheduleResponse;
import com.group6.nhms.entity.TaskSchedule;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


@Component
public class TaskScheduleMapper {


   private final ModelMapper modelMapper;


   public TaskScheduleMapper(ModelMapper modelMapper) {
       this.modelMapper = modelMapper;
   }


   public TaskScheduleResponse toResponse(TaskSchedule entity) {
       if (entity == null) return null;


       TaskScheduleResponse response = modelMapper.map(entity, TaskScheduleResponse.class);


       response.setWorkDate(entity.getShiftDate());
       response.setShift(entity.getShiftType());
       response.setStatus(entity.getStatus());
       response.setNote(entity.getTask());


       if (entity.getStaff() != null) {
           response.setStaffId(entity.getStaff().getId());
           response.setStaffName(entity.getStaff().getFullName());
           response.setRole(entity.getStaff().getJobTitle());
           response.setStaffPhone(entity.getStaff().getPhone());
           response.setStaffSpecialization(entity.getStaff().getSpecialization());
       }


       if (entity.getRoom() != null) {
           response.setRoomId(entity.getRoom().getId());
           response.setRoomNumber(entity.getRoom().getRoomNumber());
       }


       return response;
   }


   public TaskSchedule toEntity(TaskScheduleCreateRequest request) {
       if (request == null) return null;
       TaskSchedule entity = modelMapper.map(request, TaskSchedule.class);
       entity.setShiftDate(request.getWorkDate());
       entity.setShiftType(request.getShift());
       entity.setTask(request.getNote());
       return entity;
   }
}

