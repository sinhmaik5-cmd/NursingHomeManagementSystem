package com.group6.nhms.service;

import com.group6.nhms.dto.response.MonthlyRevenueSummary;
import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.entity.Bed;
import com.group6.nhms.entity.Order;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Room;
import com.group6.nhms.enums.PaymentStatus;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.mapper.OrderDetailMapper;
import com.group6.nhms.mapper.OrderMapper;
import com.group6.nhms.repository.HealthRecordDetailRepository;
import com.group6.nhms.repository.OrderDetailRepository;
import com.group6.nhms.repository.OrderRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.ServiceRepository;
import com.group6.nhms.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private HealthRecordDetailRepository healthRecordDetailRepository;
    @Mock
    private ServiceRepository serviceRepository;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        OrderMapper orderMapper = new OrderMapper(new ModelMapper(), new OrderDetailMapper());
        orderService = new OrderServiceImpl(
                orderRepository,
                orderDetailRepository,
                residentRepository,
                healthRecordDetailRepository,
                serviceRepository,
                orderMapper
        );
    }

    @Test
    void createMonthlySettlement_proRatesFirstMonthWhenAdmittedMidMonth() {
        LocalDate admissionDate = LocalDate.of(2026, 11, 12);
        Resident resident = buildActiveResident(1, admissionDate, new BigDecimal("5000000"));

        when(residentRepository.findById(1)).thenReturn(Optional.of(resident));
        when(orderRepository.existsByResidentIdAndIssueDateBetween(any(), any(), any())).thenReturn(false);
        when(orderDetailRepository.sumServicesByResidentIdAndIssueDateBetween(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(healthRecordDetailRepository.sumTotalPriceByResidentIdAndRecordDateBetween(any(), any(), any()))
                .thenReturn(new BigDecimal("200000"));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(10);
            return o;
        });

        OrderResponse res = orderService.createMonthlySettlement(1, 2026, 11);

        // 19/30 * 5,000,000 = 3,166,666.67 -> làm tròn hàng nghìn = 3,167,000
        assertEquals(new BigDecimal("3167000"), res.getRoomPrice());
        assertEquals(new BigDecimal("3367000"), res.getTotalAmount());
        assertEquals("19/30 ngày", res.getRoomQuantityLabel());
        assertEquals("12/11/2026 - 30/11/2026", res.getRoomBillingPeriod());
        assertTrue(!res.getIssueDate().isBefore(admissionDate) && !res.getIssueDate().isAfter(LocalDate.of(2026, 11, 30)));
        assertEquals(res.getIssueDate().plusDays(10), res.getDueDate());
    }

    @Test
    void createMonthlySettlement_chargesFullMonthForSubsequentMonth() {
        LocalDate admissionDate = LocalDate.of(2026, 11, 12);
        Resident resident = buildActiveResident(1, admissionDate, new BigDecimal("5000000"));

        when(residentRepository.findById(1)).thenReturn(Optional.of(resident));
        when(orderRepository.existsByResidentIdAndIssueDateBetween(any(), any(), any())).thenReturn(false);
        when(orderDetailRepository.sumServicesByResidentIdAndIssueDateBetween(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(healthRecordDetailRepository.sumTotalPriceByResidentIdAndRecordDateBetween(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse res = orderService.createMonthlySettlement(1, 2026, 12);

        assertEquals(new BigDecimal("5000000"), res.getRoomPrice());
        assertEquals("1 tháng", res.getRoomQuantityLabel());
        assertEquals("01/12/2026 - 31/12/2026", res.getRoomBillingPeriod());
        assertEquals(LocalDate.of(2026, 12, 1), res.getIssueDate());
    }

    @Test
    void createMonthlySettlement_rejectsMonthBeforeAdmissionDate() {
        Resident resident = buildActiveResident(1, LocalDate.of(2026, 11, 12), new BigDecimal("5000000"));
        when(residentRepository.findById(1)).thenReturn(Optional.of(resident));

        assertThrows(BadRequestException.class, () -> orderService.createMonthlySettlement(1, 2026, 10));
    }

    @Test
    void cancelUnpaidOrder_deletesPendingOrder() {
        Order pendingOrder = new Order();
        pendingOrder.setId(5);
        pendingOrder.setPaymentStatus(PaymentStatus.PENDING.name());
        when(orderRepository.findById(5)).thenReturn(Optional.of(pendingOrder));

        orderService.cancelUnpaidOrder(5);

        verify(orderRepository).delete(pendingOrder);
    }

    @Test
    void cancelUnpaidOrder_rejectsPaidAndNonUnpaidOrder() {
        Order paidOrder = new Order();
        paidOrder.setId(6);
        paidOrder.setPaymentStatus(PaymentStatus.PAID.name());
        when(orderRepository.findById(6)).thenReturn(Optional.of(paidOrder));

        assertThrows(BadRequestException.class, () -> orderService.cancelUnpaidOrder(6));

        Order unknownOrder = new Order();
        unknownOrder.setId(7);
        unknownOrder.setPaymentStatus(null);
        when(orderRepository.findById(7)).thenReturn(Optional.of(unknownOrder));

        assertThrows(BadRequestException.class, () -> orderService.cancelUnpaidOrder(7));
    }

    @Test
    void getMonthlyRevenueSummaries_aggregatesCollectedAndPendingByMonthDescending() {
        OrderResponse novPaid = new OrderResponse();
        novPaid.setIssueDate(LocalDate.of(2026, 11, 12));
        novPaid.setRoomPrice(new BigDecimal("3167000"));
        novPaid.setServicesPrice(BigDecimal.ZERO);
        novPaid.setMedicationsPrice(new BigDecimal("200000"));
        novPaid.setTotalAmount(new BigDecimal("3367000"));
        novPaid.setPaymentStatus(PaymentStatus.PAID.name());

        OrderResponse decPending = new OrderResponse();
        decPending.setIssueDate(LocalDate.of(2026, 12, 1));
        decPending.setRoomPrice(new BigDecimal("5000000"));
        decPending.setServicesPrice(BigDecimal.ZERO);
        decPending.setMedicationsPrice(BigDecimal.ZERO);
        decPending.setTotalAmount(new BigDecimal("5000000"));
        decPending.setPaymentStatus(PaymentStatus.PENDING.name());

        List<MonthlyRevenueSummary> summaries = orderService.getMonthlyRevenueSummaries(List.of(novPaid, decPending));

        assertEquals(2, summaries.size());
        assertEquals("12/2026", summaries.get(0).getPeriodLabel());
        assertEquals(new BigDecimal("5000000"), summaries.get(0).getPendingRevenue());
        assertEquals(BigDecimal.ZERO, summaries.get(0).getCollectedRevenue());

        assertEquals("11/2026", summaries.get(1).getPeriodLabel());
        assertEquals(new BigDecimal("3367000"), summaries.get(1).getCollectedRevenue());
        assertEquals(BigDecimal.ZERO, summaries.get(1).getPendingRevenue());
    }

    private Resident buildActiveResident(Integer id, LocalDate admissionDate, BigDecimal roomPrice) {
        Room room = new Room();
        room.setId(1);
        room.setRoomNumber("P101");
        room.setBasePrice(roomPrice);

        Bed bed = new Bed();
        bed.setId(1);
        bed.setBedNumber("G101-1");
        bed.setRoom(room);

        Resident resident = new Resident();
        resident.setId(id);
        resident.setFullName("Trần Thị Kim Ánh");
        resident.setAdmissionDate(admissionDate);
        resident.setStatus(ResidentStatus.ACTIVE.name());
        resident.setBed(bed);
        return resident;
    }
}
