package com.group6.nhms.controller.web;

import com.group6.nhms.dto.request.IncidentReportCreateRequest;
import com.group6.nhms.dto.response.IncidentReportResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.service.IncidentReportService;
import com.group6.nhms.service.ResidentService;
import com.group6.nhms.service.StaffService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/incidents")
public class WebIncidentController {

    private final IncidentReportService incidentReportService;
    private final ResidentService residentService;
    private final StaffService staffService;

    public WebIncidentController(
            IncidentReportService incidentReportService,
            ResidentService residentService,
            StaffService staffService
    ) {
        this.incidentReportService = incidentReportService;
        this.residentService = residentService;
        this.staffService = staffService;
    }

    // 1. Giao diện báo cáo và danh sách sự cố
    @GetMapping("/report")
    public String showIncidentReport(
            @RequestParam(required = false) String status,
            Model model
    ) {
        List<ResidentResponse> residents = residentService.getAllResidents(null, "ACTIVE");
        List<IncidentReportResponse> incidents = incidentReportService.getAllIncidents(status);

        IncidentReportCreateRequest request = new IncidentReportCreateRequest();
        request.setSeverityLevel("LOW");

        model.addAttribute("incidentRequest", request);
        model.addAttribute("residents", residents);
        model.addAttribute("incidents", incidents);
        model.addAttribute("filterStatus", status);
        model.addAttribute("activeMenu", "incident-report");

        return "incident/report";
    }

    // 2. Xử lý ghi nhận sự cố mới
    @PostMapping("/report")
    public String processCreateIncident(
            @ModelAttribute IncidentReportCreateRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Integer reporterStaffId = staffService.resolveCurrentStaffId(authentication);
            IncidentReportResponse res = incidentReportService.createIncident(request, reporterStaffId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã ghi nhận sự cố '" + res.getTitle() + "' cho cư dân " + res.getResidentName() + " (Mức độ: " + res.getSeverityLevel() + ").");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi gửi báo cáo sự cố: " + e.getMessage());
        }
        return "redirect:/incidents/report";
    }

    // 3. Cập nhật phương án xử lý (PENDING -> RESOLVED) - Chỉ Bác sĩ, Giám đốc cơ sở & Quản trị viên
    @PostMapping("/{id}/resolve")
    public String processResolveIncident(
            @PathVariable Integer id,
            @RequestParam String actionTaken,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_DOCTOR") || a.getAuthority().equals("ROLE_CENTER_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền cập nhật phương án xử lý sự cố.");
            return "redirect:/incidents/report";
        }

        try {
            IncidentReportResponse res = incidentReportService.resolveIncident(id, actionTaken);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xử lý thành công sự cố #" + res.getId() + " (" + res.getTitle() + ").");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật xử lý sự cố: " + e.getMessage());
        }
        return "redirect:/incidents/report";
    }
}
