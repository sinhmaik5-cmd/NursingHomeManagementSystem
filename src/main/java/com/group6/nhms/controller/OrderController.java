package com.group6.nhms.controller;

import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.repository.OrderRepository;
import com.group6.nhms.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    // 1. Lấy danh sách hóa đơn theo cư dân (cho Kế toán & Cổng thông tin người thân)
    @GetMapping("/resident/{residentId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByResident(@PathVariable Integer residentId) {
        return ResponseEntity.ok(orderService.getOrdersByResident(residentId));
    }

    // 2. Lấy danh sách hóa đơn còn nợ (Status = 'PENDING') của cư dân (phục vụ thủ tục Check-out)
    @GetMapping("/resident/{residentId}/unpaid")
    public ResponseEntity<List<OrderResponse>> getUnpaidOrders(@PathVariable Integer residentId) {
        return ResponseEntity.ok(orderService.getUnpaidOrders(residentId));
    }

    // 3. Chi tiết một hóa đơn theo ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // 4. Xác nhận thanh toán hóa đơn: PENDING -> PAID, lưu ngày giờ thanh toán
    @PutMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> confirmPayment(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.confirmPayment(id));
    }

    // 5. Khởi tạo hóa đơn tiền phòng tháng đầu tiên khi cư dân Check-in
    @PostMapping("/check-in-fee")
    public ResponseEntity<OrderResponse> createCheckInFee(@RequestParam Integer residentId) {
        return new ResponseEntity<>(orderService.createCheckInFee(residentId), HttpStatus.CREATED);
    }

    // 6. Chốt hóa đơn hàng tháng cho cư dân (Tiền phòng + Tiền dịch vụ + Tiền thuốc trong tháng)
    @PostMapping("/monthly-settlement")
    public ResponseEntity<OrderResponse> createMonthlySettlement(
            @RequestParam Integer residentId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return new ResponseEntity<>(orderService.createMonthlySettlement(residentId, year, month), HttpStatus.CREATED);
    }

    // 7. Ghi nhận dịch vụ phát sinh cho cư dân (lưu vào OrderDetail)
    @PostMapping("/services")
    public ResponseEntity<OrderResponse> addServiceToResident(
            @RequestParam Integer residentId,
            @RequestParam Integer serviceId
    ) {
        return new ResponseEntity<>(orderService.addServiceToResident(residentId, serviceId), HttpStatus.CREATED);
    }

    // 8. Báo cáo Doanh thu (Tổng tiền từ các hóa đơn có PaymentStatus = 'PAID' theo khoảng thời gian)
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        BigDecimal totalRevenue = orderRepository.sumRevenueBetweenDates(startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        return ResponseEntity.ok(result);
    }
}
