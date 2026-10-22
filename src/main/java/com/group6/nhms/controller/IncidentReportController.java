package com.group6.nhms.controller;

import com.group6.nhms.dto.request.IncidentReportCreateRequest;
import com.group6.nhms.dto.response.IncidentReportResponse;
import com.group6.nhms.enums.SeverityLevel;
import com.group6.nhms.repository.IncidentReportRepository;
import com.group6.nhms.service.IncidentReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
public class IncidentReportController {

    private final IncidentReportService incidentReportService;
    private final IncidentReportRepository incidentReportRepository;

    public IncidentReportController(
            IncidentReportService incidentReportService,
            IncidentReportRepository incidentReportRepository
    ) {
        this.incidentReportService = incidentReportService;
        this.incidentReportRepository = incidentReportRepository;
    }

    // 1. Nhân viên ghi nhận sự cố mới
    @PostMapping
    public ResponseEntity<IncidentReportResponse> createIncident(
            @Valid @RequestBody IncidentReportCreateRequest request,
            @RequestParam Integer reporterStaffId
    ) {
        IncidentReportResponse response = incidentReportService.createIncident(request, reporterStaffId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Lấy danh sách sự cố đang chờ xử lý (Status = 'PENDING')
    @GetMapping("/pending")
    public ResponseEntity<List<IncidentReportResponse>> getPendingIncidents() {
        List<IncidentReportResponse> responses = incidentReportService.getPendingIncidents();
        return ResponseEntity.ok(responses);
    }

    // 3. Cập nhật phương án xử lý sự cố (PENDING -> RESOLVED)
    @PutMapping("/{id}/resolve")
    public ResponseEntity<IncidentReportResponse> resolveIncident(
            @PathVariable Integer id,
            @RequestParam String actionTaken
    ) {
        IncidentReportResponse response = incidentReportService.resolveIncident(id, actionTaken);
        return ResponseEntity.ok(response);
    }

    // 4. Cổng thông tin người thân: Xem lịch sử sự cố của cư dân
    @GetMapping("/resident/{residentId}")
    public ResponseEntity<List<IncidentReportResponse>> getResidentIncidents(
            @PathVariable Integer residentId,
            @RequestParam(required = false) Integer relativeId
    ) {
        List<IncidentReportResponse> responses = incidentReportService.getResidentIncidents(relativeId, residentId);
        return ResponseEntity.ok(responses);
    }

    // 5. Thống kê sự cố trong 30 ngày gần nhất theo mức độ nghiêm trọng (Dashboard Admin)
    @GetMapping("/statistics/monthly")
    public ResponseEntity<Map<String, Long>> getMonthlyIncidentStats() {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(30, ChronoUnit.DAYS);

        Map<String, Long> stats = new HashMap<>();
        for (SeverityLevel level : SeverityLevel.values()) {
            long count = incidentReportRepository.countBySeverityLevelAndIncidentTimeBetween(
                    level.name(), startDate, endDate
            );
            stats.put(level.name(), count);
        }

        return ResponseEntity.ok(stats);
    }
}
