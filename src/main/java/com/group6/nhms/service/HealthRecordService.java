package com.group6.nhms.service;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;

public interface HealthRecordService {
    HealthRecordResponse createHealthRecord(
            HealthRecordCreateRequest request,
            Integer staffId
    );
}