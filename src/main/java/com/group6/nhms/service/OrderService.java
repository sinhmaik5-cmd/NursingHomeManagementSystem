package com.group6.nhms.service;

import com.group6.nhms.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    List<OrderResponse> getOrdersByResident(Integer residentId);

    List<OrderResponse> getUnpaidOrders(Integer residentId);

    OrderResponse getOrderById(Integer id);

    OrderResponse confirmPayment(Integer id);

    OrderResponse createCheckInFee(Integer residentId);

    OrderResponse createMonthlySettlement(Integer residentId, int year, int month);

    OrderResponse addServiceToResident(Integer residentId, Integer serviceId);
}
