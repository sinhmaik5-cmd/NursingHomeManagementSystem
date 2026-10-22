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

                // 1. Nghiệp vụ: Chặn kê đơn nếu thuốc đã hết hạn (hạn dùng <= ngày hiện tại)
                if (medication.getExpiryDate() != null && !medication.getExpiryDate().isAfter(LocalDate.now())) {
                    throw new BadRequestException("Thuốc " + medication.getMedName()
                            + " đã hết hạn sử dụng (" + medication.getExpiryDate() + "), không thể kê đơn");
                }

                Integer stock = medication.getQuantityInStock();

                // 2. Kiểm tra tồn kho
                if (stock == null || stock < item.getQuantityRequested()) {
                    throw new BadRequestException("Thuốc " + medication.getMedName()
                            + " trong kho không đủ (còn " + (stock == null ? 0 : stock)
                            + ", yêu cầu " + item.getQuantityRequested() + ")");
                }

                // 3. Trừ kho tự động
                medication.setQuantityInStock(stock - item.getQuantityRequested());

                // 4. Lưu vết đơn giá và thành tiền để tính phí cuối tháng
                BigDecimal unitPrice = (medication.getUnitPrice() != null) ? medication.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantityRequested()));

                HealthRecordDetail detail = new HealthRecordDetail();
                detail.setHealthRecord(savedRecord);
                detail.setMedication(medication);
                detail.setQuantity(item.getQuantityRequested());
                detail.setDosage(item.getDosage());
                detail.setUnitPrice(unitPrice);
                detail.setTotalPrice(totalPrice);

                savedDetails.add(detail);
            }
        }

        List<HealthRecordDetail> persistedDetails =
                healthRecordDetailRepository.saveAll(savedDetails);

        return buildRecordResponse(savedRecord, persistedDetails);
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ sức khỏe ID " + recordId));

        // Nghiệp vụ: Hoàn kho (Rollback) toàn bộ thuốc đã kê khi hủy đơn thuốc / phiếu khám
        List<HealthRecordDetail> details = healthRecordDetailRepository.findByHealthRecordId(recordId);
        for (HealthRecordDetail detail : details) {
            if (detail.getMedication() != null && detail.getQuantity() != null) {
                Medication medication = medicationRepository.findByIdForUpdate(detail.getMedication().getId())
                        .orElse(null);
                if (medication != null) {
                    int currentStock = (medication.getQuantityInStock() != null) ? medication.getQuantityInStock() : 0;
                    medication.setQuantityInStock(currentStock + detail.getQuantity());
                    medicationRepository.save(medication);
                }
            }
        }

        // Xóa chi tiết và hồ sơ khám
        healthRecordDetailRepository.deleteAll(details);
        healthRecordRepository.delete(record);
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
