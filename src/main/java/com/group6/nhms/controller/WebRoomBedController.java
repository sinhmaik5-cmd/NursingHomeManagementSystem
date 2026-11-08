package com.group6.nhms.controller;

import com.group6.nhms.dto.request.BedCreateRequest;
import com.group6.nhms.dto.request.BedStatusUpdateRequest;
import com.group6.nhms.dto.request.BedUpdateRequest;
import com.group6.nhms.dto.request.RoomCreateRequest;
import com.group6.nhms.dto.request.RoomUpdateRequest;
import com.group6.nhms.dto.response.BedResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.dto.response.RoomResponse;
import com.group6.nhms.dto.response.TaskScheduleResponse;
import com.group6.nhms.service.BedService;
import com.group6.nhms.service.ResidentService;
import com.group6.nhms.service.RoomService;
import com.group6.nhms.service.TaskScheduleService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rooms/beds")
public class WebRoomBedController {

    private final RoomService roomService;
    private final BedService bedService;
    private final ResidentService residentService;
    private final TaskScheduleService taskScheduleService;

    public WebRoomBedController(
            RoomService roomService,
            BedService bedService,
            ResidentService residentService,
            TaskScheduleService taskScheduleService
    ) {
        this.roomService = roomService;
        this.bedService = bedService;
        this.residentService = residentService;
        this.taskScheduleService = taskScheduleService;
    }

    @GetMapping
    public String bedManagement(Model model) {
        List<RoomResponse> rooms = roomService.getAllRooms();

        List<RoomResponse> residentRooms = rooms.stream()
                .filter(r -> !"CLINIC".equalsIgnoreCase(r.getRoomType()) && !"RECEPTION".equalsIgnoreCase(r.getRoomType()))
                .toList();
        List<RoomResponse> clinicRooms = rooms.stream()
                .filter(r -> "CLINIC".equalsIgnoreCase(r.getRoomType()))
                .toList();
        List<RoomResponse> receptionRooms = rooms.stream()
                .filter(r -> "RECEPTION".equalsIgnoreCase(r.getRoomType()))
                .toList();

        Map<Integer, List<BedResponse>> bedsByRoomId = new HashMap<>();
        for (RoomResponse room : rooms) {
            bedsByRoomId.put(room.getId(), bedService.getBedsByRoom(room.getId()));
        }

        List<ResidentResponse> activeResidents = residentService.getAllResidents(null, "ACTIVE");
        Map<Integer, ResidentResponse> residentByBedId = new HashMap<>();
        for (ResidentResponse r : activeResidents) {
            if (r.getBedId() != null) {
                residentByBedId.put(r.getBedId(), r);
            }
        }

        Map<Integer, Boolean> roomHasOccupiedBed = new HashMap<>();
        for (RoomResponse room : rooms) {
            List<BedResponse> roomBeds = bedsByRoomId.getOrDefault(room.getId(), List.of());
            boolean hasOccupied = roomBeds.stream().anyMatch(b -> "OCCUPIED".equalsIgnoreCase(b.getStatus()));
            roomHasOccupiedBed.put(room.getId(), hasOccupied);
        }

        // Lấy danh sách ca trực hôm nay cho từng phòng / vị trí (Phòng khám, Quầy lễ tân, Phòng lưu trú)
        LocalDate today = LocalDate.now();
        Map<Integer, List<TaskScheduleResponse>> schedulesByRoomId = new HashMap<>();
        for (RoomResponse room : rooms) {
            schedulesByRoomId.put(room.getId(), taskScheduleService.getSchedulesByRoomAndDate(room.getId(), today));
        }

        long totalBeds = bedService.countTotalBeds();
        long occupiedBeds = bedService.countOccupiedBeds();
        long availableBeds = bedService.countAvailableBeds();
        long maintenanceBeds = bedService.countMaintenanceBeds();

        model.addAttribute("rooms", rooms);
        model.addAttribute("residentRooms", residentRooms);
        model.addAttribute("clinicRooms", clinicRooms);
        model.addAttribute("receptionRooms", receptionRooms);
        model.addAttribute("roomHasOccupiedBed", roomHasOccupiedBed);
        model.addAttribute("bedsByRoomId", bedsByRoomId);
        model.addAttribute("residentByBedId", residentByBedId);
        model.addAttribute("schedulesByRoomId", schedulesByRoomId);
        model.addAttribute("today", today);
        model.addAttribute("totalBeds", totalBeds);
        model.addAttribute("occupiedBeds", occupiedBeds);
        model.addAttribute("availableBeds", availableBeds);
        model.addAttribute("maintenanceBeds", maintenanceBeds);
        model.addAttribute("activeMenu", "bed-management");

        return "room/bed-management";
    }

