package com.group6.nhms.dto.response;

public class BedResponse {

    private Integer id;
    private String bedNumber;
    private String roomNumber;
    private Boolean isOccupied;

    public BedResponse() {
    }

    public BedResponse(Integer id, String bedNumber, String roomNumber, Boolean isOccupied) {
        this.id = id;
        this.bedNumber = bedNumber;
        this.roomNumber = roomNumber;
        this.isOccupied = isOccupied;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Boolean getIsOccupied() { return isOccupied; }
    public void setIsOccupied(Boolean isOccupied) { this.isOccupied = isOccupied; }
}