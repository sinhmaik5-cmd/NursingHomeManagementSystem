package com.group6.nhms.controller;

import com.group6.nhms.dto.request.TaskScheduleCreateRequest;
import com.group6.nhms.dto.response.TaskScheduleResponse;
import com.group6.nhms.mapper.TaskScheduleMapper;
import com.group6.nhms.repository.TaskScheduleRepository;
import com.group6.nhms.service.TaskScheduleService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/task-schedules")
public class TaskScheduleController {

    private final TaskScheduleService taskScheduleService;
    private final TaskScheduleRepository taskScheduleRepository;
    private final TaskScheduleMapper taskScheduleMapper;

    public TaskScheduleController(
            TaskScheduleService taskScheduleService,
            TaskScheduleRepository taskScheduleRepository,
            TaskScheduleMapper taskScheduleMapper
    ) {
        this.taskScheduleService = taskScheduleService;
        this.taskScheduleRepository = taskScheduleRepository;
        this.taskScheduleMapper = taskScheduleMapper;
    }

    // 1. Quản lý phân công ca trực mới (có validate chống trùng ca trực trong cùng khung giờ/ngày)
    @PostMapping
    public ResponseEntity<TaskScheduleResponse> createSchedule(
            @Valid @RequestBody TaskScheduleCreateRequest request,
            @RequestParam Integer roomId
    ) {
        TaskScheduleResponse response = taskScheduleService.createSchedule(request, roomId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Nhân viên xem danh sách ca trực của mình theo ngày
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<TaskScheduleResponse>> getStaffSchedule(
            @PathVariable Integer staffId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<TaskScheduleResponse> responses = taskScheduleService.getStaffSchedule(staffId, targetDate);
        return ResponseEntity.ok(responses);
    }

    // 3. Nhân viên xác nhận hoàn thành công việc sau ca trực (ASSIGNED -> COMPLETED)
    @PutMapping("/{id}/complete")
    public ResponseEntity<TaskScheduleResponse> completeSchedule(
            @PathVariable Integer id,
            @RequestParam Integer loggedInStaffId
    ) {
        TaskScheduleResponse response = taskScheduleService.completeSchedule(id, loggedInStaffId);
        return ResponseEntity.ok(response);
    }

    // 4. Xem toàn bộ lịch trực của viện trong ngày
    @GetMapping("/date/{date}")
    public ResponseEntity<List<TaskScheduleResponse>> getSchedulesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<TaskScheduleResponse> responses = taskScheduleRepository.findByShiftDate(date)
                .stream()
                .map(taskScheduleMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
