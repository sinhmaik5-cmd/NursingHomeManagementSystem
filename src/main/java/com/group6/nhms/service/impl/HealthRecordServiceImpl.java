package com.group6.nhms.service.impl;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.entity.HealthRecord;
import com.group6.nhms.entity.HealthRecordDetail;
import com.group6.nhms.entity.Medication;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.HealthRecordMapper;
import com.group6.nhms.repository.HealthRecordDetailRepository;
import com.group6.nhms.repository.HealthRecordRepository;
import com.group6.nhms.repository.MedicationRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.HealthRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final HealthRecordDetailRepository healthRecordDetailRepository;
    private final MedicationRepository medicationRepository;
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;
    private final HealthRecordMapper healthRecordMapper;

    public HealthRecordServiceImpl(
            HealthRecordRepository healthRecordRepository,
            HealthRecordDetailRepository healthRecordDetailRepository,
            MedicationRepository medicationRepository,
            ResidentRepository residentRepository,
            StaffRepository staffRepository,
            HealthRecordMapper healthRecordMapper
    ) {
        this.healthRecordRepository = healthRecordRepository;
        this.healthRecordDetailRepository = healthRecordDetailRepository;
        this.medicationRepository = medicationRepository;
        this.residentRepository = residentRepository;
        this.staffRepository = staffRepository;
        this.healthRecordMapper = healthRecordMapper;
    }

    @Override
    @Transactional
    public HealthRecordResponse createHealthRecord(
            HealthRecordCreateRequest request,
            Integer staffId
    ) {
        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy cư dân ID " + request.getResidentId()));

        if (!ResidentStatus.ACTIVE.name().equals(resident.getStatus())) {
            throw new BadRequestException(
                    "Chỉ có thể tạo hồ sơ khám cho cư dân đang lưu trú");
        }

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy nhân viên ID " + staffId));

        HealthRecord healthRecord = new HealthRecord();
        healthRecord.setResident(resident);
        healthRecord.setStaff(staff);
        healthRecord.setRecordDate(Instant.now());
        healthRecord.setBloodPressure(request.getBloodPressure());
        healthRecord.setHeartRate(request.getHeartRate());
        healthRecord.setTemperature(request.getTemperature());
        healthRecord.setBloodSugar(request.getBloodSugar());
        healthRecord.setNotes(request.getNotes());

        HealthRecord savedRecord = healthRecordRepository.save(healthRecord);

        List<HealthRecordDetail> savedDetails = new ArrayList<>();

        if (request.getMedications() != null) {
            for (HealthRecordCreateRequest.MedicationItemRequest item : request.getMedications()) {
                if (item.getQuantityRequested() == null || item.getQuantityRequested() <= 0) {
                    throw new BadRequestException("Số lượng thuốc phải lớn hơn 0");
                }

                Medication medication = medicationRepository
                        .findByIdForUpdate(item.getMedicationId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy thuốc ID " + item.getMedicationId()));

                Integer stock = medication.getQuantityInStock();

                if (stock == null || stock < item.getQuantityRequested()) {
                    throw new BadRequestException("Thuốc trong kho không đủ");
                }

                medication.setQuantityInStock(stock - item.getQuantityRequested());

                HealthRecordDetail detail = new HealthRecordDetail();
                detail.setHealthRecord(savedRecord);
                detail.setMedication(medication);
                detail.setQuantity(item.getQuantityRequested());
                detail.setDosage(item.getDosage());

                savedDetails.add(detail);
            }
        }

        List<HealthRecordDetail> persistedDetails =
                healthRecordDetailRepository.saveAll(savedDetails);

        HealthRecordResponse response = healthRecordMapper.toResponse(savedRecord);
        List<HealthRecordResponse.MedicationItemResponse> medicationResponses =
                new ArrayList<>();

        for (HealthRecordDetail detail : persistedDetails) {
            medicationResponses.add(
                    new HealthRecordResponse.MedicationItemResponse(
                            detail.getMedication().getId(),
                            detail.getMedication().getMedName(),
                            detail.getQuantity(),
                            detail.getDosage()
                    )
            );
        }

        response.setMedications(medicationResponses);
        return response;
    }
}