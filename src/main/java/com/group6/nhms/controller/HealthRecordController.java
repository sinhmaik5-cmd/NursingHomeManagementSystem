package com.group6.nhms.controller;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.MedicationResponse;
import com.group6.nhms.mapper.MedicationMapper;
import com.group6.nhms.repository.MedicationRepository;
import com.group6.nhms.service.HealthRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;
    private final MedicationRepository medicationRepository;
    private final MedicationMapper medicationMapper;

    public HealthRecordController(
            HealthRecordService healthRecordService,
            MedicationRepository medicationRepository,
            MedicationMapper medicationMapper
    ) {
        this.healthRecordService = healthRecordService;
        this.medicationRepository = medicationRepository;
        this.medicationMapper = medicationMapper;
    }

    // 1. Bác sĩ/Điều dưỡng khám bệnh & Kê đơn thuốc tự động trừ kho
    @PostMapping
    public ResponseEntity<HealthRecordResponse> createHealthRecord(
            @Valid @RequestBody HealthRecordCreateRequest request,
            @RequestParam Integer staffId
    ) {
        HealthRecordResponse response = healthRecordService.createHealthRecord(request, staffId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Tra cứu lịch sử khám bệnh và biến động sinh hiệu của cư dân (cho Bác sĩ & Cổng thông tin người thân)
    @GetMapping("/resident/{residentId}")
    public ResponseEntity<List<HealthRecordResponse>> getHealthRecordsByResident(@PathVariable Integer residentId) {
        return ResponseEntity.ok(healthRecordService.getRecordsByResident(residentId));
    }

    // 3. Chi tiết hồ sơ khám bệnh theo ID
    @GetMapping("/{id}")
    public ResponseEntity<HealthRecordResponse> getHealthRecordById(@PathVariable Integer id) {
        return ResponseEntity.ok(healthRecordService.getRecordById(id));
    }

    // 4. Nghiệp vụ: Hủy đơn thuốc / phiếu khám và tự động hoàn trả tồn kho thuốc (Rollback stock)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelHealthRecord(@PathVariable Integer id) {
        healthRecordService.cancelHealthRecord(id);
        Map<String, String> result = new HashMap<>();
        result.put("message", "Đã hủy đơn thuốc và hoàn trả tồn kho thành công");
        return ResponseEntity.ok(result);
    }

    // 5. Danh sách thuốc còn hạn và còn tồn kho để Bác sĩ chọn kê đơn
    @GetMapping("/medications/available")
    public ResponseEntity<List<MedicationResponse>> getAvailableMedications() {
        List<MedicationResponse> responses = medicationRepository.findAvailableForPrescription(LocalDate.now())
                .stream()
                .map(medicationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // 6. Cảnh báo dược phẩm: Danh sách thuốc sắp hết hạn (trong vòng 30 ngày)
    @GetMapping("/medications/expiring-soon")
    public ResponseEntity<List<MedicationResponse>> getExpiringMedications() {
        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);
        List<MedicationResponse> responses = medicationRepository.findByExpiryDateBetween(today, in30Days)
                .stream()
                .map(medicationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // 7. Cảnh báo dược phẩm: Danh sách thuốc dưới ngưỡng an toàn (< 10)
    @GetMapping("/medications/low-stock")
    public ResponseEntity<List<MedicationResponse>> getLowStockMedications() {
        List<MedicationResponse> responses = medicationRepository.findByQuantityInStockLessThan(10)
                .stream()
                .map(medicationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
