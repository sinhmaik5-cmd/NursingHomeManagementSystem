package com.group6.nhms.controller.web;

import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.IncidentReportResponse;
import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.mapper.ResidentMapper;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.repository.RelativeRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.service.HealthRecordService;
import com.group6.nhms.service.IncidentReportService;
import com.group6.nhms.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/portal")
public class WebPortalController {

    private final RelativeRepository relativeRepository;
    private final ResidentRepository residentRepository;
    private final AccountRepository accountRepository;
    private final HealthRecordService healthRecordService;
    private final IncidentReportService incidentReportService;
    private final OrderService orderService;
    private final ResidentMapper residentMapper;

    public WebPortalController(
            RelativeRepository relativeRepository,
            ResidentRepository residentRepository,
            AccountRepository accountRepository,
            HealthRecordService healthRecordService,
            IncidentReportService incidentReportService,
            OrderService orderService,
            ResidentMapper residentMapper
    ) {
        this.relativeRepository = relativeRepository;
        this.residentRepository = residentRepository;
        this.accountRepository = accountRepository;
        this.healthRecordService = healthRecordService;
        this.incidentReportService = incidentReportService;
        this.orderService = orderService;
        this.residentMapper = residentMapper;
    }

    @GetMapping("/dashboard")
    public String portalDashboard(Authentication authentication, Model model) {
        Relative relative = resolveRelative(authentication);

        List<ResidentResponse> residentResponses = new ArrayList<>();
        HealthRecordResponse latestRecord = null;
        List<IncidentReportResponse> residentIncidents = new ArrayList<>();
        List<OrderResponse> residentOrders = new ArrayList<>();

        if (relative != null) {
            List<Resident> residents = residentRepository.findByRelativeId(relative.getId());
            if (residents.isEmpty()) {
                // Fallback for demo if relative doesn't have a linked resident yet
                residents = residentRepository.findAll();
            }

            residentResponses = residents.stream()
                    .map(residentMapper::toResponse)
                    .toList();

            if (!residentResponses.isEmpty()) {
                Integer primaryResidentId = residentResponses.get(0).getId();

                List<HealthRecordResponse> records = healthRecordService.getRecordsByResident(primaryResidentId);
                if (!records.isEmpty()) {
                    latestRecord = records.get(0);
                }

                residentIncidents = incidentReportService.getResidentIncidents(relative.getId(), primaryResidentId);
                residentOrders = orderService.getOrdersByResident(primaryResidentId);
            }
        }

        model.addAttribute("relative", relative);
        model.addAttribute("residents", residentResponses);
        model.addAttribute("latestRecord", latestRecord);
        model.addAttribute("residentIncidents", residentIncidents);
        model.addAttribute("residentOrders", residentOrders);
        model.addAttribute("activeMenu", "portal-dashboard");

        return "portal/dashboard";
    }

    private Relative resolveRelative(Authentication authentication) {
        if (authentication == null) {
            return relativeRepository.findAll().stream().findFirst().orElse(null);
        }

        return accountRepository.findByUsername(authentication.getName())
                .filter(a -> a.getReferenceId() != null)
                .flatMap(a -> relativeRepository.findById(a.getReferenceId()))
                .orElseGet(() -> relativeRepository.findAll().stream().findFirst().orElse(null));
    }
}
