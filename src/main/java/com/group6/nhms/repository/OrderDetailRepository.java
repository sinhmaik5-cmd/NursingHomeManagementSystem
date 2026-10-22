package com.group6.nhms.repository;

import com.group6.nhms.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    // Lấy danh sách các dòng chi tiết của 1 hóa đơn
    List<OrderDetail> findByOrderId(Integer orderId);

    // Tính tổng tiền các dịch vụ trong 1 hóa đơn
    @Query("SELECT COALESCE(SUM(od.amount), 0) FROM OrderDetail od WHERE od.order.id = :orderId")
    BigDecimal sumAmountByOrderId(@Param("orderId") Integer orderId);

    // Tính tổng tiền các dịch vụ của cư dân trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(od.amount), 0) FROM OrderDetail od WHERE od.order.resident.id = :residentId AND od.order.issueDate BETWEEN :startDate AND :endDate")
    BigDecimal sumServicesByResidentIdAndIssueDateBetween(
            @Param("residentId") Integer residentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Xóa các dòng chi tiết theo orderId (khi hủy hóa đơn)
    void deleteByOrderId(Integer orderId);
}
