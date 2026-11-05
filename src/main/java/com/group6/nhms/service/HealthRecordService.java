package com.group6.nhms.service;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.MedicationResponse;

import java.util.List;
import java.util.Map;

public interface HealthRecordService {

    HealthRecordResponse createHealthRecord(
            HealthRecordCreateRequest request,
            Integer staffId
    );

    List<HealthRecordResponse> getRecordsByResident(Integer residentId);

    Map<Integer, HealthRecordResponse> getLatestRecordsByResidents(List<Integer> residentIds);

    HealthRecordResponse getRecordById(Integer id);

    void cancelHealthRecord(Integer recordId);

    List<MedicationResponse> getAvailableMedications();

    List<MedicationResponse> getLowStockMedications();
}
