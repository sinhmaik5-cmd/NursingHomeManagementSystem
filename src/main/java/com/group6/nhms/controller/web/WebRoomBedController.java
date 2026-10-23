package com.group6.nhms.controller.web;

import com.group6.nhms.dto.request.BedStatusUpdateRequest;
import com.group6.nhms.dto.response.BedResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.dto.response.RoomResponse;
import com.group6.nhms.service.BedService;
import com.group6.nhms.service.ResidentService;
import com.group6.nhms.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rooms/beds")
public class WebRoomBedController {

    private final RoomService roomService;
    private final BedService bedService;
    private final ResidentService residentService;

    public WebRoomBedController(
            RoomService roomService,
            BedService bedService,
            ResidentService residentService
    ) {
        this.roomService = roomService;
        this.bedService = bedService;
        this.residentService = residentService;
    }

    @GetMapping
    public String bedManagement(Model model) {
        List<RoomResponse> rooms = roomService.getAllRooms();

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

        long totalBeds = bedService.countTotalBeds();
        long occupiedBeds = bedService.countOccupiedBeds();
        long availableBeds = bedService.countAvailableBeds();
        long maintenanceBeds = bedService.countMaintenanceBeds();

        model.addAttribute("rooms", rooms);
        model.addAttribute("bedsByRoomId", bedsByRoomId);
        model.addAttribute("residentByBedId", residentByBedId);
        model.addAttribute("totalBeds", totalBeds);
        model.addAttribute("occupiedBeds", occupiedBeds);
        model.addAttribute("availableBeds", availableBeds);
        model.addAttribute("maintenanceBeds", maintenanceBeds);
        model.addAttribute("activeMenu", "bed-management");

        return "room/bed-management";
    }

    @PostMapping("/{id}/status")
    public String updateBedStatus(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "AVAILABLE") String status,
            RedirectAttributes redirectAttributes
    ) {
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
