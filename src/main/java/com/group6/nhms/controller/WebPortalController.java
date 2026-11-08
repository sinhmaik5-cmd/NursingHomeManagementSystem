package com.group6.nhms.controller;

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
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @GetMapping("/incidents")
    public String portalIncidents(
            @RequestParam(required = false) Integer residentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            Authentication authentication,
            Model model
    ) {
        Relative relative = relativeService.resolveCurrentRelative(authentication);

        List<ResidentResponse> residentResponses = List.of();
        ResidentResponse selectedResident = null;
        List<IncidentReportResponse> incidents = List.of();

        if (relative != null) {
            residentResponses = residentService.getResidentsByRelative(relative.getId());
            if (!residentResponses.isEmpty()) {
                if (residentId != null) {
                    selectedResident = residentResponses.stream()
                            .filter(r -> r.getId().equals(residentId))
                            .findFirst()
                            .orElse(null);
                }
                if (selectedResident == null) {
                    selectedResident = residentResponses.get(0);
                }

                Map<Integer, List<IncidentReportResponse>> incidentMap =
                        incidentReportService.getResidentIncidentsByResidentIds(relative.getId(), List.of(selectedResident.getId()));
                incidents = incidentMap.getOrDefault(selectedResident.getId(), List.of());

                // Lọc theo trạng thái nếu có
                if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                    final String filterSt = status;
                    incidents = incidents.stream()
                            .filter(i -> filterSt.equalsIgnoreCase(i.getStatus()))
                            .toList();
                }

                // Sắp xếp theo ngày ghi nhận
                if ("oldest".equalsIgnoreCase(sort)) {
                    incidents = incidents.stream()
                            .sorted(Comparator.comparing(IncidentReportResponse::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())))
                            .toList();
                } else {
                    incidents = incidents.stream()
                            .sorted(Comparator.comparing(IncidentReportResponse::getCreatedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                            .toList();
                }
            }
        }

        model.addAttribute("relative", relative);
        model.addAttribute("residents", residentResponses);
        model.addAttribute("selectedResident", selectedResident);
        model.addAttribute("incidents", incidents);
        model.addAttribute("filterStatus", status);
        model.addAttribute("currentSort", "oldest".equalsIgnoreCase(sort) ? "oldest" : "newest");
        long pendingCount = incidents.stream().filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus())).count();
        long resolvedCount = incidents.stream().filter(i -> "RESOLVED".equalsIgnoreCase(i.getStatus())).count();
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("activeMenu", "portal-incidents");

        return "portal/incidents";
    }

    @GetMapping("/bills")
    public String portalBills(
            @RequestParam(required = false) Integer residentId,
            @RequestParam(required = false) String status,
            Authentication authentication,
            Model model
    ) {
        Relative relative = relativeService.resolveCurrentRelative(authentication);

        List<ResidentResponse> residentResponses = List.of();
        ResidentResponse selectedResident = null;
        List<OrderResponse> allResidentOrders = List.of();
        List<OrderResponse> orders = List.of();

        if (relative != null) {
            residentResponses = residentService.getResidentsByRelative(relative.getId());
            if (!residentResponses.isEmpty()) {
                if (residentId != null) {
                    selectedResident = residentResponses.stream()
                            .filter(r -> r.getId().equals(residentId))
                            .findFirst()
                            .orElse(null);
                }
                if (selectedResident == null) {
                    selectedResident = residentResponses.get(0);
                }

                allResidentOrders = orderService.getOrdersByResident(selectedResident.getId());

                if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                    final String filterSt = status;
                    orders = allResidentOrders.stream()
                            .filter(o -> filterSt.equalsIgnoreCase(o.getPaymentStatus()))
                            .toList();
                } else {
                    orders = allResidentOrders;
                }
            }
        }

        long unpaidCount = allResidentOrders.stream()
                .filter(o -> "PENDING".equalsIgnoreCase(o.getPaymentStatus()) || "UNPAID".equalsIgnoreCase(o.getPaymentStatus()))
                .count();
        long paidCount = allResidentOrders.stream()
                .filter(o -> "PAID".equalsIgnoreCase(o.getPaymentStatus()))
                .count();
        BigDecimal totalUnpaidAmount = allResidentOrders.stream()
                .filter(o -> "PENDING".equalsIgnoreCase(o.getPaymentStatus()) || "UNPAID".equalsIgnoreCase(o.getPaymentStatus()))
                .map(OrderResponse::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("relative", relative);
        model.addAttribute("residents", residentResponses);
        model.addAttribute("selectedResident", selectedResident);
        model.addAttribute("orders", orders);
        model.addAttribute("totalBillsCount", allResidentOrders.size());
        model.addAttribute("unpaidCount", unpaidCount);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("totalUnpaidAmount", totalUnpaidAmount);
        model.addAttribute("filterStatus", status);
        model.addAttribute("activeMenu", "portal-bills");

        return "portal/bills";
    }
}
