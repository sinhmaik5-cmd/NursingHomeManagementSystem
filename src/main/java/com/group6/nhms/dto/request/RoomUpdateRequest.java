package com.group6.nhms.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.math.BigDecimal;


public class RoomUpdateRequest {


    @NotBlank(message = "Số phòng không được để trống")
    private String roomNumber;


    @NotBlank(message = "Loại phòng không được để trống")
    private String roomType;


    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa tối thiểu là 1 giường")
    private Integer capacity;


    @NotNull(message = "Đơn giá cơ bản không được để trống")
    private BigDecimal basePrice;


    public RoomUpdateRequest() {
    }


    public RoomUpdateRequest(String roomNumber, String roomType, Integer capacity, BigDecimal basePrice) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.basePrice = basePrice;
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
}

