package com.group6.nhms.controller;

import com.group6.nhms.dto.request.TaskScheduleCreateRequest;
import com.group6.nhms.dto.response.RoomResponse;
import com.group6.nhms.dto.response.StaffResponse;
import com.group6.nhms.dto.response.TaskScheduleResponse;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.service.RoomService;
import com.group6.nhms.service.StaffService;
import com.group6.nhms.service.TaskScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/schedules")
public class WebScheduleController {

    private final TaskScheduleService taskScheduleService;
    private final StaffService staffService;
    private final RoomService roomService;

    public WebScheduleController(
            TaskScheduleService taskScheduleService,
            StaffService staffService,
            RoomService roomService
    ) {
        this.taskScheduleService = taskScheduleService;
        this.staffService = staffService;
        this.roomService = roomService;
    }

    // 1. Giao diện phân công ca trực (Giám đốc cơ sở)
    @GetMapping("/assign")
    public String showAssignSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate filterDate,
            Model model
    ) {
        LocalDate targetDate = (filterDate != null) ? filterDate : LocalDate.now();

        // Chỉ hiển thị nhân viên có nhiệm vụ trực (Bác sĩ, Điều dưỡng, Lễ tân) - Loại trừ Giám đốc (CENTER_MANAGER)
        List<StaffResponse> staffList = staffService.getAllStaff().stream()
                .filter(s -> !"CENTER_MANAGER".equalsIgnoreCase(s.getJobTitle()))
                .toList();

        List<RoomResponse> rooms = roomService.getAllRooms();
        List<TaskScheduleResponse> schedules = taskScheduleService.getSchedulesByDate(targetDate);

        TaskScheduleCreateRequest createRequest = new TaskScheduleCreateRequest();
        createRequest.setWorkDate(targetDate.isBefore(LocalDate.now()) ? LocalDate.now() : targetDate);
        createRequest.setShift("MORNING");

        model.addAttribute("staffList", staffList);
        model.addAttribute("rooms", rooms);
        model.addAttribute("schedules", schedules);
        model.addAttribute("scheduleRequest", createRequest);
        model.addAttribute("filterDate", targetDate);
        model.addAttribute("minDate", LocalDate.now());
        model.addAttribute("activeMenu", "schedule-assign");

        return "schedule/assign";
    }

    // 2. Xử lý phân công ca trực mới
    @PostMapping("/assign")
    public String processAssignSchedule(
            @ModelAttribute TaskScheduleCreateRequest request,
            @RequestParam(required = false) Integer roomId,
            RedirectAttributes redirectAttributes
    ) {
        Integer targetRoomId = (roomId != null) ? roomId : request.getRoomId();
        if (targetRoomId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn phòng / vị trí phụ trách");
            return "redirect:/schedules/assign?filterDate=" + (request.getWorkDate() != null ? request.getWorkDate() : LocalDate.now());
        }

        try {
            TaskScheduleResponse res = taskScheduleService.createSchedule(request, targetRoomId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Phân công thành công cho nhân viên " + res.getStaffName() + " (Ca " + res.getShift() + " - " + res.getRoomNumber() + ")!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi phân công ca trực: " + e.getMessage());
        }
        return "redirect:/schedules/assign?filterDate=" + (request.getWorkDate() != null ? request.getWorkDate() : LocalDate.now());
    }

    // 3. Giao diện xem lịch trực cá nhân của nhân viên
    @GetMapping("/my-schedule")
    public String mySchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication,
            Model model
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        Staff currentStaff = staffService.resolveCurrentStaff(authentication);

        List<TaskScheduleResponse> schedules = List.of();
        if (currentStaff != null) {
            schedules = taskScheduleService.getStaffSchedule(currentStaff.getId(), targetDate);
        }

        model.addAttribute("staff", currentStaff);
        model.addAttribute("schedules", schedules);
        model.addAttribute("selectedDate", targetDate);
        model.addAttribute("activeMenu", "my-schedule");

        return "schedule/my-schedule";
    }

    // 4. Nhân viên xác nhận hoàn thành ca trực
    @PostMapping("/{id}/complete")
    public String completeSchedule(
            @PathVariable Integer id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Integer staffId = staffService.resolveCurrentStaffId(authentication);
            taskScheduleService.completeSchedule(id, staffId);
            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận hoàn thành ca trực thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xác nhận hoàn thành: " + e.getMessage());
        }
        return "redirect:/schedules/my-schedule";
    }
}
