package com.group6.nhms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Integer id;
    private Integer residentId;
    private String residentName;
    private String roomNumber;

    private BigDecimal roomBasePrice;
    private BigDecimal totalServicesAmount;
    private BigDecimal totalAmount;

    private String paymentStatus;
    private LocalDateTime createdDate;
    private LocalDateTime paymentDate;

    private List<OrderDetailResponse> orderDetails;

    public OrderResponse() {
    }

    public OrderResponse(Integer id, Integer residentId, String residentName, String roomNumber, BigDecimal roomBasePrice, BigDecimal totalServicesAmount, BigDecimal totalAmount, String paymentStatus, LocalDateTime createdDate, LocalDateTime paymentDate, List<OrderDetailResponse> orderDetails) {
        this.id = id;
        this.residentId = residentId;
        this.residentName = residentName;
        this.roomNumber = roomNumber;
        this.roomBasePrice = roomBasePrice;
        this.totalServicesAmount = totalServicesAmount;
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
    }

    public BigDecimal getTotalServicesAmount() {
        return totalServicesAmount;
    }

    public void setTotalServicesAmount(BigDecimal totalServicesAmount) {
        this.totalServicesAmount = totalServicesAmount;
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

    public static class OrderDetailResponse {
        private String serviceName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal amount;

        public OrderDetailResponse() {
        }

        public OrderDetailResponse(String serviceName, BigDecimal unitPrice, Integer quantity, BigDecimal amount) {
            this.serviceName = serviceName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.amount = amount;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}