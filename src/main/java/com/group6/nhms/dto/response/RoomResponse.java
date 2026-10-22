package com.group6.nhms.dto.response;

import java.math.BigDecimal;

public class RoomResponse {
    private Integer id;
    private String roomNumber;
    private String roomType;
    private Integer capacity; // Thuộc tính bị thiếu gây ra lỗi đỏ
    private BigDecimal basePrice;
    private String status;

    public RoomResponse() {
    }

    public RoomResponse(Integer id, String roomNumber, String roomType, Integer capacity, BigDecimal basePrice, String status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.status = status;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}