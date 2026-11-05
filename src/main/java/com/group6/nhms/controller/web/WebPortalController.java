package com.group6.nhms.controller.web;

import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.IncidentReportResponse;
import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.dto.response.ResidentPortalResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.service.HealthRecordService;
import com.group6.nhms.service.IncidentReportService;
import com.group6.nhms.service.OrderService;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.ResidentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/portal")
public class WebPortalController {

    private final RelativeService relativeService;
    private final ResidentService residentService;
    private final HealthRecordService healthRecordService;
    private final IncidentReportService incidentReportService;
    private final OrderService orderService;

    public WebPortalController(
            RelativeService relativeService,
            ResidentService residentService,
            HealthRecordService healthRecordService,
            IncidentReportService incidentReportService,
            OrderService orderService
    ) {
        this.relativeService = relativeService;
        this.residentService = residentService;
        this.healthRecordService = healthRecordService;
        this.incidentReportService = incidentReportService;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public String portalDashboard(Authentication authentication, Model model) {
        Relative relative = relativeService.resolveCurrentRelative(authentication);

        List<ResidentResponse> residentResponses = List.of();
        List<ResidentPortalResponse> residentPortals = List.of();

        if (relative != null) {
            residentResponses = residentService.getResidentsByRelative(relative.getId());
            List<Integer> residentIds = residentResponses.stream()
                    .map(ResidentResponse::getId)
                    .toList();
            Map<Integer, HealthRecordResponse> latestRecords =
                    healthRecordService.getLatestRecordsByResidents(residentIds);
            Map<Integer, List<IncidentReportResponse>> incidents =
                    incidentReportService.getResidentIncidentsByResidentIds(relative.getId(), residentIds);
            Map<Integer, List<OrderResponse>> orders = orderService.getOrdersByResidents(residentIds);

            residentPortals = residentResponses.stream()
                    .map(resident -> {
                        Integer residentId = resident.getId();
                        return new ResidentPortalResponse(
                                resident,
                                latestRecords.get(residentId),
                                incidents.getOrDefault(residentId, List.of()),
                                orders.getOrDefault(residentId, List.of()));
                    })
                    .toList();
        }

        model.addAttribute("relative", relative);
        model.addAttribute("residents", residentResponses);
        model.addAttribute("residentPortals", residentPortals);
        model.addAttribute("activeMenu", "portal-dashboard");

        return "portal/dashboard";
    }
}
