package com.group6.nhms.service;

import com.group6.nhms.dto.request.IncidentReportCreateRequest;
import com.group6.nhms.dto.response.IncidentReportResponse;

import java.util.List;
import java.util.Map;

public interface IncidentReportService {
    IncidentReportResponse createIncident(
            IncidentReportCreateRequest request,
            Integer reporterStaffId
    );

    List<IncidentReportResponse> getPendingIncidents();

    List<IncidentReportResponse> getResidentIncidents(
            Integer relativeId,
            Integer residentId
    );

    IncidentReportResponse resolveIncident(Integer incidentId, String actionTaken);

    List<IncidentReportResponse> getAllIncidents(String status);

    long countPendingIncidents();

    Map<String, Long> getMonthlyIncidentStats();
}
