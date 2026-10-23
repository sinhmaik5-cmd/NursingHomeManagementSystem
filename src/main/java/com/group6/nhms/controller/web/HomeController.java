package com.group6.nhms.controller.web;

import com.group6.nhms.entity.Resident;
import com.group6.nhms.enums.BedStatus;
import com.group6.nhms.enums.IncidentStatus;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.repository.BedRepository;
import com.group6.nhms.repository.IncidentReportRepository;
import com.group6.nhms.repository.ResidentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final ResidentRepository residentRepository;
    private final BedRepository bedRepository;
    private final IncidentReportRepository incidentReportRepository;

    public HomeController(
            ResidentRepository residentRepository,
            BedRepository bedRepository,
            IncidentReportRepository incidentReportRepository
    ) {
        this.residentRepository = residentRepository;
        this.bedRepository = bedRepository;
        this.incidentReportRepository = incidentReportRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        long totalResidents = residentRepository.countByStatus(ResidentStatus.ACTIVE.name());
        long totalBeds = bedRepository.count();
        long occupiedBeds = bedRepository.countByStatus(BedStatus.OCCUPIED.name());
        long availableBeds = bedRepository.countByStatus(BedStatus.AVAILABLE.name());
        long maintenanceBeds = bedRepository.countByStatus(BedStatus.MAINTENANCE.name());
        long pendingIncidents = incidentReportRepository.countByStatus(IncidentStatus.PENDING.name());

        double occupancyRate = totalBeds > 0 ? ((double) occupiedBeds / totalBeds) * 100.0 : 0.0;
        double roundedRate = Math.round(occupancyRate * 10.0) / 10.0;

        List<Resident> recentResidents = residentRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "admissionDate", "id"))
        ).getContent();

        model.addAttribute("totalResidents", totalResidents);
        model.addAttribute("totalBeds", totalBeds);
        model.addAttribute("occupiedBeds", occupiedBeds);
        model.addAttribute("availableBeds", availableBeds);
        model.addAttribute("maintenanceBeds", maintenanceBeds);
        model.addAttribute("occupancyRate", roundedRate);
        model.addAttribute("pendingIncidents", pendingIncidents);
        model.addAttribute("recentResidents", recentResidents);
        model.addAttribute("activeMenu", "dashboard");

        return "index";
    }
}
