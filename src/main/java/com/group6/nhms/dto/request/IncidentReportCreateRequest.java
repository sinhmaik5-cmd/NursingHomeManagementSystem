package com.group6.nhms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class IncidentReportCreateRequest {

    @NotNull(message = "ID Cư dân không được để trống")
    private Integer residentId;

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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }
}