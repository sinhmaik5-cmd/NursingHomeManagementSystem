package com.group6.nhms.service;

import com.group6.nhms.dto.request.TaskScheduleCreateRequest;
import com.group6.nhms.dto.response.TaskScheduleResponse;

import java.time.LocalDate;
import java.util.List;

public interface TaskScheduleService {
    TaskScheduleResponse createSchedule(TaskScheduleCreateRequest request, Integer roomId);

    List<TaskScheduleResponse> getStaffSchedule(Integer staffId, LocalDate date);

    TaskScheduleResponse completeSchedule(Integer scheduleId, Integer loggedInStaffId);

    List<TaskScheduleResponse> getSchedulesByDate(LocalDate date);
}
