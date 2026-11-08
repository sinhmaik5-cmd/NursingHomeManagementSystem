package com.group6.nhms;

import com.group6.nhms.dto.response.NotificationSummaryDto;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.repository.*;
import com.group6.nhms.service.RelativeNotificationService;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.StaffService;
import com.group6.nhms.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private IncidentReportRepository incidentReportRepository;
    private BedRepository bedRepository;
    private OrderRepository orderRepository;
    private MedicationRepository medicationRepository;
    private TaskScheduleRepository taskScheduleRepository;
    private HealthRecordRepository healthRecordRepository;
    private ResidentRepository residentRepository;
    private StaffService staffService;
    private RelativeService relativeService;
    private RelativeNotificationService relativeNotificationService;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        incidentReportRepository = mock(IncidentReportRepository.class);
        bedRepository = mock(BedRepository.class);
        orderRepository = mock(OrderRepository.class);
        medicationRepository = mock(MedicationRepository.class);
        taskScheduleRepository = mock(TaskScheduleRepository.class);
        healthRecordRepository = mock(HealthRecordRepository.class);
        residentRepository = mock(ResidentRepository.class);
        staffService = mock(StaffService.class);
        relativeService = mock(RelativeService.class);
        relativeNotificationService = mock(RelativeNotificationService.class);

        notificationService = new NotificationServiceImpl(
                incidentReportRepository,
                bedRepository,
                orderRepository,
                medicationRepository,
                taskScheduleRepository,
                healthRecordRepository,
                residentRepository,
                staffService,
                relativeService,
                relativeNotificationService
        );
    }

    @Test
    void unauthenticatedUserHasNoNotifications() {
        NotificationSummaryDto summary = notificationService.getNotificationsForUser(null);
        assertFalse(summary.isHasNew());
        assertEquals(0, summary.getTotalNewCount());
        assertTrue(summary.getItems().isEmpty());
    }

    @Test
    void managerWithPendingIncidentsHasRedDot() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "manager", "password", List.of(new SimpleGrantedAuthority("ROLE_CENTER_MANAGER")));

        when(incidentReportRepository.countByStatus("PENDING")).thenReturn(3L);
        when(bedRepository.countByStatus("MAINTENANCE")).thenReturn(0L);
        when(orderRepository.countByPaymentStatus("PENDING")).thenReturn(0L);
        when(medicationRepository.findByQuantityInStockLessThan(10)).thenReturn(List.of());

        NotificationSummaryDto summary = notificationService.getNotificationsForUser(auth);

        assertTrue(summary.isHasNew(), "Main bell red dot should be visible when there is at least one new alert");
        assertEquals(1, summary.getTotalNewCount());
        assertEquals(4, summary.getItems().size());

        // Incident item should have red dot (hasNew == true)
        assertTrue(summary.getItems().get(0).isHasNew());
        assertEquals(3, summary.getItems().get(0).getCount());

        // Bed item should NOT have red dot (hasNew == false)
        assertFalse(summary.getItems().get(1).isHasNew());
        assertEquals(0, summary.getItems().get(1).getCount());
    }

    @Test
    void relativeWithNoPendingAlertsHasNoRedDot() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "relative", "password", List.of(new SimpleGrantedAuthority("ROLE_RELATIVE")));

        Relative relative = new Relative();
        relative.setId(10);
        when(relativeService.resolveCurrentRelative(auth)).thenReturn(relative);

        Resident resident = new Resident();
        resident.setId(100);
        when(residentRepository.findByRelativeId(10)).thenReturn(List.of(resident));

        when(incidentReportRepository.findByResidentIdIn(List.of(100))).thenReturn(List.of());
        when(healthRecordRepository.findByResidentIdInOrderByRecordDateDesc(List.of(100))).thenReturn(List.of());
        when(orderRepository.findByResidentIdInOrderByIssueDateDesc(List.of(100))).thenReturn(List.of());

        NotificationSummaryDto summary = notificationService.getNotificationsForUser(auth);

        assertFalse(summary.isHasNew(), "Main bell red dot should NOT be visible when there are no new notifications");
        assertEquals(0, summary.getTotalNewCount());
        assertEquals(3, summary.getItems().size());
        for (var item : summary.getItems()) {
            assertFalse(item.isHasNew(), "Item should not have red dot when count is 0");
        }
    }

    @Test
    void relativeWithPendingIncidentHasRedDotOnSpecificItem() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "relative", "password", List.of(new SimpleGrantedAuthority("ROLE_RELATIVE")));

        Relative relative = new Relative();
        relative.setId(10);
        when(relativeService.resolveCurrentRelative(auth)).thenReturn(relative);

        Resident resident = new Resident();
        resident.setId(100);
        when(residentRepository.findByRelativeId(10)).thenReturn(List.of(resident));

        com.group6.nhms.entity.IncidentReport incident = new com.group6.nhms.entity.IncidentReport();
        incident.setStatus("PENDING");
        when(incidentReportRepository.findByResidentIdIn(List.of(100))).thenReturn(List.of(incident));
        when(healthRecordRepository.findByResidentIdInOrderByRecordDateDesc(List.of(100))).thenReturn(List.of());
        when(orderRepository.findByResidentIdInOrderByIssueDateDesc(List.of(100))).thenReturn(List.of());

        NotificationSummaryDto summary = notificationService.getNotificationsForUser(auth);

        assertTrue(summary.isHasNew(), "Main bell must show red dot");
        assertEquals(1, summary.getTotalNewCount());

        // First item (Sự cố của người thân) must have red dot
        assertTrue(summary.getItems().get(0).isHasNew());
        assertEquals(1, summary.getItems().get(0).getCount());

        // Other items must not have red dot
        assertFalse(summary.getItems().get(1).isHasNew());
        assertFalse(summary.getItems().get(2).isHasNew());
    }
}
