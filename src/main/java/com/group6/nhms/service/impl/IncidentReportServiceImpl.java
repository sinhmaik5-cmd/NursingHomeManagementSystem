package com.group6.nhms.service.impl;

import com.group6.nhms.dto.request.IncidentReportCreateRequest;
import com.group6.nhms.dto.response.IncidentReportResponse;
import com.group6.nhms.entity.Bed;
import com.group6.nhms.entity.IncidentReport;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Room;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.enums.BedStatus;
import com.group6.nhms.enums.IncidentStatus;
import com.group6.nhms.enums.SeverityLevel;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.IncidentReportMapper;
import com.group6.nhms.repository.BedRepository;
import com.group6.nhms.repository.IncidentReportRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.RoomRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.IncidentReportService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IncidentReportServiceImpl implements IncidentReportService {

    private final IncidentReportRepository incidentReportRepository;
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;
    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;
    private final IncidentReportMapper incidentReportMapper;

    public IncidentReportServiceImpl(
            IncidentReportRepository incidentReportRepository,
            ResidentRepository residentRepository,
            StaffRepository staffRepository,
            BedRepository bedRepository,
            RoomRepository roomRepository,
            IncidentReportMapper incidentReportMapper
    ) {
        this.incidentReportRepository = incidentReportRepository;
        this.residentRepository = residentRepository;
        this.staffRepository = staffRepository;
        this.bedRepository = bedRepository;
        this.roomRepository = roomRepository;
        this.incidentReportMapper = incidentReportMapper;
    }

    @Override
    @Transactional
    public IncidentReportResponse createIncident(
            IncidentReportCreateRequest request,
            Integer reporterStaffId
    ) {
        Staff reporter = staffRepository.findById(reporterStaffId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy nhân viên ID " + reporterStaffId));

        if ("CENTER_MANAGER".equalsIgnoreCase(reporter.getJobTitle())) {
            throw new BadRequestException("Giám đốc cơ sở giữ vai trò quản lý và điều hành xử lý sự cố, không trực tiếp báo cáo sự cố.");
        }

        Resident resident = null;
        if (request.getResidentId() != null) {
            resident = residentRepository.findById(request.getResidentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy cư dân ID " + request.getResidentId()));
        }

        String severity = normalizeSeverity(request.getSeverityLevel());

        if ("NURSE".equalsIgnoreCase(reporter.getJobTitle())) {
            if (resident == null) {
                throw new BadRequestException("Điều dưỡng báo cáo sự cố lâm sàng bắt buộc phải gắn với cư dân cụ thể.");
            }
            if (!"LOW".equalsIgnoreCase(severity) && !"MEDIUM".equalsIgnoreCase(severity)) {
                throw new BadRequestException("Điều dưỡng chỉ báo cáo các sự cố mức độ LOW hoặc MEDIUM cho Bác sĩ và Giám đốc. Sự cố nặng (HIGH) và cấp cứu (CRITICAL) do Bác sĩ đánh giá và báo cáo.");
            }
        } else if ("DOCTOR".equalsIgnoreCase(reporter.getJobTitle())) {
            if (resident == null) {
                throw new BadRequestException("Bác sĩ báo cáo sự cố y tế bắt buộc phải gắn với cư dân cụ thể.");
            }
            if (!"HIGH".equalsIgnoreCase(severity) && !"CRITICAL".equalsIgnoreCase(severity)) {
                throw new BadRequestException("Bác sĩ chuyên trách báo cáo các sự cố y tế mức độ HIGH hoặc CRITICAL để Giám đốc xử lý và chỉ đạo.");
            }
        } else if ("RECEPTIONIST".equalsIgnoreCase(reporter.getJobTitle())) {
            // Nâng cấp 3: Lễ tân có thể báo cáo theo Cư dân, Phòng/Giường hoặc Khu vực cơ sở vật chất chung
            if (resident == null && request.getBedId() == null && request.getRoomId() == null
                    && (request.getLocation() == null || request.getLocation().isBlank())) {
                throw new BadRequestException("Vui lòng chọn Cư dân, Phòng/Giường hoặc Khu vực cơ sở vật chất gặp sự cố.");
            }
        }

        IncidentReport incident = new IncidentReport();
        incident.setReporterStaff(reporter);
        incident.setIncidentTime(Instant.now());
        String description = request.getDescription() != null ? request.getDescription().trim() : "";
        String title = request.getTitle() != null ? request.getTitle().trim() : "";
        incident.setDescription(title.isEmpty() ? description : "[" + title + "] " + description);
        incident.setSeverityLevel(severity);
        incident.setStatus(IncidentStatus.PENDING.name());

        // Gắn Cư dân, Giường, Phòng & Vị trí
        if (resident != null) {
            incident.setResident(resident);
            if (resident.getBed() != null) {
                incident.setBed(resident.getBed());
                if (resident.getBed().getRoom() != null) {
                    incident.setRoom(resident.getBed().getRoom());
                }
            }
        }

        if (request.getBedId() != null) {
            Bed bed = bedRepository.findById(request.getBedId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giường ID " + request.getBedId()));
            incident.setBed(bed);
            if (bed.getRoom() != null && incident.getRoom() == null) {
                incident.setRoom(bed.getRoom());
            }
        }

        if (request.getRoomId() != null && incident.getRoom() == null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ID " + request.getRoomId()));
            incident.setRoom(room);
        }

        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            incident.setLocation(request.getLocation().trim());
        }

        // Nâng cấp 2: Tự động chuyển giường sang trạng thái MAINTENANCE nếu là sự cố cơ sở vật chất
        if (Boolean.TRUE.equals(request.getSetMaintenance()) && incident.getBed() != null) {
            Bed targetBed = incident.getBed();
            if (!BedStatus.MAINTENANCE.name().equalsIgnoreCase(targetBed.getStatus())) {
                targetBed.setStatus(BedStatus.MAINTENANCE.name());
                bedRepository.save(targetBed);
            }
        }

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
    @Transactional(readOnly = true)
    public Map<Integer, List<IncidentReportResponse>> getResidentIncidentsByResidentIds(
            Integer relativeId,
            List<Integer> residentIds
    ) {
        if (residentIds.isEmpty()) {
            return Map.of();
        }

        HashSet<Integer> requestedResidentIds = new HashSet<>(residentIds);
        List<Resident> linkedResidents = residentRepository.findByRelativeIdAndIdIn(
                relativeId, requestedResidentIds);
        if (linkedResidents.size() != requestedResidentIds.size()) {
            throw new BadRequestException("Bạn không có quyền xem thông tin sự cố của một hoặc nhiều cư dân");
        }

        List<Integer> linkedResidentIds = linkedResidents.stream().map(Resident::getId).toList();
        return incidentReportRepository.findByResidentIdIn(linkedResidentIds).stream()
                .map(incidentReportMapper::toResponse)
                .collect(Collectors.groupingBy(IncidentReportResponse::getResidentId));
    }

    @Override
    @Transactional
    public IncidentReportResponse resolveIncident(Integer incidentId, String actionTaken) {
        return resolveIncident(incidentId, actionTaken, null);
    }

    @Override
    @Transactional
    public IncidentReportResponse resolveIncident(Integer incidentId, String actionTaken, Integer resolverStaffId) {
        if (actionTaken == null || actionTaken.isBlank()) {
            throw new BadRequestException("Hướng xử lý không được để trống");
        }

        IncidentReport incident = incidentReportRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy báo cáo sự cố ID " + incidentId));

        if (!IncidentStatus.PENDING.name().equals(incident.getStatus())) {
            throw new BadRequestException("Chỉ có thể xử lý sự cố đang ở trạng thái PENDING");
        }

        if (resolverStaffId != null) {
            Staff resolver = staffRepository.findById(resolverStaffId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên xử lý ID " + resolverStaffId));
            String resolverRole = resolver.getJobTitle();
            if ("NURSE".equalsIgnoreCase(resolverRole)) {
                throw new BadRequestException("Điều dưỡng không có thẩm quyền xử lý sự cố.");
            }

            Staff reporter = incident.getReporterStaff();
            String reporterRole = reporter != null ? reporter.getJobTitle() : "";

            if ("RECEPTIONIST".equalsIgnoreCase(resolverRole)) {
                if (!"RECEPTIONIST".equalsIgnoreCase(reporterRole) && incident.getReporterStaff() != null) {
                    throw new BadRequestException("Lễ tân chỉ có thẩm quyền báo cáo giải quyết các sự cố cơ sở vật chất.");
                }
            } else if ("DOCTOR".equalsIgnoreCase(resolverRole)) {
                if ("RECEPTIONIST".equalsIgnoreCase(reporterRole)) {
                    throw new BadRequestException("Bác sĩ không xử lý các sự cố cơ sở vật chất của Lễ tân.");
                }
                if ("CRITICAL".equalsIgnoreCase(incident.getSeverityLevel())) {
                    throw new BadRequestException("Sự cố y tế mức độ khẩn cấp / nguy kịch (CRITICAL) thuộc thẩm quyền xử lý và chỉ đạo của Giám đốc cơ sở.");
                }
            } else if ("CENTER_MANAGER".equalsIgnoreCase(resolverRole)) {
                if ("NURSE".equalsIgnoreCase(reporterRole)) {
                    throw new BadRequestException("Sự cố do Điều dưỡng báo cáo thuộc thẩm quyền chuyên môn của Bác sĩ trực tiếp nhận và xử lý y tế.");
                }
            }
        }

        String finalAction = actionTaken.trim();

        // Nâng cấp 2: Tự động khôi phục trạng thái giường sau khi Giám đốc xử lý xong sự cố cơ sở vật chất
        if (incident.getBed() != null) {
            Bed targetBed = incident.getBed();
            if (BedStatus.MAINTENANCE.name().equalsIgnoreCase(targetBed.getStatus())) {
                Optional<Resident> activeResident = residentRepository.findByBedId(targetBed.getId())
                        .filter(r -> "ACTIVE".equalsIgnoreCase(r.getStatus()));
                if (activeResident.isPresent()) {
                    targetBed.setStatus(BedStatus.OCCUPIED.name());
                } else {
                    targetBed.setStatus(BedStatus.AVAILABLE.name());
                }
                bedRepository.save(targetBed);
                finalAction += " [Hệ thống: Đã khôi phục trạng thái giường " + targetBed.getBedNumber() + " -> " + targetBed.getStatus() + "]";
            }
        }

        incident.setActionTaken(finalAction);
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
