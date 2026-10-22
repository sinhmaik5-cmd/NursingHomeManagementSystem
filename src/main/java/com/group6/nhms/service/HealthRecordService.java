package com.group6.nhms.service;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;

import java.util.List;

public interface HealthRecordService {

    HealthRecordResponse createHealthRecord(
            HealthRecordCreateRequest request,
            Integer staffId
    );

    List<HealthRecordResponse> getRecordsByResident(Integer residentId);

    HealthRecordResponse getRecordById(Integer id);

    void cancelHealthRecord(Integer recordId);
}
