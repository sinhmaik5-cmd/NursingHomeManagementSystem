package com.group6.nhms.controller;

import com.group6.nhms.dto.request.BedCreateRequest;
import com.group6.nhms.dto.request.BedStatusUpdateRequest;
import com.group6.nhms.dto.response.BedResponse;
import com.group6.nhms.enums.BedStatus;
import com.group6.nhms.repository.BedRepository;
import com.group6.nhms.service.BedService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/beds")
public class BedController {

    private final BedService bedService;
    private final BedRepository bedRepository;

    public BedController(BedService bedService, BedRepository bedRepository) {
        this.bedService = bedService;
        this.bedRepository = bedRepository;
    }

    // 1. Lấy danh sách tất cả các giường trống (Status = 'AVAILABLE')
    @GetMapping("/available")
    public ResponseEntity<List<BedResponse>> getAvailableBeds() {
        return ResponseEntity.ok(bedService.getAvailableBeds());
    }

    // 2. Lấy danh sách giường theo phòng
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<BedResponse>> getBedsByRoom(@PathVariable Integer roomId) {
        return ResponseEntity.ok(bedService.getBedsByRoom(roomId));
    }

    // 3. Lấy chi tiết thông tin một giường
    @GetMapping("/{id}")
    public ResponseEntity<BedResponse> getBedById(@PathVariable Integer id) {
        return ResponseEntity.ok(bedService.getBedById(id));
    }

    // 4. Nghiệp vụ: Staff tự chuyển trạng thái giường (Sau khi khử trùng: MAINTENANCE -> AVAILABLE)
    @PutMapping("/{id}/status")
    public ResponseEntity<BedResponse> updateBedStatus(
            @PathVariable Integer id,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) BedStatusUpdateRequest request
    ) {
        BedStatusUpdateRequest actualRequest = request != null ? request : new BedStatusUpdateRequest(status);
        return ResponseEntity.ok(bedService.updateBedStatus(id, actualRequest));
    }

    // 5. Thêm giường mới vào phòng (Có kiểm tra ràng buộc không vượt quá Room.capacity)
    @PostMapping
    public ResponseEntity<BedResponse> createBed(@Valid @RequestBody BedCreateRequest request) {
        return new ResponseEntity<>(bedService.createBed(request), HttpStatus.CREATED);
    }

    // 6. Dashboard Thống kê Tỷ lệ Lấp đầy (Occupancy Rate)
    // Công thức: Tỷ lệ lấp đầy (%) = (Số giường OCCUPIED / Tổng số giường) * 100
    @GetMapping("/occupancy-rate")
    public ResponseEntity<Map<String, Object>> getOccupancyRate() {
        long totalBeds = bedRepository.count();
        long occupiedBeds = bedRepository.countByStatus(BedStatus.OCCUPIED.name());
        long availableBeds = bedRepository.countByStatus(BedStatus.AVAILABLE.name());
        long maintenanceBeds = bedRepository.countByStatus(BedStatus.MAINTENANCE.name());

        double rate = totalBeds > 0 ? ((double) occupiedBeds / totalBeds) * 100.0 : 0.0;
        double roundedRate = Math.round(rate * 100.0) / 100.0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalBeds", totalBeds);
        result.put("occupiedBeds", occupiedBeds);
        result.put("availableBeds", availableBeds);
        result.put("maintenanceBeds", maintenanceBeds);
        result.put("occupancyRatePercentage", roundedRate);

        return ResponseEntity.ok(result);
    }
}
