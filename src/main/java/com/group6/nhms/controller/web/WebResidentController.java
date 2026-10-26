package com.group6.nhms.controller.web;

import com.group6.nhms.dto.request.BedTransferRequest;
import com.group6.nhms.dto.request.ResidentCheckInRequest;
import com.group6.nhms.dto.response.BedResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.service.BedService;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.ResidentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/residents")
public class WebResidentController {

    private final ResidentService residentService;
    private final BedService bedService;
    private final RelativeService relativeService;

    public WebResidentController(
            ResidentService residentService,
            BedService bedService,
            RelativeService relativeService
    ) {
        this.residentService = residentService;
        this.bedService = bedService;
        this.relativeService = relativeService;
    }

    // 1. Danh sách cư dân
    @GetMapping
    public String listResidents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            Model model
    ) {
        List<ResidentResponse> residents = residentService.getAllResidents(name, status);
        List<BedResponse> availableBeds = bedService.getAvailableBeds();

        model.addAttribute("residents", residents);
        model.addAttribute("availableBeds", availableBeds);
        model.addAttribute("activeMenu", "resident-list");
        return "resident/list";
    }

    // 2. Form tiếp nhận cư dân mới
    @GetMapping("/check-in")
    public String showCheckInForm(Model model) {
        List<BedResponse> availableBeds = bedService.getAvailableBeds();
        List<Relative> relatives = relativeService.getAllRelatives();

        model.addAttribute("checkInRequest", new ResidentCheckInRequest());
        model.addAttribute("availableBeds", availableBeds);
        model.addAttribute("relatives", relatives);
        model.addAttribute("activeMenu", "check-in");
        return "resident/check-in";
    }

    // 3. Xử lý tiếp nhận cư dân mới
    @PostMapping("/check-in")
    public String processCheckIn(
            @ModelAttribute("checkInRequest") ResidentCheckInRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ResidentResponse res = residentService.checkIn(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tiếp nhận thành công cư dân " + res.getFullName() + "! Đã xếp phòng " + res.getRoomNumber() + " - Giường " + res.getBedNumber() + ".");
            return "redirect:/residents";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tiếp nhận: " + e.getMessage());
            return "redirect:/residents/check-in";
        }
    }

    // 4. Xử lý đổi phòng / Chuyển giường cuối tháng
    @PostMapping("/transfer-bed")
    public String processTransferBed(
            @ModelAttribute BedTransferRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ResidentResponse res = residentService.transferBed(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Chuyển giường thành công cho cư dân " + res.getFullName() + " sang Phòng " + res.getRoomNumber() + " - Giường " + res.getBedNumber() + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi chuyển giường: " + e.getMessage());
        }
        return "redirect:/residents";
    }

    // 5. Xử lý thủ tục xuất viện (Check-out)
    @PostMapping("/{id}/check-out")
    public String processCheckOut(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "DISCHARGED") String finalStatus,
            RedirectAttributes redirectAttributes
    ) {
        try {
            residentService.checkOut(id, finalStatus);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hoàn tất thủ tục xuất viện cho cư dân ID " + id + ". Giường đã chuyển sang bảo trì/dọn dẹp.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xuất viện: " + e.getMessage());
        }
        return "redirect:/residents";
    }
}
