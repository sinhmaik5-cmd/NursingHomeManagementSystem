package com.group6.nhms.controller.web;

import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.service.BedService;
import com.group6.nhms.service.IncidentReportService;
import com.group6.nhms.service.ResidentService;
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
    public String index(Model model) {
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
