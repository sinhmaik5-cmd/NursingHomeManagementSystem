package com.group6.nhms.dto.response;

import java.time.LocalDateTime;

public class IncidentReportResponse {

    private Integer id;
    private Integer residentId;
    private String residentName;
    private String roomNumber;
    private String reportedByStaffName;
    private String title;
    private String description;
    private String severityLevel;
    private String status; // PENDING, PROCESSING, RESOLVED
    private String actionTaken;
    private LocalDateTime createdDate;

    public IncidentReportResponse() {
    }

    public IncidentReportResponse(Integer id, Integer residentId, String residentName, String roomNumber, String reportedByStaffName, String title, String description, String severityLevel, String status, String actionTaken, LocalDateTime createdDate) {
        this.id = id;
        this.residentId = residentId;
        this.residentName = residentName;
        this.roomNumber = roomNumber;
        this.reportedByStaffName = reportedByStaffName;
        this.title = title;
        this.description = description;
        this.severityLevel = severityLevel;
        this.status = status;
        this.actionTaken = actionTaken;
        this.createdDate = createdDate;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getResidentId() { return residentId; }
    public void setResidentId(Integer residentId) { this.residentId = residentId; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getReportedByStaffName() { return reportedByStaffName; }
    public void setReportedByStaffName(String reportedByStaffName) { this.reportedByStaffName = reportedByStaffName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
