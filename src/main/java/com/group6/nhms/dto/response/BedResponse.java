package com.group6.nhms.dto.response;

public class BedResponse {
    private Integer id;
    private String bedNumber;
    private String status;
    private Integer roomId;
    private String roomNumber;

    public BedResponse() {
    }

    public BedResponse(Integer id, String bedNumber, String status, Integer roomId, String roomNumber) {
        this.id = id;
        this.bedNumber = bedNumber;
        this.status = status;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}