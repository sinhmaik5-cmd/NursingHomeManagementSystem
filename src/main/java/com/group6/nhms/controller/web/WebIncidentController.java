package com.group6.nhms.controller.web;

import com.group6.nhms.dto.request.IncidentReportCreateRequest;
import com.group6.nhms.dto.response.IncidentReportResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.IncidentReport;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.mapper.IncidentReportMapper;
import com.group6.nhms.mapper.ResidentMapper;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.repository.IncidentReportRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.IncidentReportService;
import org.springframework.data.domain.Sort;
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
    private final IncidentReportRepository incidentReportRepository;
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;
    private final AccountRepository accountRepository;
    private final ResidentMapper residentMapper;
    private final IncidentReportMapper incidentReportMapper;

    public WebIncidentController(
            IncidentReportService incidentReportService,
            IncidentReportRepository incidentReportRepository,
            ResidentRepository residentRepository,
            StaffRepository staffRepository,
            AccountRepository accountRepository,
            ResidentMapper residentMapper,
            IncidentReportMapper incidentReportMapper
    ) {
        this.incidentReportService = incidentReportService;
        this.incidentReportRepository = incidentReportRepository;
        this.residentRepository = residentRepository;
        this.staffRepository = staffRepository;
        this.accountRepository = accountRepository;
        this.residentMapper = residentMapper;
        this.incidentReportMapper = incidentReportMapper;
    }

    // 1. Giao diện báo cáo và danh sách sự cố
    @GetMapping("/report")
    public String showIncidentReport(
            @RequestParam(required = false) String status,
            Model model
    ) {
        List<ResidentResponse> residents = residentRepository.findByStatus(ResidentStatus.ACTIVE.name())
                .stream()
                .map(residentMapper::toResponse)
                .toList();

        List<IncidentReportResponse> incidents;
        if ("PENDING".equalsIgnoreCase(status)) {
            incidents = incidentReportService.getPendingIncidents();
        } else if ("RESOLVED".equalsIgnoreCase(status)) {
            incidents = incidentReportRepository.findByStatus("RESOLVED")
                    .stream()
                    .map(incidentReportMapper::toResponse)
                    .toList();
        } else {
            incidents = incidentReportRepository.findAll(Sort.by(Sort.Direction.DESC, "incidentTime", "id"))
                    .stream()
                    .map(incidentReportMapper::toResponse)
                    .toList();
        }

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
            Integer reporterStaffId = resolveStaffId(authentication);
            IncidentReportResponse res = incidentReportService.createIncident(request, reporterStaffId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã ghi nhận sự cố '" + res.getTitle() + "' cho cư dân " + res.getResidentName() + " (Mức độ: " + res.getSeverityLevel() + ").");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi gửi báo cáo sự cố: " + e.getMessage());
        }
        return "redirect:/incidents/report";
    }

    // 3. Cập nhật phương án xử lý (PENDING -> RESOLVED)
    @PostMapping("/{id}/resolve")
    public String processResolveIncident(
            @PathVariable Integer id,
            @RequestParam String actionTaken,
            RedirectAttributes redirectAttributes
    ) {
        try {
            IncidentReportResponse res = incidentReportService.resolveIncident(id, actionTaken);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xử lý thành công sự cố #" + res.getId() + " (" + res.getTitle() + ").");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật xử lý sự cố: " + e.getMessage());
        }
        return "redirect:/incidents/report";
    }

    private Integer resolveStaffId(Authentication authentication) {
        if (authentication == null) {
            return staffRepository.findAll().stream().findFirst().map(Staff::getId).orElse(1);
        }

        return accountRepository.findByUsername(authentication.getName())
                .filter(a -> a.getReferenceId() != null)
                .map(a -> a.getReferenceId())
                .orElseGet(() -> staffRepository.findAll().stream().findFirst().map(Staff::getId).orElse(1));
    }
}
