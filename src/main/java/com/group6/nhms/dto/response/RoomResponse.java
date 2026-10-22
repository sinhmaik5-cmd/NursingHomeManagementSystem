package com.group6.nhms.dto.response;

import java.math.BigDecimal;

public class RoomResponse {

    private Integer id;
    private String roomNumber;
    private String roomType;
    private BigDecimal basePrice;
    private Integer totalBeds;
    private Integer availableBeds;

    public RoomResponse() {
    }

    public RoomResponse(Integer id, String roomNumber, String roomType, BigDecimal basePrice, Integer totalBeds, Integer availableBeds) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.basePrice = basePrice;
        this.totalBeds = totalBeds;
        this.availableBeds = availableBeds;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public Integer getTotalBeds() { return totalBeds; }
    public void setTotalBeds(Integer totalBeds) { this.totalBeds = totalBeds; }

    public Integer getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(Integer availableBeds) { this.availableBeds = availableBeds; }
}