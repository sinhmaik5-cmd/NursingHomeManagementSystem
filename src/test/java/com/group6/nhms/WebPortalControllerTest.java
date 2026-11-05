package com.group6.nhms;

import com.group6.nhms.controller.web.WebPortalController;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.IncidentReportResponse;
import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.dto.response.ResidentPortalResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.service.HealthRecordService;
import com.group6.nhms.service.IncidentReportService;
import com.group6.nhms.service.OrderService;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.ResidentService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebPortalControllerTest {

    private final RelativeService relativeService = mock(RelativeService.class);
    private final ResidentService residentService = mock(ResidentService.class);
    private final HealthRecordService healthRecordService = mock(HealthRecordService.class);
    private final IncidentReportService incidentReportService = mock(IncidentReportService.class);
    private final OrderService orderService = mock(OrderService.class);

    @Test
    void dashboardUsesDataForEachLinkedResident() {
        Authentication authentication = mock(Authentication.class);
        Relative relative = new Relative();
        relative.setId(7);
        ResidentResponse firstResident = resident(10);
        ResidentResponse secondResident = resident(20);
        HealthRecordResponse firstRecord = new HealthRecordResponse();
        HealthRecordResponse secondRecord = new HealthRecordResponse();
        IncidentReportResponse firstIncident = new IncidentReportResponse();
        IncidentReportResponse secondIncident = new IncidentReportResponse();
        OrderResponse firstOrder = new OrderResponse();
        OrderResponse secondOrder = new OrderResponse();

        when(relativeService.resolveCurrentRelative(authentication)).thenReturn(relative);
        when(residentService.getResidentsByRelative(7)).thenReturn(List.of(firstResident, secondResident));
        when(healthRecordService.getLatestRecordsByResidents(List.of(10, 20)))
                .thenReturn(Map.of(10, firstRecord, 20, secondRecord));
        when(incidentReportService.getResidentIncidentsByResidentIds(7, List.of(10, 20)))
                .thenReturn(Map.of(10, List.of(firstIncident), 20, List.of(secondIncident)));
        when(orderService.getOrdersByResidents(List.of(10, 20)))
                .thenReturn(Map.of(10, List.of(firstOrder), 20, List.of(secondOrder)));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller().portalDashboard(authentication, model);

        assertEquals("portal/dashboard", view);
        List<?> portals = (List<?>) model.get("residentPortals");
        assertEquals(2, portals.size());
        ResidentPortalResponse firstPortal = (ResidentPortalResponse) portals.get(0);
        ResidentPortalResponse secondPortal = (ResidentPortalResponse) portals.get(1);
        assertSame(firstResident, firstPortal.getResident());
        assertSame(firstRecord, firstPortal.getLatestRecord());
        assertSame(firstIncident, firstPortal.getIncidents().get(0));
        assertSame(firstOrder, firstPortal.getOrders().get(0));
        assertSame(secondResident, secondPortal.getResident());
        assertSame(secondRecord, secondPortal.getLatestRecord());
        assertSame(secondIncident, secondPortal.getIncidents().get(0));
        assertSame(secondOrder, secondPortal.getOrders().get(0));
        verify(incidentReportService).getResidentIncidentsByResidentIds(7, List.of(10, 20));
    }

    private WebPortalController controller() {
        return new WebPortalController(relativeService, residentService, healthRecordService,
                incidentReportService, orderService);
    }

    private ResidentResponse resident(Integer id) {
        ResidentResponse resident = new ResidentResponse();
        resident.setId(id);
        return resident;
    }
}
