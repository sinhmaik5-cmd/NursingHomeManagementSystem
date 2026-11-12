package com.group6.nhms.service;

import com.group6.nhms.dto.response.MonthlyRevenueSummary;
import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.entity.Bed;
import com.group6.nhms.entity.HealthRecord;
import com.group6.nhms.entity.Order;
import com.group6.nhms.entity.OrderDetail;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Room;
import com.group6.nhms.entity.Service;
import com.group6.nhms.enums.PaymentStatus;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.mapper.OrderDetailMapper;
import com.group6.nhms.mapper.OrderMapper;
import com.group6.nhms.repository.HealthRecordDetailRepository;
import com.group6.nhms.repository.HealthRecordRepository;
import com.group6.nhms.repository.OrderDetailRepository;
import com.group6.nhms.repository.OrderRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.ServiceRepository;
import com.group6.nhms.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
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
    @Mock
    private HealthRecordRepository healthRecordRepository;

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
                orderMapper,
                healthRecordRepository
        );
    }

    @Test
    void createMonthlySettlement_proRatesFirstMonthWhenAdmittedMidMonth() {
        LocalDate admissionDate = LocalDate.of(2026, 11, 12);
        Resident resident = buildActiveResident(1, admissionDate, new BigDecimal("5000000"));

        when(residentRepository.findById(1)).thenReturn(Optional.of(resident));
        when(orderRepository.findByResidentIdAndIssueDateBetween(any(), any(), any())).thenReturn(List.of());
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
        when(orderRepository.findByResidentIdAndIssueDateBetween(any(), any(), any())).thenReturn(List.of());
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

    @Test
    void addServiceToResident_createsDraftOrderAndMergesWithMonthlySettlement() {
        LocalDate today = LocalDate.now();
        Resident resident = buildActiveResident(1, today.withDayOfMonth(1), new BigDecimal("5000000"));

        Service service = new Service();
        service.setId(3);
        service.setServiceName("Ngâm chân thảo dược & Massage");
        service.setUnitPrice(new BigDecimal("800000"));

        when(residentRepository.findById(1)).thenReturn(Optional.of(resident));
        when(serviceRepository.findById(3)).thenReturn(Optional.of(service));
        when(orderRepository.findByResidentIdAndPaymentStatus(1, PaymentStatus.PENDING.name()))
                .thenReturn(List.of());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            if (o.getId() == null) o.setId(21);
            return o;
        });
        when(orderDetailRepository.save(any(OrderDetail.class))).thenAnswer(inv -> {
            OrderDetail od = inv.getArgument(0);
            od.setId(99);
            return od;
        });

        OrderResponse draftRes = orderService.addServiceToResident(1, 3, 2);

        assertEquals(new BigDecimal("1600000"), draftRes.getServicesPrice());
        assertEquals(new BigDecimal("1600000"), draftRes.getTotalAmount());
        assertEquals("Chưa chốt phòng", draftRes.getRoomQuantityLabel());
        assertEquals(1, draftRes.getOrderDetails().size());
        assertEquals("Ngâm chân thảo dược & Massage (x2)", draftRes.getOrderDetails().get(0).getServiceName());

        // Now settle the month onto that draft PENDING order
        Order draftEntity = new Order();
        draftEntity.setId(21);
        draftEntity.setResident(resident);
        draftEntity.setIssueDate(today);
        draftEntity.setPaymentStatus(PaymentStatus.PENDING.name());
        draftEntity.setRoomPrice(BigDecimal.ZERO);
        draftEntity.setServicesPrice(new BigDecimal("1600000"));
        draftEntity.setMedicationsPrice(BigDecimal.ZERO);
        draftEntity.setTotalAmount(new BigDecimal("1600000"));

        when(orderRepository.findByResidentIdAndIssueDateBetween(any(), any(), any()))
                .thenReturn(List.of(draftEntity));
        when(orderDetailRepository.sumServicesByResidentIdAndIssueDateBetween(any(), any(), any()))
                .thenReturn(new BigDecimal("1600000"));
        when(healthRecordDetailRepository.sumTotalPriceByResidentIdAndRecordDateBetween(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        OrderResponse settled = orderService.createMonthlySettlement(1, today.getYear(), today.getMonthValue());
        assertEquals(21, settled.getId());
        assertEquals(new BigDecimal("5000000"), settled.getRoomPrice());
        assertEquals(new BigDecimal("1600000"), settled.getServicesPrice());
        assertEquals(new BigDecimal("6600000"), settled.getTotalAmount());
    }

    @Test
    void createMonthlySettlement_rejectsWhenMonthlyRoomAlreadySettled() {
        Resident resident = buildActiveResident(1, LocalDate.of(2026, 11, 1), new BigDecimal("5000000"));
        Order settledOrder = new Order();
        settledOrder.setId(30);
        settledOrder.setPaymentStatus(PaymentStatus.PENDING.name());
        settledOrder.setRoomPrice(new BigDecimal("5000000"));

        when(residentRepository.findById(1)).thenReturn(Optional.of(resident));
        when(orderRepository.findByResidentIdAndIssueDateBetween(any(), any(), any()))
                .thenReturn(List.of(settledOrder));

        assertThrows(BadRequestException.class, () -> orderService.createMonthlySettlement(1, 2026, 11));
    }

    @Test
    void removeServiceDetail_deletesDraftOrderWhenEmptyAndRejectsPaidOrder() {
        Order draftOrder = new Order();
        draftOrder.setId(40);
        draftOrder.setPaymentStatus(PaymentStatus.PENDING.name());
        draftOrder.setRoomPrice(BigDecimal.ZERO);
        draftOrder.setServicesPrice(new BigDecimal("800000"));
        draftOrder.setMedicationsPrice(BigDecimal.ZERO);
        draftOrder.setTotalAmount(new BigDecimal("800000"));

        OrderDetail detail = new OrderDetail();
        detail.setId(55);
        detail.setOrder(draftOrder);
        detail.setAmount(new BigDecimal("800000"));
        draftOrder.getOrderDetails().add(detail);

        when(orderDetailRepository.findById(55)).thenReturn(Optional.of(detail));
        when(orderDetailRepository.findByOrderId(40)).thenReturn(List.of());

        orderService.removeServiceDetail(55);

        verify(orderDetailRepository).delete(detail);
        verify(orderRepository).delete(draftOrder);

        Order paidOrder = new Order();
        paidOrder.setId(41);
        paidOrder.setPaymentStatus(PaymentStatus.PAID.name());
        OrderDetail paidDetail = new OrderDetail();
        paidDetail.setId(56);
        paidDetail.setOrder(paidOrder);
        paidDetail.setAmount(new BigDecimal("800000"));
        when(orderDetailRepository.findById(56)).thenReturn(Optional.of(paidDetail));

        assertThrows(BadRequestException.class, () -> orderService.removeServiceDetail(56));
    }

    @Test
    void createCareService_rejectsDuplicateAndInvalidPrice() {
        when(serviceRepository.existsByServiceName("Xông hơi")).thenReturn(true);
        assertThrows(BadRequestException.class,
                () -> orderService.createCareService("Xông hơi", new BigDecimal("500000"), null));
        assertThrows(BadRequestException.class,
                () -> orderService.createCareService("Dịch vụ mới", BigDecimal.ZERO, null));
    }

    @Test
    void cancelUnpaidOrder_preservesCareServicesInDraftOrderWhenMonthlySettled() {
        Resident resident = buildActiveResident(1, LocalDate.of(2026, 10, 1), new BigDecimal("5000000"));
        Order settledPending = new Order();
        settledPending.setId(88);
        settledPending.setResident(resident);
        settledPending.setIssueDate(LocalDate.of(2026, 11, 20));
        settledPending.setDueDate(LocalDate.of(2026, 11, 30));
        settledPending.setPaymentStatus(PaymentStatus.PENDING.name());
        settledPending.setRoomPrice(new BigDecimal("5000000"));
        settledPending.setServicesPrice(new BigDecimal("400000"));
        settledPending.setMedicationsPrice(new BigDecimal("100000"));
        settledPending.setTotalAmount(new BigDecimal("5500000"));

        Service careService = new Service();
        careService.setId(3);
        careService.setServiceName("Xoa bóp bấm huyệt phục hồi chức năng (theo buổi)");

        OrderDetail detail = new OrderDetail();
        detail.setId(901);
        detail.setOrder(settledPending);
        detail.setService(careService);
        detail.setItemName("Xoa bóp bấm huyệt phục hồi chức năng (theo buổi) (x2)");
        detail.setAmount(new BigDecimal("400000"));

        when(orderRepository.findById(88)).thenReturn(Optional.of(settledPending));
        when(orderDetailRepository.findByOrderId(88)).thenReturn(List.of(detail));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order saved = inv.getArgument(0);
            saved.setId(89);
            return saved;
        });
        when(orderDetailRepository.save(any(OrderDetail.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.cancelUnpaidOrder(88);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());
        Order preservedDraft = orderCaptor.getValue();
        assertEquals(BigDecimal.ZERO, preservedDraft.getRoomPrice());
        assertEquals(BigDecimal.ZERO, preservedDraft.getMedicationsPrice());
        assertEquals(new BigDecimal("400000"), preservedDraft.getServicesPrice());
        assertEquals(new BigDecimal("400000"), preservedDraft.getTotalAmount());
        assertEquals(PaymentStatus.PENDING.name(), preservedDraft.getPaymentStatus());

        ArgumentCaptor<OrderDetail> detailCaptor = ArgumentCaptor.forClass(OrderDetail.class);
        verify(orderDetailRepository).save(detailCaptor.capture());
        assertEquals("Xoa bóp bấm huyệt phục hồi chức năng (theo buổi) (x2)", detailCaptor.getValue().getItemName());
        assertEquals(new BigDecimal("400000"), detailCaptor.getValue().getAmount());
        assertEquals(89, detailCaptor.getValue().getOrder().getId());

        verify(orderRepository).delete(settledPending);
    }

    @Test
    void createMonthlySettlement_splitsRoomPriceAcrossMidMonthRoomTransfer() {
        Resident resident = buildActiveResident(1, LocalDate.of(2026, 10, 1), new BigDecimal("8000000"));
        HealthRecord transferLog = new HealthRecord();
        transferLog.setId(10);
        transferLog.setRecordDate(LocalDate.of(2026, 11, 16).atStartOfDay(ZoneId.systemDefault()).toInstant());
        transferLog.setNotes("[Đổi phòng 16/11/2026 09:30]: Từ phòng P101 (5000000 VNĐ/tháng) sang phòng P201 (8000000 VNĐ/tháng).");

        when(residentRepository.findById(1)).thenReturn(Optional.of(resident));
        when(orderRepository.findByResidentIdAndIssueDateBetween(any(), any(), any())).thenReturn(List.of());
        when(healthRecordRepository.findByResidentIdOrderByRecordDateDesc(1)).thenReturn(List.of(transferLog));
        when(orderDetailRepository.sumServicesByResidentIdAndIssueDateBetween(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(healthRecordDetailRepository.sumTotalPriceByResidentIdAndRecordDateBetween(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse res = orderService.createMonthlySettlement(1, 2026, 11);

        // 15 ngày @ 5.000.000 (01..15/11) + 15 ngày @ 8.000.000 (16..30/11) = 6.500.000 VNĐ
        assertEquals(new BigDecimal("6500000"), res.getRoomPrice());
        assertEquals(new BigDecimal("6500000"), res.getTotalAmount());
        assertEquals("Theo ngày thực tế (30 ngày/tháng)", res.getRoomQuantityLabel());
    }

    @Test
    void createMonthlySettlement_excludesExternalHospitalizationDays() {
        Resident resident = buildActiveResident(1, LocalDate.of(2026, 10, 1), new BigDecimal("5000000"));
        HealthRecord hospitalOut = new HealthRecord();
        hospitalOut.setId(20);
        hospitalOut.setRecordDate(LocalDate.of(2026, 11, 10).atStartOfDay(ZoneId.systemDefault()).toInstant());
        hospitalOut.setNotes("[Chuyển viện khẩn cấp 10/11/2026 08:00]: Đến Bệnh viện Bạch Mai. Lý do: Khó thở.");

        HealthRecord hospitalReturn = new HealthRecord();
        hospitalReturn.setId(21);
        hospitalReturn.setRecordDate(LocalDate.of(2026, 11, 15).atStartOfDay(ZoneId.systemDefault()).toInstant());
        hospitalReturn.setNotes("[Tái nhập viện 15/11/2026 14:00]: Đã xuất viện từ bệnh viện và trở lại viện dưỡng lão ổn định.");

        when(residentRepository.findById(1)).thenReturn(Optional.of(resident));
        when(orderRepository.findByResidentIdAndIssueDateBetween(any(), any(), any())).thenReturn(List.of());
        when(healthRecordRepository.findByResidentIdOrderByRecordDateDesc(1)).thenReturn(List.of(hospitalReturn, hospitalOut));
        when(orderDetailRepository.sumServicesByResidentIdAndIssueDateBetween(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(healthRecordDetailRepository.sumTotalPriceByResidentIdAndRecordDateBetween(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse res = orderService.createMonthlySettlement(1, 2026, 11);

        // Nằm viện ngoài từ 10..14/11 (5 ngày) -> tính tiền phòng 25/30 ngày @ 5.000.000 = 4.167.000 VNĐ
        assertEquals(new BigDecimal("4167000"), res.getRoomPrice());
        assertEquals("Theo ngày thực tế (30 ngày/tháng)", res.getRoomQuantityLabel());
    }

    @Test
    void createMonthlySettlement_usesHistoricalRoomRateWhenSettlingPreviousMonthAfterNextMonthTransfer() {
        Resident resident = buildActiveResident(1, LocalDate.of(2026, 10, 1), new BigDecimal("8000000"));
        HealthRecord decTransfer = new HealthRecord();
        decTransfer.setId(30);
        decTransfer.setRecordDate(LocalDate.of(2026, 12, 2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        decTransfer.setNotes("[Đổi phòng 02/12/2026 10:00]: Từ phòng P101 (5000000 VNĐ/tháng) sang phòng P201 (8000000 VNĐ/tháng).");

        when(residentRepository.findById(1)).thenReturn(Optional.of(resident));
        when(orderRepository.findByResidentIdAndIssueDateBetween(any(), any(), any())).thenReturn(List.of());
        when(healthRecordRepository.findByResidentIdOrderByRecordDateDesc(1)).thenReturn(List.of(decTransfer));
        when(orderDetailRepository.sumServicesByResidentIdAndIssueDateBetween(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(healthRecordDetailRepository.sumTotalPriceByResidentIdAndRecordDateBetween(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse res = orderService.createMonthlySettlement(1, 2026, 11);

        // Toàn bộ tháng 11 vẫn đang ở phòng cũ giá 5.000.000 trước khi chuyển sang 8.000.000 ngày 02/12
        assertEquals(new BigDecimal("5000000"), res.getRoomPrice());
    }

    @Test
    void getMonthlyRevenueSummaries_excludesPreSettlementServiceDrafts() {
        OrderResponse draft = new OrderResponse();
        draft.setId(201);
        draft.setResidentId(10);
        draft.setIssueDate(LocalDate.of(2026, 11, 15));
        draft.setRoomPrice(BigDecimal.ZERO);
        draft.setServicesPrice(new BigDecimal("400000"));
        draft.setMedicationsPrice(BigDecimal.ZERO);
        draft.setTotalAmount(new BigDecimal("400000"));
        draft.setPaymentStatus(PaymentStatus.PENDING.name());

        OrderResponse settled = new OrderResponse();
        settled.setId(202);
        settled.setResidentId(11);
        settled.setIssueDate(LocalDate.of(2026, 11, 20));
        settled.setRoomPrice(new BigDecimal("5000000"));
        settled.setServicesPrice(BigDecimal.ZERO);
        settled.setMedicationsPrice(BigDecimal.ZERO);
        settled.setTotalAmount(new BigDecimal("5000000"));
        settled.setPaymentStatus(PaymentStatus.PENDING.name());

        List<MonthlyRevenueSummary> summaries = orderService.getMonthlyRevenueSummaries(List.of(draft, settled));
        assertEquals(1, summaries.size());
        assertEquals(1, summaries.get(0).getTotalOrders());
        assertEquals(new BigDecimal("5000000"), summaries.get(0).getPendingRevenue());
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
