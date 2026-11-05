package com.group6.nhms;

import com.group6.nhms.controller.web.WebBillingController;
import com.group6.nhms.controller.web.WebHealthController;
import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Account;
import com.group6.nhms.entity.HealthRecord;
import com.group6.nhms.entity.HealthRecordDetail;
import com.group6.nhms.entity.Medication;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.mapper.HealthRecordMapper;
import com.group6.nhms.mapper.MedicationMapper;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.repository.HealthRecordDetailRepository;
import com.group6.nhms.repository.HealthRecordRepository;
import com.group6.nhms.repository.MedicationRepository;
import com.group6.nhms.repository.RelativeRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.HealthRecordService;
import com.group6.nhms.service.OrderService;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.ResidentService;
import com.group6.nhms.service.StaffService;
import com.group6.nhms.service.impl.HealthRecordServiceImpl;
import com.group6.nhms.service.impl.RelativeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityAuthorizationRegressionTests {

    @Mock
    private OrderService orderService;
    @Mock
    private ResidentService residentService;
    @Mock
    private RelativeService relativeService;
    @Mock
    private HealthRecordService healthRecordService;
    @Mock
    private StaffService staffService;
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
    private AccountRepository accountRepository;
    @Mock
    private RelativeRepository relativeRepository;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void relativeCanViewInvoiceForLinkedResident() {
        OrderResponse order = orderForResident(12);
        ResidentResponse resident = residentLinkedTo(12, 4);
        Relative relative = relativeWithId(4);
        when(orderService.getOrderById(8)).thenReturn(order);
        when(relativeService.resolveCurrentRelative(relativeAuthentication())).thenReturn(relative);
        when(residentService.getResidentById(12)).thenReturn(resident);
        WebBillingController controller = billingController();
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.orderDetail(8, relativeAuthentication(), model);

        assertEquals("billing/order-detail", view);
        assertSame(order, model.get("order"));
        assertSame(resident, model.get("resident"));
    }

    @Test
    void relativeCannotViewInvoiceForUnlinkedResident() {
        when(orderService.getOrderById(8)).thenReturn(orderForResident(12));
        when(relativeService.resolveCurrentRelative(relativeAuthentication())).thenReturn(relativeWithId(4));
        when(residentService.getResidentById(12)).thenReturn(residentLinkedTo(12, 5));
        ExtendedModelMap model = new ExtendedModelMap();

        assertThrows(AccessDeniedException.class,
                () -> billingController().orderDetail(8, relativeAuthentication(), model));
        assertNull(model.get("order"));
        assertNull(model.get("resident"));
    }

    @Test
    void relativeCannotViewHealthHistoryForUnlinkedResident() {
        when(relativeService.resolveCurrentRelative(relativeAuthentication())).thenReturn(relativeWithId(4));
        when(residentService.getResidentsByRelative(4)).thenReturn(List.of(residentLinkedTo(12, 4)));

        assertThrows(AccessDeniedException.class, () -> healthController().showHealthHistory(
                13, relativeAuthentication(), new ExtendedModelMap()));
        verify(healthRecordService, never()).getRecordsByResident(13);
    }

    @Test
    void relativeHealthHistoryLoadsOnlyLinkedResidents() {
        ResidentResponse linkedResident = residentLinkedTo(12, 4);
        when(relativeService.resolveCurrentRelative(relativeAuthentication())).thenReturn(relativeWithId(4));
        when(residentService.getResidentsByRelative(4)).thenReturn(List.of(linkedResident));
        when(healthRecordService.getRecordsByResident(12)).thenReturn(List.of());
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("health/history", healthController().showHealthHistory(
                12, relativeAuthentication(), model));
        assertEquals(List.of(linkedResident), model.get("residents"));
        assertSame(linkedResident, model.get("selectedResident"));
    }

    @Test
    void relativeWithNoAccountAssociationIsDeniedWithoutLoadingResident() {
        when(orderService.getOrderById(8)).thenReturn(orderForResident(12));
        when(relativeService.resolveCurrentRelative(relativeAuthentication())).thenReturn(null);

        assertThrows(AccessDeniedException.class,
                () -> billingController().orderDetail(8, relativeAuthentication(), new ExtendedModelMap()));
        verify(residentService, never()).getResidentById(12);
    }

    @Test
    void nurseMedicationSubmissionIsRejectedBeforeHealthRecordCreation() {
        HealthRecordCreateRequest request = new HealthRecordCreateRequest();
        request.setResidentId(12);
        request.setMedications(List.of(new HealthRecordCreateRequest.MedicationItemRequest(2, 1, "daily")));
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = healthController().processCreateHealthRecord(request, nurseAuthentication(), redirectAttributes);

        assertEquals("redirect:/health/records/create?residentId=12", view);
        assertNotNull(redirectAttributes.getFlashAttributes().get("errorMessage"));
        verifyNoInteractions(healthRecordService, staffService);
    }

    @Test
    void nurseMedicationSubmissionCannotReachStockDeductionInService() {
        Resident resident = new Resident();
        resident.setStatus("ACTIVE");
        Staff nurse = new Staff();
        nurse.setJobTitle("NURSE");
        when(residentRepository.findById(12)).thenReturn(Optional.of(resident));
        when(staffRepository.findById(3)).thenReturn(Optional.of(nurse));
        HealthRecordCreateRequest request = new HealthRecordCreateRequest();
        request.setResidentId(12);
        request.setMedications(List.of(new HealthRecordCreateRequest.MedicationItemRequest(2, 1, "daily")));

        assertThrows(BadRequestException.class, () -> healthRecordService().createHealthRecord(request, 3));
        verifyNoInteractions(medicationRepository);
        verify(healthRecordRepository, never()).save(any());
    }

    @Test
    void doctorPrescriptionStillDeductsMedicationStock() {
        Resident resident = new Resident();
        resident.setStatus("ACTIVE");
        Staff doctor = new Staff();
        doctor.setJobTitle("DOCTOR");
        Medication medication = new Medication();
        medication.setId(2);
        medication.setMedName("Test medication");
        medication.setQuantityInStock(5);
        medication.setUnitPrice(new BigDecimal("3.50"));
        when(residentRepository.findById(12)).thenReturn(Optional.of(resident));
        when(staffRepository.findById(3)).thenReturn(Optional.of(doctor));
        when(medicationRepository.findByIdForUpdate(2)).thenReturn(Optional.of(medication));
        when(healthRecordRepository.save(any(HealthRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(healthRecordDetailRepository.save(any(HealthRecordDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(healthRecordMapper.toResponse(any(HealthRecord.class))).thenReturn(new HealthRecordResponse());
        HealthRecordCreateRequest request = new HealthRecordCreateRequest();
        request.setResidentId(12);
        request.setMedications(List.of(new HealthRecordCreateRequest.MedicationItemRequest(2, 1, "daily")));

        healthRecordService().createHealthRecord(request, 3);

        assertEquals(4, medication.getQuantityInStock());
        verify(medicationRepository).save(medication);
    }

    @Test
    void doctorCanCreateHealthRecordWithoutMedicationOrStockDeduction() {
        Resident resident = new Resident();
        resident.setStatus("ACTIVE");
        Staff doctor = new Staff();
        doctor.setJobTitle("DOCTOR");
        when(residentRepository.findById(12)).thenReturn(Optional.of(resident));
        when(staffRepository.findById(3)).thenReturn(Optional.of(doctor));
        when(healthRecordRepository.save(any(HealthRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(healthRecordMapper.toResponse(any(HealthRecord.class))).thenReturn(new HealthRecordResponse());
        HealthRecordCreateRequest request = new HealthRecordCreateRequest();
        request.setResidentId(12);
        request.setMedications(List.of());

        healthRecordService().createHealthRecord(request, 3);

        verify(healthRecordRepository).save(any(HealthRecord.class));
        verifyNoInteractions(medicationRepository, healthRecordDetailRepository);
    }

    @Test
    void relativeResolutionDoesNotFallBackToAnotherRelativeForNonRelativeAccount() {
        Account manager = new Account();
        manager.setRole("CENTER_MANAGER");
        manager.setReferenceId(4);
        when(accountRepository.findByUsername("manager")).thenReturn(Optional.of(manager));
        Authentication authentication = new UsernamePasswordAuthenticationToken("manager", "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_CENTER_MANAGER")));

        Relative resolved = relativeServiceImpl().resolveCurrentRelative(authentication);

        assertNull(resolved);
        verifyNoInteractions(relativeRepository);
    }

    @Test
    void relativeResolutionUsesAuthenticatedAccountReference() {
        Account account = new Account();
        account.setRole("RELATIVE");
        account.setReferenceId(4);
        Relative relative = relativeWithId(4);
        when(accountRepository.findByUsername("family")).thenReturn(Optional.of(account));
        when(relativeRepository.findById(4)).thenReturn(Optional.of(relative));
        Authentication authentication = relativeAuthentication();

        Relative resolved = relativeServiceImpl().resolveCurrentRelative(authentication);

        assertSame(relative, resolved);
    }

    private WebBillingController billingController() {
        return new WebBillingController(orderService, residentService, relativeService);
    }

    private WebHealthController healthController() {
        return new WebHealthController(healthRecordService, residentService, staffService, relativeService);
    }

    private HealthRecordServiceImpl healthRecordService() {
        return new HealthRecordServiceImpl(healthRecordRepository, healthRecordDetailRepository,
                medicationRepository, residentRepository, staffRepository, healthRecordMapper, medicationMapper);
    }

    private RelativeServiceImpl relativeServiceImpl() {
        return new RelativeServiceImpl(relativeRepository, accountRepository, passwordEncoder);
    }

    private Authentication relativeAuthentication() {
        return new UsernamePasswordAuthenticationToken("family", "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_RELATIVE")));
    }

    private Authentication nurseAuthentication() {
        return new UsernamePasswordAuthenticationToken("nurse", "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_NURSE")));
    }

    private OrderResponse orderForResident(Integer residentId) {
        OrderResponse order = new OrderResponse();
        order.setId(8);
        order.setResidentId(residentId);
        return order;
    }

    private ResidentResponse residentLinkedTo(Integer residentId, Integer relativeId) {
        ResidentResponse resident = new ResidentResponse();
        resident.setId(residentId);
        resident.setRelativeId(relativeId);
        return resident;
    }

    private Relative relativeWithId(Integer id) {
        Relative relative = new Relative();
        relative.setId(id);
        return relative;
    }
}
