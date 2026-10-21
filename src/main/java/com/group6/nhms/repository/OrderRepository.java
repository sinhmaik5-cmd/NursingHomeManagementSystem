package com.group6.nhms.repository;

import com.group6.nhms.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    // Lấy các hóa đơn chưa thanh toán của 1 cư dân
    List<Order> findByResidentIdAndPaymentStatus(Integer residentId, String paymentStatus);
}