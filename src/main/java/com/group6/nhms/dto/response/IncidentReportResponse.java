package com.group6.nhms.dto.response;


import java.time.LocalDateTime;


public class IncidentReportResponse {


    private Integer id;
    private Integer residentId;
    private String residentName;
    private Integer roomId;
    private String roomNumber;
    private Integer bedId;
    private String bedNumber;
    private String location;
    private String reportedByStaffName;
    private Integer reporterStaffId;
    private String reporterStaffRole;
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


    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }


    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }


    public Integer getBedId() { return bedId; }
    public void setBedId(Integer bedId) { this.bedId = bedId; }


    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }


    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }


    public String getReportedByStaffName() { return reportedByStaffName; }
    public void setReportedByStaffName(String reportedByStaffName) { this.reportedByStaffName = reportedByStaffName; }


    public Integer getReporterStaffId() { return reporterStaffId; }
    public void setReporterStaffId(Integer reporterStaffId) { this.reporterStaffId = reporterStaffId; }
    public Integer getReportedByStaffId() { return reporterStaffId; }
    public void setReportedByStaffId(Integer reportedByStaffId) { this.reporterStaffId = reportedByStaffId; }


    public String getReporterStaffRole() { return reporterStaffRole; }
    public void setReporterStaffRole(String reporterStaffRole) { this.reporterStaffRole = reporterStaffRole; }
    public String getReportedByStaffRole() { return reporterStaffRole; }
    public void setReportedByStaffRole(String reportedByStaffRole) { this.reporterStaffRole = reportedByStaffRole; }


    public boolean isReportedByNurse() { return "NURSE".equalsIgnoreCase(reporterStaffRole); }
    public boolean isReportedByDoctor() { return "DOCTOR".equalsIgnoreCase(reporterStaffRole); }
    public boolean isReportedByReceptionist() { return "RECEPTIONIST".equalsIgnoreCase(reporterStaffRole); }


    public String getDisplayTarget() {
        if (residentName != null && !residentName.isBlank()) {
            StringBuilder sb = new StringBuilder(residentName);
            if (roomNumber != null) {
                sb.append(" (Phòng ").append(roomNumber);
                if (bedNumber != null) {
                    sb.append(" - ").append(bedNumber);
                }
                sb.append(")");
            }
            return sb.toString();
        }
        if (location != null && !location.isBlank()) {
            return location + (roomNumber != null ? " - Phòng " + roomNumber : "");
        }
        if (roomNumber != null) {
            return "Phòng " + roomNumber + (bedNumber != null ? " - " + bedNumber : "");
        }
        return "Khu vực viện";
    }


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

