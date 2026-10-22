package com.group6.nhms.controller;

import com.group6.nhms.dto.request.BedTransferRequest;
import com.group6.nhms.dto.request.ResidentCheckInRequest;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.ResidentMapper;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.service.ResidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/residents")
public class ResidentController {

    private final ResidentService residentService;
    private final ResidentRepository residentRepository;
    private final ResidentMapper residentMapper;

    public ResidentController(
            ResidentService residentService,
            ResidentRepository residentRepository,
            ResidentMapper residentMapper
    ) {
        this.residentService = residentService;
        this.residentRepository = residentRepository;
        this.residentMapper = residentMapper;
    }

    // 1. Nghiệp vụ Tiếp nhận Cư dân mới (Check-in)
    @PostMapping("/check-in")
    public ResponseEntity<ResidentResponse> checkIn(@Valid @RequestBody ResidentCheckInRequest request) {
        ResidentResponse response = residentService.checkIn(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Nghiệp vụ Đổi phòng / Chuyển giường (Bed Transfer)
    @PutMapping("/transfer-bed")
    public ResponseEntity<ResidentResponse> transferBed(@Valid @RequestBody BedTransferRequest request) {
        ResidentResponse response = residentService.transferBed(request);
        return ResponseEntity.ok(response);
    }

    // 3. Nghiệp vụ Xuất viện (Check-out)
    @PostMapping("/{id}/check-out")
    public ResponseEntity<String> checkOut(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "DISCHARGED") String finalStatus
    ) {
        residentService.checkOut(id, finalStatus);
        return ResponseEntity.ok("Hoàn tất thủ tục xuất viện cho cư dân ID " + id);
    }

    // 4. Lấy danh sách cư dân (hỗ trợ lọc theo status hoặc tìm kiếm theo tên)
    @GetMapping
    public ResponseEntity<List<ResidentResponse>> getAllResidents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name
    ) {
        List<Resident> residents;
        if (name != null && !name.trim().isEmpty()) {
            residents = residentRepository.findByFullNameContainingIgnoreCase(name.trim());
        } else if (status != null && !status.trim().isEmpty()) {
            residents = residentRepository.findByStatus(status.trim().toUpperCase());
        } else {
            residents = residentRepository.findAll();
        }

        List<ResidentResponse> responses = residents.stream()
                .map(residentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // 5. Lấy chi tiết thông tin cư dân theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ResidentResponse> getResidentById(@PathVariable Integer id) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cư dân ID " + id));
        return ResponseEntity.ok(residentMapper.toResponse(resident));
    }

    // 6. Cổng thông tin người thân: Tra cứu danh sách cư dân thuộc quyền bảo lãnh
    @GetMapping("/relative/{relativeId}")
    public ResponseEntity<List<ResidentResponse>> getResidentsByRelative(@PathVariable Integer relativeId) {
        List<ResidentResponse> responses = residentRepository.findByRelativeId(relativeId)
                .stream()
                .map(residentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
