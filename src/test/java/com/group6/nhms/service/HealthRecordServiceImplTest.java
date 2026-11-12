package com.group6.nhms.service;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.entity.Medication;
import com.group6.nhms.entity.Order;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.enums.PaymentStatus;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.mapper.HealthRecordMapper;
import com.group6.nhms.mapper.MedicationMapper;
import com.group6.nhms.repository.HealthRecordDetailRepository;
import com.group6.nhms.repository.HealthRecordRepository;
import com.group6.nhms.repository.MedicationRepository;
import com.group6.nhms.repository.OrderRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.impl.HealthRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthRecordServiceImplTest {

    @Mock
    private HealthRecordRepository healthRecordRepository;
    @Mock
    private HealthRecordDetailRepository healthRecordDetailRepository;
    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private HealthRecordMapper healthRecordMapper;
    @Mock
    private MedicationMapper medicationMapper;
    @Mock
    private OrderRepository orderRepository;

    private HealthRecordServiceImpl healthRecordService;

    @BeforeEach
    void setUp() {
        healthRecordService = new HealthRecordServiceImpl(
                healthRecordRepository,
                healthRecordDetailRepository,
                medicationRepository,
                residentRepository,
                staffRepository,
                healthRecordMapper,
                medicationMapper,
                orderRepository
        );
    }

    @Test
    void createHealthRecord_addsMedicationCostToExistingPendingMonthlyOrder() {
        Resident resident = activeResident(1);
        Staff doctor = doctorStaff(2);
        Medication med = availableMed(10, new BigDecimal("50000"), 100);

        Order pendingMonthly = new Order();
        pendingMonthly.setId(50);
        pendingMonthly.setResident(resident);
        pendingMonthly.setIssueDate(LocalDate.now());
        pendingMonthly.setPaymentStatus(PaymentStatus.PENDING.name());
        pendingMonthly.setRoomPrice(new BigDecimal("5000000"));
        pendingMonthly.setServicesPrice(new BigDecimal("200000"));
        pendingMonthly.setMedicationsPrice(new BigDecimal("100000"));
        pendingMonthly.setTotalAmount(new BigDecimal("5300000"));

        stubBasicSaveFlow(resident, doctor, med, List.of(pendingMonthly));

        HealthRecordCreateRequest request = prescriptionRequest(1, 10, 3);
        healthRecordService.createHealthRecord(request, 2);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order updated = orderCaptor.getValue();
        assertEquals(50, updated.getId());
        assertEquals(new BigDecimal("250000"), updated.getMedicationsPrice());
        assertEquals(new BigDecimal("5450000"), updated.getTotalAmount());
    }

    @Test
    void createHealthRecord_createsSupplementaryPendingOrderWhenMonthAlreadyPaid() {
        Resident resident = activeResident(1);
        Staff doctor = doctorStaff(2);
        Medication med = availableMed(10, new BigDecimal("75000"), 50);

        Order paidOrder = new Order();
        paidOrder.setId(60);
        paidOrder.setResident(resident);
        paidOrder.setIssueDate(LocalDate.now());
        paidOrder.setPaymentStatus(PaymentStatus.PAID.name());
        paidOrder.setRoomPrice(new BigDecimal("5000000"));
        paidOrder.setServicesPrice(BigDecimal.ZERO);
        paidOrder.setMedicationsPrice(BigDecimal.ZERO);
        paidOrder.setTotalAmount(new BigDecimal("5000000"));

        stubBasicSaveFlow(resident, doctor, med, List.of(paidOrder));

        HealthRecordCreateRequest request = prescriptionRequest(1, 10, 2);
        healthRecordService.createHealthRecord(request, 2);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order supplementary = orderCaptor.getValue();
        assertEquals(PaymentStatus.PENDING.name(), supplementary.getPaymentStatus());
        assertEquals(BigDecimal.ZERO, supplementary.getRoomPrice());
        assertEquals(BigDecimal.ZERO, supplementary.getServicesPrice());
        assertEquals(new BigDecimal("150000"), supplementary.getMedicationsPrice());
        assertEquals(new BigDecimal("150000"), supplementary.getTotalAmount());
    }

    @Test
    void createHealthRecord_leavesBillingToMonthlySettlementWhenNoOrderExistsInMonth() {
        Resident resident = activeResident(1);
        Staff doctor = doctorStaff(2);
        Medication med = availableMed(10, new BigDecimal("50000"), 50);

        stubBasicSaveFlow(resident, doctor, med, List.of());

        HealthRecordCreateRequest request = prescriptionRequest(1, 10, 2);
        healthRecordService.createHealthRecord(request, 2);

        verify(orderRepository, never()).save(any(Order.class));
    }

    private void stubBasicSaveFlow(Resident resident, Staff doctor, Medication med, List<Order> existingOrders) {
        when(residentRepository.findById(1)).thenReturn(Optional.of(resident));
        when(staffRepository.findById(2)).thenReturn(Optional.of(doctor));
        when(medicationRepository.findByIdForUpdate(10)).thenReturn(Optional.of(med));
        when(healthRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(healthRecordDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findByResidentIdAndIssueDateBetween(any(), any(), any())).thenReturn(existingOrders);
        when(healthRecordMapper.toResponse(any())).thenReturn(new HealthRecordResponse());
    }

    private Resident activeResident(Integer id) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setFullName("Nguyễn Văn An");
        resident.setStatus(ResidentStatus.ACTIVE.name());
        return resident;
    }

    private Staff doctorStaff(Integer id) {
        Staff staff = new Staff();
        staff.setId(id);
        staff.setFullName("BS. Trần Minh");
        staff.setJobTitle("DOCTOR");
        return staff;
    }

    private Medication availableMed(Integer id, BigDecimal price, int stock) {
        Medication med = new Medication();
        med.setId(id);
        med.setMedName("Paracetamol");
        med.setUnitPrice(price);
        med.setQuantityInStock(stock);
        med.setExpiryDate(LocalDate.now().plusYears(1));
        return med;
    }

    private HealthRecordCreateRequest prescriptionRequest(Integer residentId, Integer medId, int qty) {
        HealthRecordCreateRequest.MedicationItemRequest item = new HealthRecordCreateRequest.MedicationItemRequest();
        item.setMedicationId(medId);
        item.setQuantityRequested(qty);
        item.setDosage("2 lần/ngày");

        HealthRecordCreateRequest req = new HealthRecordCreateRequest();
        req.setResidentId(residentId);
        req.setDiagnosis("Sốt nhẹ");
        req.setMedications(List.of(item));
        return req;
    }
}
