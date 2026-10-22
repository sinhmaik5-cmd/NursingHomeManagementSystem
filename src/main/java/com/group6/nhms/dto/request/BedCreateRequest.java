package com.group6.nhms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BedCreateRequest {
    @NotBlank(message = "Mã số giường không được để trống")
    private String bedNumber;
    
    @NotNull(message = "ID Phòng không được để trống")
    private Integer roomId;

    public BedCreateRequest() {
    }

    public BedCreateRequest(String bedNumber, Integer roomId) {
        this.bedNumber = bedNumber;
        this.roomId = roomId;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }
}