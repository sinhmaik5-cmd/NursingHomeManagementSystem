package com.group6.nhms.controller.web;

import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.IncidentReportResponse;
import com.group6.nhms.dto.response.OrderResponse;
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

import java.util.ArrayList;
import java.util.List;

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

        List<ResidentResponse> residentResponses = new ArrayList<>();
        HealthRecordResponse latestRecord = null;
        List<IncidentReportResponse> residentIncidents = new ArrayList<>();
        List<OrderResponse> residentOrders = new ArrayList<>();

        if (relative != null) {
            residentResponses = residentService.getResidentsByRelative(relative.getId());

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
}
