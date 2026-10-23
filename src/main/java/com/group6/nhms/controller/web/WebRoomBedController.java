package com.group6.nhms.controller.web;

import com.group6.nhms.dto.request.BedStatusUpdateRequest;
import com.group6.nhms.entity.Bed;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Room;
import com.group6.nhms.enums.BedStatus;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.repository.BedRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.RoomRepository;
import com.group6.nhms.service.BedService;
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

    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final ResidentRepository residentRepository;
    private final BedService bedService;

    public WebRoomBedController(
            RoomRepository roomRepository,
            BedRepository bedRepository,
            ResidentRepository residentRepository,
            BedService bedService
    ) {
        this.roomRepository = roomRepository;
        this.bedRepository = bedRepository;
        this.residentRepository = residentRepository;
        this.bedService = bedService;
    }

    @GetMapping
    public String bedManagement(Model model) {
        List<Room> rooms = roomRepository.findAll();
        List<Bed> allBeds = bedRepository.findAll();

        Map<Integer, List<Bed>> bedsByRoomId = new HashMap<>();
        for (Room room : rooms) {
            bedsByRoomId.put(room.getId(), bedRepository.findByRoomId(room.getId()));
        }

        List<Resident> activeResidents = residentRepository.findByStatus(ResidentStatus.ACTIVE.name());
        Map<Integer, Resident> residentByBedId = new HashMap<>();
        for (Resident r : activeResidents) {
            if (r.getBed() != null) {
                residentByBedId.put(r.getBed().getId(), r);
            }
        }

        long totalBeds = allBeds.size();
        long occupiedBeds = bedRepository.countByStatus(BedStatus.OCCUPIED.name());
        long availableBeds = bedRepository.countByStatus(BedStatus.AVAILABLE.name());
        long maintenanceBeds = bedRepository.countByStatus(BedStatus.MAINTENANCE.name());

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
