package com.group6.nhms.dto.request;


import jakarta.validation.constraints.NotBlank;


public class IncidentReportCreateRequest {


    private Integer residentId;


    private Integer roomId;


    private Integer bedId;


    private String location;


    private Boolean setMaintenance = true;


    @NotBlank(message = "Tên/Loại sự cố không được để trống")
    private String title;


    @NotBlank(message = "Mô tả chi tiết sự cố không được để trống")
    private String description;


    private String severityLevel; // LOW, MEDIUM, HIGH, CRITICAL


    public IncidentReportCreateRequest() {
    }


    public IncidentReportCreateRequest(Integer residentId, String title, String description, String severityLevel) {
        this.residentId = residentId;
        this.title = title;
        this.description = description;
        this.severityLevel = severityLevel;
    }


    public Integer getResidentId() { return residentId; }
    public void setResidentId(Integer residentId) { this.residentId = residentId; }


    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }


    public Integer getBedId() { return bedId; }
    public void setBedId(Integer bedId) { this.bedId = bedId; }


    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }


    public Boolean getSetMaintenance() { return setMaintenance; }
    public void setSetMaintenance(Boolean setMaintenance) { this.setMaintenance = setMaintenance; }


    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }


    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }
}

