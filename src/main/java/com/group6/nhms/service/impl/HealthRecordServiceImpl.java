package com.group6.nhms.service.impl;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.MedicationResponse;
import com.group6.nhms.entity.HealthRecord;
import com.group6.nhms.entity.HealthRecordDetail;
import com.group6.nhms.entity.Medication;
import com.group6.nhms.entity.Order;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.enums.PaymentStatus;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.HealthRecordMapper;
import com.group6.nhms.mapper.MedicationMapper;
import com.group6.nhms.repository.HealthRecordDetailRepository;
import com.group6.nhms.repository.HealthRecordRepository;
import com.group6.nhms.repository.MedicationRepository;
import com.group6.nhms.repository.OrderRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.HealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final HealthRecordDetailRepository healthRecordDetailRepository;
    private final MedicationRepository medicationRepository;
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;
    private final HealthRecordMapper healthRecordMapper;
    private final MedicationMapper medicationMapper;
    private final OrderRepository orderRepository;

    @Autowired
    public HealthRecordServiceImpl(
            HealthRecordRepository healthRecordRepository,
            HealthRecordDetailRepository healthRecordDetailRepository,
            MedicationRepository medicationRepository,
            ResidentRepository residentRepository,
            StaffRepository staffRepository,
            HealthRecordMapper healthRecordMapper,
            MedicationMapper medicationMapper,
            @Autowired(required = false) OrderRepository orderRepository
    ) {
        this.healthRecordRepository = healthRecordRepository;
        this.healthRecordDetailRepository = healthRecordDetailRepository;
        this.medicationRepository = medicationRepository;
        this.residentRepository = residentRepository;
        this.staffRepository = staffRepository;
        this.healthRecordMapper = healthRecordMapper;
        this.medicationMapper = medicationMapper;
        this.orderRepository = orderRepository;
    }

    public HealthRecordServiceImpl(
            HealthRecordRepository healthRecordRepository,
            HealthRecordDetailRepository healthRecordDetailRepository,
            MedicationRepository medicationRepository,
            ResidentRepository residentRepository,
            StaffRepository staffRepository,
            HealthRecordMapper healthRecordMapper,
            MedicationMapper medicationMapper
    ) {
        this(
                healthRecordRepository,
                healthRecordDetailRepository,
                medicationRepository,
                residentRepository,
                staffRepository,
                healthRecordMapper,
                medicationMapper,
                null
        );
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

        // Đồng bộ tiền thuốc mới vào hóa đơn PENDING cùng tháng (hoặc tạo hóa đơn bổ sung nếu tháng đã PAID)
        syncNewMedicationCostToOrder(resident, savedRecord.getRecordDate(), savedDetails);

        return buildRecordResponse(savedRecord, savedDetails);
    }

    private void syncNewMedicationCostToOrder(
            Resident resident,
            Instant recordDate,
            List<HealthRecordDetail> savedDetails
    ) {
        if (orderRepository == null || savedDetails == null || savedDetails.isEmpty()) {
            return;
        }
        BigDecimal newMedicationCost = savedDetails.stream()
                .map(HealthRecordDetail::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (newMedicationCost.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        LocalDate recordDay = (recordDate != null ? recordDate : Instant.now())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate startOfMonth = recordDay.withDayOfMonth(1);
        LocalDate endOfMonth = recordDay.withDayOfMonth(recordDay.lengthOfMonth());

        List<Order> monthlyOrders = orderRepository.findByResidentIdAndIssueDateBetween(
                resident.getId(), startOfMonth, endOfMonth);
        if (monthlyOrders == null || monthlyOrders.isEmpty()) {
            return;
        }

        Order pendingOrder = monthlyOrders.stream()
                .filter(o -> PaymentStatus.PENDING.name().equalsIgnoreCase(o.getPaymentStatus())
                        || "UNPAID".equalsIgnoreCase(o.getPaymentStatus()))
                .filter(o -> o.getRoomPrice() != null && o.getRoomPrice().compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .orElseGet(() -> monthlyOrders.stream()
                        .filter(o -> PaymentStatus.PENDING.name().equalsIgnoreCase(o.getPaymentStatus())
                                || "UNPAID".equalsIgnoreCase(o.getPaymentStatus()))
                        .findFirst()
                        .orElse(null));

        if (pendingOrder != null) {
            BigDecimal currentMeds = pendingOrder.getMedicationsPrice() != null
                    ? pendingOrder.getMedicationsPrice()
                    : BigDecimal.ZERO;
            BigDecimal updatedMeds = currentMeds.add(newMedicationCost);
            BigDecimal room = pendingOrder.getRoomPrice() != null ? pendingOrder.getRoomPrice() : BigDecimal.ZERO;
            BigDecimal services = pendingOrder.getServicesPrice() != null ? pendingOrder.getServicesPrice() : BigDecimal.ZERO;
            pendingOrder.setMedicationsPrice(updatedMeds);
            pendingOrder.setTotalAmount(room.add(services).add(updatedMeds));
            orderRepository.save(pendingOrder);
            return;
        }

        boolean hasPaidOrder = monthlyOrders.stream()
                .anyMatch(o -> PaymentStatus.PAID.name().equalsIgnoreCase(o.getPaymentStatus()));
        if (hasPaidOrder) {
            Order supplementaryOrder = new Order();
            supplementaryOrder.setResident(resident);
            supplementaryOrder.setIssueDate(recordDay);
            supplementaryOrder.setDueDate(recordDay.plusDays(10));
            supplementaryOrder.setPaymentStatus(PaymentStatus.PENDING.name());
            supplementaryOrder.setRoomPrice(BigDecimal.ZERO);
            supplementaryOrder.setServicesPrice(BigDecimal.ZERO);
            supplementaryOrder.setMedicationsPrice(newMedicationCost);
            supplementaryOrder.setTotalAmount(newMedicationCost);
            orderRepository.save(supplementaryOrder);
        }
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
    public Map<Integer, HealthRecordResponse> getLatestRecordsByResidents(List<Integer> residentIds) {
        if (residentIds.isEmpty()) {
            return Map.of();
        }

        List<HealthRecord> records = healthRecordRepository.findByResidentIdInOrderByRecordDateDesc(residentIds);
        if (records.isEmpty()) {
            return Map.of();
        }

        List<Integer> recordIds = records.stream().map(HealthRecord::getId).toList();
        Map<Integer, List<HealthRecordDetail>> detailsByRecordId =
                healthRecordDetailRepository.findByHealthRecordIdIn(recordIds).stream()
                        .collect(Collectors.groupingBy(detail -> detail.getHealthRecord().getId()));

        Map<Integer, HealthRecordResponse> latestRecords = new LinkedHashMap<>();
        for (HealthRecord record : records) {
            latestRecords.computeIfAbsent(record.getResident().getId(), residentId ->
                    buildRecordResponse(record, detailsByRecordId.getOrDefault(record.getId(), List.of())));
        }
        return latestRecords;
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
