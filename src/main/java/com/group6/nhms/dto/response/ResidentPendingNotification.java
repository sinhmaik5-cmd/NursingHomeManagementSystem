package com.group6.nhms.dto.response;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class ResidentPendingNotification {
    private Integer residentId;
    private String residentName;
    private String roomNumber;
    private String bedNumber;
    private List<OrderResponse> unpaidOrders = new ArrayList<>();
    private BigDecimal totalUnpaidAmount = BigDecimal.ZERO;
    private List<IncidentReportResponse> pendingIncidents = new ArrayList<>();


    public ResidentPendingNotification() {
    }


    public ResidentPendingNotification(Integer residentId, String residentName, String roomNumber, String bedNumber) {
        this.residentId = residentId;
        this.residentName = residentName;
        this.roomNumber = roomNumber;
        this.bedNumber = bedNumber;
    }


    public Integer getResidentId() { return residentId; }
    public void setResidentId(Integer residentId) { this.residentId = residentId; }


    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }


    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }


    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }


    public List<OrderResponse> getUnpaidOrders() { return unpaidOrders; }
    public void setUnpaidOrders(List<OrderResponse> unpaidOrders) {
        this.unpaidOrders = unpaidOrders != null ? unpaidOrders : new ArrayList<>();
        calculateTotalUnpaid();
    }


    public BigDecimal getTotalUnpaidAmount() { return totalUnpaidAmount; }
    public void setTotalUnpaidAmount(BigDecimal totalUnpaidAmount) { this.totalUnpaidAmount = totalUnpaidAmount; }


    public List<IncidentReportResponse> getPendingIncidents() { return pendingIncidents; }
    public void setPendingIncidents(List<IncidentReportResponse> pendingIncidents) {
        this.pendingIncidents = pendingIncidents != null ? pendingIncidents : new ArrayList<>();
    }


    public boolean hasPendingItems() {
        return (!unpaidOrders.isEmpty()) || (!pendingIncidents.isEmpty());
    }


    private void calculateTotalUnpaid() {
        if (unpaidOrders == null || unpaidOrders.isEmpty()) {
            this.totalUnpaidAmount = BigDecimal.ZERO;
            return;
        }
        this.totalUnpaidAmount = unpaidOrders.stream()
                .map(OrderResponse::getTotalAmount)
                .filter(amt -> amt != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

