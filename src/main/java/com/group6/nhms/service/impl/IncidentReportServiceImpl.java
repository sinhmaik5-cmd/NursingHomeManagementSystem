package com.group6.nhms.service.impl;

import com.group6.nhms.dto.request.IncidentReportCreateRequest;
import com.group6.nhms.dto.response.IncidentReportResponse;
import com.group6.nhms.entity.IncidentReport;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.enums.IncidentStatus;
import com.group6.nhms.enums.SeverityLevel;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.IncidentReportMapper;
import com.group6.nhms.repository.IncidentReportRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.IncidentReportService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IncidentReportServiceImpl implements IncidentReportService {

    private final IncidentReportRepository incidentReportRepository;
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;
    private final IncidentReportMapper incidentReportMapper;

    public IncidentReportServiceImpl(
            IncidentReportRepository incidentReportRepository,
            ResidentRepository residentRepository,
            StaffRepository staffRepository,
            IncidentReportMapper incidentReportMapper
    ) {
        this.incidentReportRepository = incidentReportRepository;
        this.residentRepository = residentRepository;
        this.staffRepository = staffRepository;
        this.incidentReportMapper = incidentReportMapper;
    }

    @Override
    @Transactional
    public IncidentReportResponse createIncident(
            IncidentReportCreateRequest request,
            Integer reporterStaffId
    ) {
        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy cư dân ID " + request.getResidentId()));

        Staff reporter = staffRepository.findById(reporterStaffId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy nhân viên ID " + reporterStaffId));

        String severity = normalizeSeverity(request.getSeverityLevel());

        IncidentReport incident = new IncidentReport();
        incident.setResident(resident);
        incident.setReporterStaff(reporter);
        incident.setIncidentTime(Instant.now());
        String description = request.getDescription() != null ? request.getDescription().trim() : "";
        String title = request.getTitle() != null ? request.getTitle().trim() : "";
        incident.setDescription(title.isEmpty() ? description : "[" + title + "] " + description);
        incident.setSeverityLevel(severity);
        incident.setStatus(IncidentStatus.PENDING.name());

        IncidentReport saved = incidentReportRepository.save(incident);
        return incidentReportMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentReportResponse> getPendingIncidents() {
        return incidentReportRepository.findByStatusOrderByIncidentTimeDesc(
                IncidentStatus.PENDING.name()
        ).stream().map(incidentReportMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentReportResponse> getResidentIncidents(
            Integer relativeId,
            Integer residentId
    ) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy cư dân ID " + residentId));

        if (relativeId != null) {
            if (resident.getRelative() == null || !relativeId.equals(resident.getRelative().getId())) {
                throw new BadRequestException("Bạn không có quyền xem thông tin sự cố của cư dân này");
            }
        }

        return incidentReportRepository.findByResidentId(residentId)
                .stream()
                .map(incidentReportMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public IncidentReportResponse resolveIncident(Integer incidentId, String actionTaken) {
        if (actionTaken == null || actionTaken.isBlank()) {
            throw new BadRequestException("Hướng xử lý không được để trống");
        }

        IncidentReport incident = incidentReportRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy báo cáo sự cố ID " + incidentId));

        if (!IncidentStatus.PENDING.name().equals(incident.getStatus())) {
            throw new BadRequestException("Chỉ có thể xử lý sự cố đang ở trạng thái PENDING");
        }

        incident.setActionTaken(actionTaken.trim());
        incident.setStatus(IncidentStatus.RESOLVED.name());

        return incidentReportMapper.toResponse(incidentReportRepository.save(incident));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentReportResponse> getAllIncidents(String status) {
        if ("PENDING".equalsIgnoreCase(status)) {
            return getPendingIncidents();
        } else if ("RESOLVED".equalsIgnoreCase(status)) {
            return incidentReportRepository.findByStatus("RESOLVED")
                    .stream()
                    .map(incidentReportMapper::toResponse)
                    .toList();
        } else {
            return incidentReportRepository.findAll(Sort.by(Sort.Direction.DESC, "incidentTime", "id"))
                    .stream()
                    .map(incidentReportMapper::toResponse)
                    .toList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingIncidents() {
        return incidentReportRepository.countByStatus(IncidentStatus.PENDING.name());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getMonthlyIncidentStats() {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(30, ChronoUnit.DAYS);

        Map<String, Long> stats = new HashMap<>();
        for (SeverityLevel level : SeverityLevel.values()) {
            long count = incidentReportRepository.countBySeverityLevelAndIncidentTimeBetween(
                    level.name(), startDate, endDate
            );
            stats.put(level.name(), count);
        }
        return stats;
    }

    private String normalizeSeverity(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Vui lòng chọn mức độ nghiêm trọng của sự cố");
        }

        try {
            return SeverityLevel.valueOf(value.trim().toUpperCase()).name();
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(
                    "Mức độ sự cố không hợp lệ. Giá trị hợp lệ: LOW, MEDIUM, HIGH, CRITICAL");
        }
    }
}
