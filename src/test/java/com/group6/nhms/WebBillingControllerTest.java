package com.group6.nhms;

import com.group6.nhms.controller.WebBillingController;
import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.enums.PaymentStatus;
import com.group6.nhms.service.OrderService;
import com.group6.nhms.service.RelativeNotificationService;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.ResidentService;
import com.group6.nhms.service.StaffService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebBillingControllerTest {

    @Mock
    private OrderService orderService;
    @Mock
    private ResidentService residentService;
    @Mock
    private RelativeService relativeService;
    @Mock
    private RelativeNotificationService relativeNotificationService;
    @Mock
    private StaffService staffService;

    @Test
    @SuppressWarnings("unchecked")
    void monthlyBilling_excludesPreSettlementServiceDraftFromUnpaidTotalsAndSupportsDraftFilter() {
        WebBillingController controller = new WebBillingController(
                orderService, residentService, relativeService, relativeNotificationService, staffService);

        OrderResponse preSettlementDraft = new OrderResponse();
        preSettlementDraft.setId(101);
        preSettlementDraft.setResidentId(1);
        preSettlementDraft.setIssueDate(LocalDate.of(2026, 11, 10));
        preSettlementDraft.setRoomPrice(BigDecimal.ZERO);
        preSettlementDraft.setServicesPrice(new BigDecimal("400000"));
        preSettlementDraft.setTotalAmount(new BigDecimal("400000"));
        preSettlementDraft.setPaymentStatus(PaymentStatus.PENDING.name());

        OrderResponse officialSettledPending = new OrderResponse();
        officialSettledPending.setId(102);
        officialSettledPending.setResidentId(2);
        officialSettledPending.setIssueDate(LocalDate.of(2026, 11, 10));
        officialSettledPending.setRoomPrice(new BigDecimal("5000000"));
        officialSettledPending.setServicesPrice(BigDecimal.ZERO);
        officialSettledPending.setTotalAmount(new BigDecimal("5000000"));
        officialSettledPending.setPaymentStatus(PaymentStatus.PENDING.name());

        OrderResponse officialPaid = new OrderResponse();
        officialPaid.setId(103);
        officialPaid.setResidentId(3);
        officialPaid.setIssueDate(LocalDate.of(2026, 11, 10));
        officialPaid.setRoomPrice(new BigDecimal("8000000"));
        officialPaid.setTotalAmount(new BigDecimal("8000000"));
        officialPaid.setPaymentStatus(PaymentStatus.PAID.name());

        OrderResponse postPaidSupplementary = new OrderResponse();
        postPaidSupplementary.setId(104);
        postPaidSupplementary.setResidentId(3);
        postPaidSupplementary.setIssueDate(LocalDate.of(2026, 11, 12));
        postPaidSupplementary.setRoomPrice(BigDecimal.ZERO);
        postPaidSupplementary.setMedicationsPrice(new BigDecimal("150000"));
        postPaidSupplementary.setTotalAmount(new BigDecimal("150000"));
        postPaidSupplementary.setPaymentStatus(PaymentStatus.PENDING.name());

        List<OrderResponse> all = List.of(preSettlementDraft, officialSettledPending, officialPaid, postPaidSupplementary);
        when(orderService.getAllOrders(null)).thenReturn(all);
        when(orderService.getMonthlyRevenueSummaries(all)).thenReturn(List.of());
        when(residentService.getAllResidents(null, "ACTIVE")).thenReturn(List.of());

        Model allModel = new ExtendedModelMap();
        controller.monthlyBilling(null, 11, 2026, allModel);

        // Official period orders: #102 (5M), #103 (8M), #104 (150k supplementary after #103 PAID) = 3; #101 is a pre-settlement draft
        assertEquals(3, allModel.getAttribute("totalPeriodOrders"));
        assertEquals(2L, allModel.getAttribute("unpaidCount"));
        assertEquals(1L, allModel.getAttribute("paidCount"));
        assertEquals(new BigDecimal("5150000"), allModel.getAttribute("totalPendingAmount"));
        assertEquals(1L, allModel.getAttribute("draftCount"));
        assertEquals(new BigDecimal("400000"), allModel.getAttribute("draftTotalAmount"));

        Set<Integer> draftIds = (Set<Integer>) allModel.getAttribute("draftOrderIds");
        assertTrue(draftIds.contains(101));
        assertFalse(draftIds.contains(104));

        Model draftModel = new ExtendedModelMap();
        controller.monthlyBilling("DRAFT", 11, 2026, draftModel);
        List<OrderResponse> draftRows = (List<OrderResponse>) draftModel.getAttribute("orders");
        assertEquals(1, draftRows.size());
        assertEquals(101, draftRows.get(0).getId());
    }
}
