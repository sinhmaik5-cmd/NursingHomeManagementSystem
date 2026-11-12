package com.group6.nhms.dto.response;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public class OrderResponse {


    private Integer id;
    private Integer residentId;
    private String residentName;
    private String roomNumber;


    private BigDecimal roomBasePrice;
    private BigDecimal roomPrice;
    private BigDecimal totalServicesAmount;
    private BigDecimal servicesPrice;
    private BigDecimal medicationsPrice;
    private BigDecimal totalAmount;


    private String paymentStatus;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDateTime createdDate;
    private LocalDateTime paymentDate;


    private List<OrderDetailResponse> orderDetails;
    private String roomBillingPeriod;
    private String roomQuantityLabel;


    public OrderResponse() {
    }


    public OrderResponse(Integer id, Integer residentId, String residentName, String roomNumber, BigDecimal roomBasePrice, BigDecimal totalServicesAmount, BigDecimal totalAmount, String paymentStatus, LocalDateTime createdDate, LocalDateTime paymentDate, List<OrderDetailResponse> orderDetails) {
        this.id = id;
        this.residentId = residentId;
        this.residentName = residentName;
        this.roomNumber = roomNumber;
        this.roomBasePrice = roomBasePrice;
        this.roomPrice = roomBasePrice;
        this.totalServicesAmount = totalServicesAmount;
        this.servicesPrice = totalServicesAmount;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.createdDate = createdDate;
        this.paymentDate = paymentDate;
        this.orderDetails = orderDetails;
    }


    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public Integer getResidentId() {
        return residentId;
    }


    public void setResidentId(Integer residentId) {
        this.residentId = residentId;
    }


    public String getResidentName() {
        return residentName;
    }


    public void setResidentName(String residentName) {
        this.residentName = residentName;
    }


    public String getRoomNumber() {
        return roomNumber;
    }


    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }


    public BigDecimal getRoomBasePrice() {
        return roomBasePrice;
    }


    public void setRoomBasePrice(BigDecimal roomBasePrice) {
        this.roomBasePrice = roomBasePrice;
        this.roomPrice = roomBasePrice;
    }


    public BigDecimal getRoomPrice() {
        return roomPrice != null ? roomPrice : roomBasePrice;
    }


    public void setRoomPrice(BigDecimal roomPrice) {
        this.roomPrice = roomPrice;
        this.roomBasePrice = roomPrice;
    }


    public BigDecimal getTotalServicesAmount() {
        return totalServicesAmount;
    }


    public void setTotalServicesAmount(BigDecimal totalServicesAmount) {
        this.totalServicesAmount = totalServicesAmount;
        this.servicesPrice = totalServicesAmount;
    }


    public BigDecimal getServicesPrice() {
        return servicesPrice != null ? servicesPrice : totalServicesAmount;
    }


    public void setServicesPrice(BigDecimal servicesPrice) {
        this.servicesPrice = servicesPrice;
        this.totalServicesAmount = servicesPrice;
    }


    public BigDecimal getMedicationsPrice() {
        return medicationsPrice;
    }


    public void setMedicationsPrice(BigDecimal medicationsPrice) {
        this.medicationsPrice = medicationsPrice;
    }


    public BigDecimal getTotalAmount() {
        return totalAmount;
    }


    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }


    public String getPaymentStatus() {
        return paymentStatus;
    }


    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }


    public LocalDate getIssueDate() {
        return issueDate;
    }


    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }


    public LocalDate getDueDate() {
        return dueDate;
    }


    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }


    public LocalDateTime getCreatedDate() {
        return createdDate;
    }


    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }


    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }


    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }


    public List<OrderDetailResponse> getOrderDetails() {
        return orderDetails;
    }


    public void setOrderDetails(List<OrderDetailResponse> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public String getRoomBillingPeriod() {
        return roomBillingPeriod;
    }

    public void setRoomBillingPeriod(String roomBillingPeriod) {
        this.roomBillingPeriod = roomBillingPeriod;
    }

    public String getRoomQuantityLabel() {
        return roomQuantityLabel;
    }

    public void setRoomQuantityLabel(String roomQuantityLabel) {
        this.roomQuantityLabel = roomQuantityLabel;
    }
}

