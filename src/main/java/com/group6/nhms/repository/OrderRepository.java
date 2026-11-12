package com.group6.nhms.repository;

import com.group6.nhms.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Lấy các hóa đơn theo cư dân
    List<Order> findByResidentId(Integer residentId);

    // Lấy lịch sử hóa đơn của 1 cư dân sắp xếp theo ngày phát hành mới nhất
    List<Order> findByResidentIdOrderByIssueDateDesc(Integer residentId);

    List<Order> findByResidentIdInOrderByIssueDateDesc(List<Integer> residentIds);

    // Lấy các hóa đơn theo cư dân và trạng thái thanh toán (PENDING, PAID, CANCELLED)
    List<Order> findByResidentIdAndPaymentStatus(Integer residentId, String paymentStatus);

    // Lấy tất cả hóa đơn theo trạng thái thanh toán
    List<Order> findByPaymentStatus(String paymentStatus);

    // Đếm số lượng hóa đơn theo trạng thái thanh toán
    long countByPaymentStatus(String paymentStatus);

    // Tìm hóa đơn phát hành trong khoảng thời gian
    List<Order> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);

    // Lấy hóa đơn của 1 cư dân trong khoảng thời gian (phục vụ kiểm tra & gộp chốt hóa đơn tháng)
    List<Order> findByResidentIdAndIssueDateBetween(Integer residentId, LocalDate startDate, LocalDate endDate);

    // Kiểm tra xem cư dân đã được tạo hóa đơn trong khoảng thời gian này chưa (tránh tạo trùng hóa đơn tháng)
    boolean existsByResidentIdAndIssueDateBetween(Integer residentId, LocalDate startDate, LocalDate endDate);

    // Thống kê tổng doanh thu từ các hóa đơn đã thanh toán (PAID) trong khoảng thời gian (Dashboard Giám đốc)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAID' AND o.issueDate BETWEEN :startDate AND :endDate")
    BigDecimal sumRevenueBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
