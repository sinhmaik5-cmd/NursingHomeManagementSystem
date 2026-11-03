package com.group6.nhms.controller.web;

import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.service.BedService;
import com.group6.nhms.service.IncidentReportService;
import com.group6.nhms.service.ResidentService;
import org.springframework.security.core.Authentication; // Đã thêm thư viện Authentication
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final ResidentService residentService;
    private final BedService bedService;
    private final IncidentReportService incidentReportService;

    public HomeController(
            ResidentService residentService,
            BedService bedService,
            IncidentReportService incidentReportService
    ) {
        this.residentService = residentService;
        this.bedService = bedService;
        this.incidentReportService = incidentReportService;
    }

    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        // 1. Khách chưa đăng nhập -> Hiển thị trang chủ công khai (Landing Page có nút Login góc phải)
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return "landing";
        }

        // 2. Người dùng là Thân nhân (RELATIVE) -> Chuyển hướng thẳng vào Cổng Portal
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RELATIVE"))) {
            return "redirect:/portal/dashboard";
        }

        // 3. Quản lý / Nhân viên -> Hiển thị Dashboard thống kê nội bộ (index.html) như cũ
        long totalResidents = residentService.countActiveResidents();
        long totalBeds = bedService.countTotalBeds();
        long occupiedBeds = bedService.countOccupiedBeds();
        long availableBeds = bedService.countAvailableBeds();
        long maintenanceBeds = bedService.countMaintenanceBeds();
        long pendingIncidents = incidentReportService.countPendingIncidents();
        double occupancyRate = bedService.getOccupancyRate();

        List<ResidentResponse> recentResidents = residentService.getRecentResidents(5);

        model.addAttribute("totalResidents", totalResidents);
        model.addAttribute("totalBeds", totalBeds);
        model.addAttribute("occupiedBeds", occupiedBeds);
        model.addAttribute("availableBeds", availableBeds);
        model.addAttribute("maintenanceBeds", maintenanceBeds);
        model.addAttribute("occupancyRate", occupancyRate);
        model.addAttribute("pendingIncidents", pendingIncidents);
        model.addAttribute("recentResidents", recentResidents);
        model.addAttribute("activeMenu", "dashboard");

        return "index";
    }
}