package com.group6.nhms.controller;

import com.group6.nhms.dto.request.IncidentReportCreateRequest;
import com.group6.nhms.dto.response.BedResponse;
import com.group6.nhms.dto.response.IncidentReportResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.dto.response.RoomResponse;
import com.group6.nhms.service.BedService;
import com.group6.nhms.service.IncidentReportService;
import com.group6.nhms.service.ResidentService;
import com.group6.nhms.service.RoomService;
import com.group6.nhms.service.StaffService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/incidents")
public class WebIncidentController {

    private final IncidentReportService incidentReportService;
    private final ResidentService residentService;
    private final StaffService staffService;
    private final RoomService roomService;
    private final BedService bedService;

    public WebIncidentController(
            IncidentReportService incidentReportService,
            ResidentService residentService,
            StaffService staffService,
            RoomService roomService,
            BedService bedService
    ) {
        this.incidentReportService = incidentReportService;
        this.residentService = residentService;
        this.staffService = staffService;
        this.roomService = roomService;
        this.bedService = bedService;
    }

    // 1. Giao diện báo cáo và danh sách sự cố
    @GetMapping("/report")
    public String showIncidentReport(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            Authentication authentication,
            Model model
    ) {
        List<ResidentResponse> residents = residentService.getAllResidents(null, "ACTIVE");
        List<IncidentReportResponse> incidents = incidentReportService.getAllIncidents(status);
        List<RoomResponse> rooms = roomService.getAllRooms();
        List<BedResponse> beds = bedService.getAllBeds();

        // Sắp xếp theo thời gian: newest (mới nhất trước - mặc định) hoặc oldest (cũ nhất trước)
        if ("oldest".equalsIgnoreCase(sort)) {
            incidents = incidents.stream()
                    .sorted(Comparator.comparing(IncidentReportResponse::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
        } else {
            incidents = incidents.stream()
                    .sorted(Comparator.comparing(IncidentReportResponse::getCreatedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        }

        // Tách riêng 2 mảng báo cáo cho Giám đốc cơ sở & Lễ tân
        List<IncidentReportResponse> receptionistIncidents = incidents.stream()
                .filter(IncidentReportResponse::isReportedByReceptionist)
                .toList();
        List<IncidentReportResponse> clinicalIncidents = incidents.stream()
                .filter(i -> i.isReportedByDoctor() || i.isReportedByNurse())
                .toList();

        // Thống kê nhanh số lượng sự cố cho Dashboard Giám đốc cơ sở
        long pendingCount = incidents.stream()
                .filter(i -> "PENDING".equalsIgnoreCase(i.getStatus()))
                .count();
        long urgentCount = incidents.stream()
                .filter(i -> "CRITICAL".equalsIgnoreCase(i.getSeverityLevel()) || "HIGH".equalsIgnoreCase(i.getSeverityLevel()))
                .count();
        long resolvedCount = incidents.stream()
                .filter(i -> "RESOLVED".equalsIgnoreCase(i.getStatus()))
                .count();

        IncidentReportCreateRequest request = new IncidentReportCreateRequest();
        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
            request.setSeverityLevel("HIGH");
        } else {
            request.setSeverityLevel("LOW");
        }

        model.addAttribute("incidentRequest", request);
        model.addAttribute("residents", residents);
        model.addAttribute("incidents", incidents);
        model.addAttribute("receptionistIncidents", receptionistIncidents);
        model.addAttribute("clinicalIncidents", clinicalIncidents);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("urgentCount", urgentCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("rooms", rooms);
        model.addAttribute("beds", beds);
        model.addAttribute("filterStatus", status);
        model.addAttribute("currentSort", "oldest".equalsIgnoreCase(sort) ? "oldest" : "newest");
        model.addAttribute("activeMenu", "incident-report");

        return "incident/report";
    }

    // 2. Xử lý ghi nhận sự cố mới (Chỉ nhân viên trực ca: Điều dưỡng, Bác sĩ, Lễ tân)
    @PostMapping("/report")
    public String processCreateIncident(
            @ModelAttribute IncidentReportCreateRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CENTER_MANAGER"))) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Giám đốc cơ sở giữ vai trò quản lý và điều hành xử lý sự cố, không trực tiếp gửi báo cáo sự cố.");
            return "redirect:/incidents/report";
        }
        try {
            Integer reporterStaffId = staffService.resolveCurrentStaffId(authentication);
            IncidentReportResponse res = incidentReportService.createIncident(request, reporterStaffId);
            String targetDesc = res.getDisplayTarget();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã ghi nhận sự cố '" + res.getTitle() + "' tại [" + targetDesc + "] (Mức độ: " + res.getSeverityLevel() + ").");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi gửi báo cáo sự cố: " + e.getMessage());
        }
        return "redirect:/incidents/report";
    }

    // 3. Cập nhật phương án xử lý (PENDING -> RESOLVED) - Chỉ Bác sĩ, Giám đốc cơ sở & Quản trị viên
    @PostMapping("/{id}/resolve")
    public String resolveIncident(
            @PathVariable Integer id,
            @RequestParam String actionTaken,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Integer resolverStaffId = staffService.resolveCurrentStaffId(authentication);
            incidentReportService.resolveIncident(id, actionTaken, resolverStaffId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã cập nhật phương án khắc phục và giải quyết sự cố thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xử lý sự cố: " + e.getMessage());
        }
        return "redirect:/incidents/report";
    }
}