    @PostMapping("/rooms/create")
    public String createRoom(
            @Valid @ModelAttribute RoomCreateRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_CENTER_MANAGER"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thêm phòng mới.");
            return "redirect:/rooms/beds";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/rooms/beds";
        }

        try {
            RoomResponse res = roomService.createRoom(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã thêm phòng mới " + res.getRoomNumber() + " (" + res.getRoomType() + " - Sức chứa: " + res.getCapacity() + ") thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thêm phòng: " + e.getMessage());
        }
        return "redirect:/rooms/beds";
    }

    @PostMapping("/rooms/{id}/edit")
    public String updateRoom(
            @PathVariable Integer id,
            @Valid @ModelAttribute RoomUpdateRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_CENTER_MANAGER"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền chỉnh sửa thông tin phòng.");
            return "redirect:/rooms/beds";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/rooms/beds";
        }

        try {
            RoomResponse res = roomService.updateRoom(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã cập nhật thông tin phòng " + res.getRoomNumber() + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật phòng: " + e.getMessage());
        }
        return "redirect:/rooms/beds";
    }

    @PostMapping("/rooms/{id}/delete")
    public String deleteRoom(
            @PathVariable Integer id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_CENTER_MANAGER"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa phòng.");
            return "redirect:/rooms/beds";
        }

        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa phòng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa phòng: " + e.getMessage());
        }
        return "redirect:/rooms/beds";
    }

    @PostMapping("/create")
    public String createBed(
            @Valid @ModelAttribute BedCreateRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_CENTER_MANAGER"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thêm giường mới.");
            return "redirect:/rooms/beds";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/rooms/beds";
        }

        try {
            BedResponse res = bedService.createBed(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã thêm giường mới " + res.getBedNumber() + " vào phòng " + res.getRoomNumber() + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thêm giường: " + e.getMessage());
        }
        return "redirect:/rooms/beds";
    }

    @PostMapping("/{id}/edit")
    public String updateBed(
            @PathVariable Integer id,
            @Valid @ModelAttribute BedUpdateRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_CENTER_MANAGER"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền chỉnh sửa giường.");
            return "redirect:/rooms/beds";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/rooms/beds";
        }

        try {
            BedResponse res = bedService.updateBed(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã cập nhật giường " + res.getBedNumber() + " (Trạng thái: " + res.getStatus() + ") thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật giường: " + e.getMessage());
        }
        return "redirect:/rooms/beds";
    }

    @PostMapping("/{id}/delete")
    public String deleteBed(
            @PathVariable Integer id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_CENTER_MANAGER"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa giường.");
            return "redirect:/rooms/beds";
        }

        try {
            bedService.deleteBed(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa giường thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa giường: " + e.getMessage());
        }
        return "redirect:/rooms/beds";
    }

    @PostMapping("/{id}/status")
    public String updateBedStatus(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "AVAILABLE") String status,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_NURSE") || a.getAuthority().equals("ROLE_CENTER_MANAGER"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền cập nhật trạng thái giường.");
            return "redirect:/rooms/beds";
        }

        try {
            bedService.updateBedStatus(id, new BedStatusUpdateRequest(status));
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã cập nhật trạng thái giường ID " + id + " thành " + status + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật trạng thái giường: " + e.getMessage());
        }
        return "redirect:/rooms/beds";
    }
}
