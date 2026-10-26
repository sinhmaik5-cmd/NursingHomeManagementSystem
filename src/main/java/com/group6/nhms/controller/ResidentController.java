package com.group6.nhms.controller;

import com.group6.nhms.dto.request.BedTransferRequest;
import com.group6.nhms.dto.request.ResidentCheckInRequest;
import com.group6.nhms.dto.response.ResidentResponse;
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

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
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
        return ResponseEntity.ok(residentService.getAllResidents(name, status));
    }

    // 5. Lấy chi tiết thông tin cư dân theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ResidentResponse> getResidentById(@PathVariable Integer id) {
        return ResponseEntity.ok(residentService.getResidentById(id));
    }

    // 6. Cổng thông tin người thân: Tra cứu danh sách cư dân thuộc quyền bảo lãnh
    @GetMapping("/relative/{relativeId}")
    public ResponseEntity<List<ResidentResponse>> getResidentsByRelative(@PathVariable Integer relativeId) {
        return ResponseEntity.ok(residentService.getResidentsByRelative(relativeId));
    }
}
