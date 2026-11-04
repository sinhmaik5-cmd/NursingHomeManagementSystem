package com.group6.nhms.service.impl;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.MedicationResponse;
import com.group6.nhms.entity.HealthRecord;
import com.group6.nhms.entity.HealthRecordDetail;
import com.group6.nhms.entity.Medication;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.HealthRecordMapper;
import com.group6.nhms.mapper.MedicationMapper;
import com.group6.nhms.repository.HealthRecordDetailRepository;
import com.group6.nhms.repository.HealthRecordRepository;
import com.group6.nhms.repository.MedicationRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.HealthRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
    private final MedicationMapper medicationMapper;

    public HealthRecordServiceImpl(
            HealthRecordRepository healthRecordRepository,
            HealthRecordDetailRepository healthRecordDetailRepository,
            MedicationRepository medicationRepository,
            ResidentRepository residentRepository,
            StaffRepository staffRepository,
            HealthRecordMapper healthRecordMapper,
            MedicationMapper medicationMapper
    ) {
        this.healthRecordRepository = healthRecordRepository;
        this.healthRecordDetailRepository = healthRecordDetailRepository;
        this.medicationRepository = medicationRepository;
        this.residentRepository = residentRepository;
        this.staffRepository = staffRepository;
        this.healthRecordMapper = healthRecordMapper;
        this.medicationMapper = medicationMapper;
    }

    @Override
    @Transactional
    public HealthRecordResponse createHealthRecord(
            HealthRecordCreateRequest request,
            Integer staffId
    ) {
        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cư dân ID " + request.getResidentId()));

        if (!ResidentStatus.ACTIVE.name().equals(resident.getStatus())) {
            throw new BadRequestException("Cư dân không ở trạng thái lưu trú");
        }

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên/bác sĩ ID " + staffId));

        if ("NURSE".equalsIgnoreCase(staff.getJobTitle())
                && request.getMedications() != null && !request.getMedications().isEmpty()) {
            throw new BadRequestException("Điều dưỡng không được kê đơn hoặc xuất kho thuốc");
        }

        HealthRecord record = new HealthRecord();
        record.setResident(resident);
        record.setStaff(staff);
        record.setRecordDate(Instant.now());
        record.setBloodPressure(request.getBloodPressure());
        record.setHeartRate(request.getHeartRate());
        record.setBloodSugar(request.getBloodSugar());
        record.setTemperature(request.getTemperature());
        String notes = request.getNotes();
        if (request.getDiagnosis() != null && !request.getDiagnosis().isBlank()) {
            notes = (notes == null || notes.isBlank()) ? request.getDiagnosis() : request.getDiagnosis() + " - " + notes;
        }
        record.setNotes(notes);

        HealthRecord savedRecord = healthRecordRepository.save(record);
        List<HealthRecordDetail> savedDetails = new ArrayList<>();

        if (request.getMedications() != null && !request.getMedications().isEmpty()) {
            for (HealthRecordCreateRequest.MedicationItemRequest item : request.getMedications()) {
                if (item.getMedicationId() == null) continue;

                Medication medication = medicationRepository.findByIdForUpdate(item.getMedicationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc ID " + item.getMedicationId()));

                if (medication.getExpiryDate() != null && medication.getExpiryDate().isBefore(LocalDate.now())) {
                    throw new BadRequestException("Thuốc '" + medication.getMedName() + "' đã hết hạn sử dụng");
                }

                int requestedQty = item.getQuantityRequested() != null ? item.getQuantityRequested() : 0;
                if (requestedQty <= 0) {
                    throw new BadRequestException("Số lượng thuốc phải lớn hơn 0");
                }

                if (medication.getQuantityInStock() < requestedQty) {
                    throw new BadRequestException("Thuốc '" + medication.getMedName() + "' không đủ số lượng trong kho (Còn: "
                            + medication.getQuantityInStock() + ", Yêu cầu: " + requestedQty + ")");
                }

                medication.setQuantityInStock(medication.getQuantityInStock() - requestedQty);
                medicationRepository.save(medication);

                HealthRecordDetail detail = new HealthRecordDetail();
                detail.setHealthRecord(savedRecord);
                detail.setMedication(medication);
                detail.setQuantity(requestedQty);
                detail.setDosage(item.getDosage());
                detail.setUnitPrice(medication.getUnitPrice());

                BigDecimal total = medication.getUnitPrice().multiply(BigDecimal.valueOf(requestedQty));
                detail.setTotalPrice(total);

                savedDetails.add(healthRecordDetailRepository.save(detail));
            }
        }

        return buildRecordResponse(savedRecord, savedDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthRecordResponse> getRecordsByResident(Integer residentId) {
        List<HealthRecord> records = healthRecordRepository.findByResidentIdOrderByRecordDateDesc(residentId);
        List<HealthRecordResponse> responses = new ArrayList<>();

        for (HealthRecord record : records) {
            List<HealthRecordDetail> details = healthRecordDetailRepository.findByHealthRecordId(record.getId());
            responses.add(buildRecordResponse(record, details));
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public HealthRecordResponse getRecordById(Integer id) {
        HealthRecord record = healthRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ sức khỏe ID " + id));

        List<HealthRecordDetail> details = healthRecordDetailRepository.findByHealthRecordId(record.getId());
        return buildRecordResponse(record, details);
    }

    @Override
    @Transactional
    public void cancelHealthRecord(Integer recordId) {
        HealthRecord record = healthRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ khám ID " + recordId));

        List<HealthRecordDetail> details = healthRecordDetailRepository.findByHealthRecordId(recordId);

        for (HealthRecordDetail detail : details) {
            Medication medication = medicationRepository.findByIdForUpdate(detail.getMedication().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thuốc ID " + detail.getMedication().getId()));

            medication.setQuantityInStock(medication.getQuantityInStock() + detail.getQuantity());
            medicationRepository.save(medication);
        }

        healthRecordDetailRepository.deleteAll(details);
        healthRecordRepository.delete(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationResponse> getAvailableMedications() {
        return medicationRepository.findAvailableForPrescription(LocalDate.now())
                .stream()
                .map(medicationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationResponse> getLowStockMedications() {
        return medicationRepository.findByQuantityInStockLessThan(10)
                .stream()
                .map(medicationMapper::toResponse)
                .toList();
    }

    private HealthRecordResponse buildRecordResponse(HealthRecord record, List<HealthRecordDetail> details) {
        HealthRecordResponse response = healthRecordMapper.toResponse(record);
        List<HealthRecordResponse.MedicationItemResponse> medicationResponses = new ArrayList<>();

        if (details != null) {
            for (HealthRecordDetail detail : details) {
                medicationResponses.add(
                        new HealthRecordResponse.MedicationItemResponse(
                                detail.getMedication().getId(),
                                detail.getMedication().getMedName(),
                                detail.getQuantity(),
                                detail.getDosage(),
                                detail.getUnitPrice(),
                                detail.getTotalPrice()
                        )
                );
            }
        }

        response.setMedications(medicationResponses);
        return response;
    }
}
