package com.group6.nhms.service;


import com.group6.nhms.dto.response.OrderResponse;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public interface OrderService {


    List<OrderResponse> getOrdersByResident(Integer residentId);


    Map<Integer, List<OrderResponse>> getOrdersByResidents(List<Integer> residentIds);


    List<OrderResponse> getUnpaidOrders(Integer residentId);


    OrderResponse getOrderById(Integer id);


    OrderResponse confirmPayment(Integer id);


    OrderResponse createCheckInFee(Integer residentId);


    OrderResponse createMonthlySettlement(Integer residentId, int year, int month);


    OrderResponse addServiceToResident(Integer residentId, Integer serviceId);


    List<OrderResponse> getAllOrders(String paymentStatus);


    long countUnpaidOrders();


    long countPaidOrders();


    BigDecimal getTotalCollectedRevenue();


    BigDecimal getRevenueBetweenDates(LocalDate startDate, LocalDate endDate);
}

