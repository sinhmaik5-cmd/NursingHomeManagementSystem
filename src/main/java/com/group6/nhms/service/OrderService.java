package com.group6.nhms.service;


import com.group6.nhms.dto.response.MonthlyRevenueSummary;
import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.entity.Service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface OrderService {

    static Set<String> findSettledResidentPeriods(List<OrderResponse> orders) {
        Set<String> settledPeriods = new HashSet<>();
        if (orders == null) {
            return settledPeriods;
        }
        for (OrderResponse order : orders) {
            if (order == null || order.getResidentId() == null || order.getIssueDate() == null) {
                continue;
            }
            boolean hasRoomFee = order.getRoomPrice() != null && order.getRoomPrice().compareTo(BigDecimal.ZERO) > 0;
            if (hasRoomFee) {
                settledPeriods.add(residentPeriodKey(order));
            }
        }
        return settledPeriods;
    }

    static boolean isPreSettlementDraft(OrderResponse order, Set<String> settledResidentPeriods) {
        if (order == null) {
            return false;
        }
        String status = order.getPaymentStatus();
        boolean unpaid = "PENDING".equalsIgnoreCase(status) || "UNPAID".equalsIgnoreCase(status);
        if (!unpaid) {
            return false;
        }
        boolean noRoomFee = order.getRoomPrice() == null || order.getRoomPrice().compareTo(BigDecimal.ZERO) <= 0;
        if (!noRoomFee) {
            return false;
        }
        String key = residentPeriodKey(order);
        return key == null || settledResidentPeriods == null || !settledResidentPeriods.contains(key);
    }

    private static String residentPeriodKey(OrderResponse order) {
        if (order.getResidentId() == null || order.getIssueDate() == null) {
            return null;
        }
        return order.getResidentId() + "_" + order.getIssueDate().getYear() + "_" + order.getIssueDate().getMonthValue();
    }


    List<OrderResponse> getOrdersByResident(Integer residentId);


    Map<Integer, List<OrderResponse>> getOrdersByResidents(List<Integer> residentIds);


    List<OrderResponse> getUnpaidOrders(Integer residentId);


    OrderResponse getOrderById(Integer id);


    OrderResponse confirmPayment(Integer id);


    void cancelUnpaidOrder(Integer id);


    List<MonthlyRevenueSummary> getMonthlyRevenueSummaries(List<OrderResponse> orders);


    OrderResponse createCheckInFee(Integer residentId);


    OrderResponse createMonthlySettlement(Integer residentId, int year, int month);


    OrderResponse addServiceToResident(Integer residentId, Integer serviceId);


    OrderResponse addServiceToResident(Integer residentId, Integer serviceId, int quantity);


    List<Service> getAllCareServices();


    Service createCareService(String serviceName, BigDecimal unitPrice, String description);


    void removeServiceDetail(Integer orderDetailId);


    List<OrderResponse> getAllOrders(String paymentStatus);


    long countUnpaidOrders();


    long countPaidOrders();


    BigDecimal getTotalCollectedRevenue();


    BigDecimal getRevenueBetweenDates(LocalDate startDate, LocalDate endDate);
}

