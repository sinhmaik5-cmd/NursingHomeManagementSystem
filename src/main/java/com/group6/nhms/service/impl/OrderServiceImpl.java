package com.group6.nhms.service.impl;


import com.group6.nhms.dto.response.MonthlyRevenueSummary;
import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.entity.HealthRecord;
import com.group6.nhms.entity.Order;
import com.group6.nhms.entity.OrderDetail;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Service;
import com.group6.nhms.enums.PaymentStatus;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.OrderMapper;
import com.group6.nhms.repository.HealthRecordDetailRepository;
import com.group6.nhms.repository.HealthRecordRepository;
import com.group6.nhms.repository.OrderDetailRepository;
import com.group6.nhms.repository.OrderRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.ServiceRepository;
import com.group6.nhms.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@org.springframework.stereotype.Service
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter LOG_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern ROOM_TRANSFER_LOG_PATTERN = Pattern.compile(
            "^\\[Đổi phòng (\\d{2}/\\d{2}/\\d{4})(?:\\s+\\d{2}:\\d{2})?\\]:\\s*Từ phòng .+ \\(([0-9.,]+) VNĐ/tháng\\) sang phòng .+ \\(([0-9.,]+) VNĐ/tháng\\).*"
    );
    private static final Pattern HOSPITAL_TRANSFER_LOG_PATTERN = Pattern.compile(
            "^\\[Chuyển viện khẩn cấp (\\d{2}/\\d{2}/\\d{4})(?:\\s+\\d{2}:\\d{2})?\\]:.*"
    );
    private static final Pattern HOSPITAL_RETURN_LOG_PATTERN = Pattern.compile(
            "^\\[Tái nhập viện (\\d{2}/\\d{2}/\\d{4})(?:\\s+\\d{2}:\\d{2})?\\]:.*"
    );


    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ResidentRepository residentRepository;
    private final HealthRecordDetailRepository healthRecordDetailRepository;
    private final ServiceRepository serviceRepository;
    private final OrderMapper orderMapper;
    private final HealthRecordRepository healthRecordRepository;


    @Autowired
    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            ResidentRepository residentRepository,
            HealthRecordDetailRepository healthRecordDetailRepository,
            ServiceRepository serviceRepository,
            OrderMapper orderMapper,
            @Autowired(required = false) HealthRecordRepository healthRecordRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.residentRepository = residentRepository;
        this.healthRecordDetailRepository = healthRecordDetailRepository;
        this.serviceRepository = serviceRepository;
        this.orderMapper = orderMapper;
        this.healthRecordRepository = healthRecordRepository;
    }

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            ResidentRepository residentRepository,
            HealthRecordDetailRepository healthRecordDetailRepository,
            ServiceRepository serviceRepository,
            OrderMapper orderMapper
    ) {
        this(
                orderRepository,
                orderDetailRepository,
                residentRepository,
                healthRecordDetailRepository,
                serviceRepository,
                orderMapper,
                null
        );
    }


    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByResident(Integer residentId) {
        return orderRepository.findByResidentIdOrderByIssueDateDesc(residentId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public Map<Integer, List<OrderResponse>> getOrdersByResidents(List<Integer> residentIds) {
        if (residentIds.isEmpty()) {
            return Map.of();
        }


        return orderRepository.findByResidentIdInOrderByIssueDateDesc(residentIds).stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.groupingBy(OrderResponse::getResidentId));
    }


    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getUnpaidOrders(Integer residentId) {
        return orderRepository.findByResidentIdAndPaymentStatus(residentId, PaymentStatus.PENDING.name())
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID " + id));
        return orderMapper.toResponse(order);
    }


    @Override
    @Transactional
    public OrderResponse confirmPayment(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID " + id));


        if (PaymentStatus.PAID.name().equals(order.getPaymentStatus())) {
            throw new BadRequestException("Hóa đơn này đã được thanh toán trước đó");
        }


        order.setPaymentStatus(PaymentStatus.PAID.name());
        order.setPaymentDate(LocalDateTime.now());


        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public void cancelUnpaidOrder(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID " + id));

        String status = order.getPaymentStatus();
        boolean isUnpaid = PaymentStatus.PENDING.name().equalsIgnoreCase(status)
                || "UNPAID".equalsIgnoreCase(status);
        if (!isUnpaid) {
            throw new BadRequestException("Chỉ được hủy hóa đơn chưa thanh toán (UNPAID/PENDING). Hóa đơn đã thanh toán không thể hủy.");
        }

        boolean hasSettledMonthlyFee = (order.getRoomPrice() != null && order.getRoomPrice().compareTo(BigDecimal.ZERO) > 0)
                || (order.getMedicationsPrice() != null && order.getMedicationsPrice().compareTo(BigDecimal.ZERO) > 0);
        List<OrderDetail> existingDetails = orderDetailRepository.findByOrderId(order.getId());
        if ((existingDetails == null || existingDetails.isEmpty())
                && order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            existingDetails = order.getOrderDetails();
        }
        if (hasSettledMonthlyFee && existingDetails != null && !existingDetails.isEmpty()) {
            preserveCareServicesInDraftOrder(order, existingDetails);
        }

        orderRepository.delete(order);
    }

    private void preserveCareServicesInDraftOrder(Order cancelledOrder, List<OrderDetail> existingDetails) {
        BigDecimal servicesTotal = existingDetails.stream()
                .map(detail -> detail.getAmount() != null ? detail.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order draftOrder = new Order();
        draftOrder.setResident(cancelledOrder.getResident());
        LocalDate fallbackDate = cancelledOrder.getIssueDate() != null ? cancelledOrder.getIssueDate() : LocalDate.now();
        draftOrder.setIssueDate(fallbackDate);
        draftOrder.setDueDate(cancelledOrder.getDueDate() != null ? cancelledOrder.getDueDate() : fallbackDate.plusDays(10));
        draftOrder.setPaymentStatus(PaymentStatus.PENDING.name());
        draftOrder.setRoomPrice(BigDecimal.ZERO);
        draftOrder.setMedicationsPrice(BigDecimal.ZERO);
        draftOrder.setServicesPrice(servicesTotal);
        draftOrder.setTotalAmount(servicesTotal);
        Order savedDraft = orderRepository.save(draftOrder);

        for (OrderDetail src : existingDetails) {
            OrderDetail copy = new OrderDetail();
            copy.setOrder(savedDraft);
            copy.setService(src.getService());
            copy.setItemName(src.getItemName());
            copy.setAmount(src.getAmount());
            OrderDetail savedCopy = orderDetailRepository.save(copy);
            if (savedDraft.getOrderDetails() != null) {
                savedDraft.getOrderDetails().add(savedCopy);
            }
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<MonthlyRevenueSummary> getMonthlyRevenueSummaries(List<OrderResponse> orders) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }

        java.util.Set<String> settledPeriods = OrderService.findSettledResidentPeriods(orders);
        Map<YearMonth, MonthlyRevenueSummary> summaryMap = new LinkedHashMap<>();
        orders.stream()
                .filter(o -> o.getIssueDate() != null)
                .filter(o -> !OrderService.isPreSettlementDraft(o, settledPeriods))
                .sorted(Comparator.comparing(OrderResponse::getIssueDate).reversed())
                .forEach(order -> {
                    YearMonth ym = YearMonth.from(order.getIssueDate());
                    MonthlyRevenueSummary summary = summaryMap.computeIfAbsent(ym, k -> {
                        MonthlyRevenueSummary s = new MonthlyRevenueSummary();
                        s.setYear(k.getYear());
                        s.setMonth(k.getMonthValue());
                        return s;
                    });

                    summary.setTotalOrders(summary.getTotalOrders() + 1);

                    BigDecimal room = order.getRoomPrice() != null ? order.getRoomPrice() : BigDecimal.ZERO;
                    BigDecimal services = order.getServicesPrice() != null ? order.getServicesPrice() : BigDecimal.ZERO;
                    BigDecimal meds = order.getMedicationsPrice() != null ? order.getMedicationsPrice() : BigDecimal.ZERO;
                    BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

                    summary.setTotalRoomRevenue(summary.getTotalRoomRevenue().add(room));
                    summary.setTotalServiceAndMedRevenue(summary.getTotalServiceAndMedRevenue().add(services).add(meds));
                    summary.setTotalBilledAmount(summary.getTotalBilledAmount().add(total));

                    if (PaymentStatus.PAID.name().equalsIgnoreCase(order.getPaymentStatus())) {
                        summary.setPaidOrders(summary.getPaidOrders() + 1);
                        summary.setCollectedRevenue(summary.getCollectedRevenue().add(total));
                    } else {
                        summary.setUnpaidOrders(summary.getUnpaidOrders() + 1);
                        summary.setPendingRevenue(summary.getPendingRevenue().add(total));
                    }
                });

        return List.copyOf(summaryMap.values());
    }


    @Override
    @Transactional
    public OrderResponse createCheckInFee(Integer residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cư dân ID " + residentId));


        if (!ResidentStatus.ACTIVE.name().equals(resident.getStatus())) {
            throw new BadRequestException("Cư dân không ở trạng thái lưu trú");
        }


        if (resident.getBed() == null || resident.getBed().getRoom() == null) {
            throw new BadRequestException("Cư dân chưa được gán giường/phòng");
        }


        BigDecimal roomPrice = resident.getBed().getRoom().getBasePrice();


        Order order = new Order();
        order.setResident(resident);
        order.setIssueDate(LocalDate.now());
        order.setDueDate(LocalDate.now().plusDays(7));
        order.setPaymentStatus(PaymentStatus.PENDING.name());
        order.setRoomPrice(roomPrice);
        order.setServicesPrice(BigDecimal.ZERO);
        order.setMedicationsPrice(BigDecimal.ZERO);
        order.setTotalAmount(roomPrice);


        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public OrderResponse createMonthlySettlement(Integer residentId, int year, int month) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cư dân ID " + residentId));


        if (!ResidentStatus.ACTIVE.name().equals(resident.getStatus())) {
            throw new BadRequestException("Chỉ có thể tạo hóa đơn tháng cho cư dân đang lưu trú");
        }


        if (resident.getBed() == null || resident.getBed().getRoom() == null) {
            throw new BadRequestException("Cư dân chưa được gán giường/phòng");
        }


        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        LocalDate admissionDate = resident.getAdmissionDate();

        if (admissionDate != null && admissionDate.isAfter(endDate)) {
            throw new BadRequestException("Không thể chốt hóa đơn tháng " + month + "/" + year
                    + " vì cư dân nhập viện từ ngày " + admissionDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        List<Order> existingOrders = orderRepository.findByResidentIdAndIssueDateBetween(residentId, startDate, endDate);
        Order draftPendingOrder = null;
        for (Order existing : existingOrders) {
            boolean alreadySettledRoom = existing.getRoomPrice() != null
                    && existing.getRoomPrice().compareTo(BigDecimal.ZERO) > 0;
            boolean isPaid = PaymentStatus.PAID.name().equalsIgnoreCase(existing.getPaymentStatus());
            if (isPaid || alreadySettledRoom) {
                throw new BadRequestException("Cư dân đã có hóa đơn trong tháng " + month + "/" + year);
            }
            if (isUnpaidStatus(existing.getPaymentStatus()) && draftPendingOrder == null) {
                draftPendingOrder = existing;
            }
        }

        LocalDate effectiveStartDate = (admissionDate != null && admissionDate.isAfter(startDate))
                ? admissionDate
                : startDate;
        int totalDaysInMonth = startDate.lengthOfMonth();

        BigDecimal baseRoomPrice = (resident.getBed() != null && resident.getBed().getRoom() != null
                && resident.getBed().getRoom().getBasePrice() != null)
                ? resident.getBed().getRoom().getBasePrice()
                : BigDecimal.ZERO;

        BigDecimal roomPrice = calculateMonthlyRoomPrice(
                residentId, baseRoomPrice, effectiveStartDate, endDate, totalDaysInMonth);


        BigDecimal servicesPrice = orderDetailRepository.sumServicesByResidentIdAndIssueDateBetween(
                residentId, startDate, endDate
        );
        if (servicesPrice == null) servicesPrice = BigDecimal.ZERO;


        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        BigDecimal medicationsPrice = healthRecordDetailRepository.sumTotalPriceByResidentIdAndRecordDateBetween(
                residentId, startInstant, endInstant
        );
        if (medicationsPrice == null) medicationsPrice = BigDecimal.ZERO;


        BigDecimal totalAmount = roomPrice.add(servicesPrice).add(medicationsPrice);

        LocalDate issueDate = resolveMonthlyIssueDate(startDate, endDate, effectiveStartDate, LocalDate.now());

        Order monthlyOrder = draftPendingOrder != null ? draftPendingOrder : new Order();
        monthlyOrder.setResident(resident);
        monthlyOrder.setIssueDate(issueDate);
        monthlyOrder.setDueDate(issueDate.plusDays(10));
        monthlyOrder.setPaymentStatus(PaymentStatus.PENDING.name());
        monthlyOrder.setRoomPrice(roomPrice);
        monthlyOrder.setServicesPrice(servicesPrice);
        monthlyOrder.setMedicationsPrice(medicationsPrice);
        monthlyOrder.setTotalAmount(totalAmount);


        Order saved = orderRepository.save(monthlyOrder);
        return orderMapper.toResponse(saved);
    }

    private boolean isUnpaidStatus(String status) {
        return PaymentStatus.PENDING.name().equalsIgnoreCase(status) || "UNPAID".equalsIgnoreCase(status);
    }

    private BigDecimal calculateMonthlyRoomPrice(
            Integer residentId,
            BigDecimal currentRoomBasePrice,
            LocalDate effectiveStartDate,
            LocalDate endDate,
            int totalDaysInMonth
    ) {
        if (currentRoomBasePrice == null || currentRoomBasePrice.compareTo(BigDecimal.ZERO) <= 0
                || effectiveStartDate.isAfter(endDate)) {
            return BigDecimal.ZERO;
        }
        long defaultBillableDays = ChronoUnit.DAYS.between(effectiveStartDate, endDate) + 1;
        if (healthRecordRepository == null) {
            return calculateProRataRoomPrice(currentRoomBasePrice, defaultBillableDays, totalDaysInMonth);
        }

        List<HealthRecord> records = healthRecordRepository.findByResidentIdOrderByRecordDateDesc(residentId);
        if (records == null || records.isEmpty()) {
            return calculateProRataRoomPrice(currentRoomBasePrice, defaultBillableDays, totalDaysInMonth);
        }

        List<HealthRecord> chronological = new ArrayList<>(records);
        chronological.sort(Comparator
                .comparing(HealthRecord::getRecordDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(r -> r.getId() != null ? r.getId() : 0));

        List<RoomTransferEvent> roomTransfers = new ArrayList<>();
        List<LocalDate[]> hospitalIntervals = new ArrayList<>();
        LocalDate pendingHospitalStart = null;

        for (HealthRecord record : chronological) {
            String note = record.getNotes();
            if (note == null || note.isBlank()) {
                continue;
            }
            Matcher roomMatcher = ROOM_TRANSFER_LOG_PATTERN.matcher(note.trim());
            if (roomMatcher.matches()) {
                LocalDate transferDate = parseLogDate(roomMatcher.group(1), record.getRecordDate());
                BigDecimal oldPrice = parseLogPrice(roomMatcher.group(2));
                BigDecimal newPrice = parseLogPrice(roomMatcher.group(3));
                if (transferDate != null && oldPrice != null && newPrice != null) {
                    roomTransfers.add(new RoomTransferEvent(transferDate, oldPrice, newPrice));
                }
                continue;
            }
            Matcher hospitalOutMatcher = HOSPITAL_TRANSFER_LOG_PATTERN.matcher(note.trim());
            if (hospitalOutMatcher.matches()) {
                if (pendingHospitalStart == null) {
                    pendingHospitalStart = parseLogDate(hospitalOutMatcher.group(1), record.getRecordDate());
                }
                continue;
            }
            Matcher hospitalReturnMatcher = HOSPITAL_RETURN_LOG_PATTERN.matcher(note.trim());
            if (hospitalReturnMatcher.matches() && pendingHospitalStart != null) {
                LocalDate returnDate = parseLogDate(hospitalReturnMatcher.group(1), record.getRecordDate());
                if (returnDate != null && !returnDate.isBefore(pendingHospitalStart)) {
                    hospitalIntervals.add(new LocalDate[]{pendingHospitalStart, returnDate});
                }
                pendingHospitalStart = null;
            }
        }

        if (roomTransfers.isEmpty() && hospitalIntervals.isEmpty()) {
            return calculateProRataRoomPrice(currentRoomBasePrice, defaultBillableDays, totalDaysInMonth);
        }

        Map<BigDecimal, Long> daysByRate = new LinkedHashMap<>();
        for (LocalDate day = effectiveStartDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            if (isHospitalizedOnDay(day, hospitalIntervals)) {
                continue;
            }
            BigDecimal rate = resolveRoomRateForDay(day, roomTransfers, currentRoomBasePrice);
            daysByRate.merge(rate, 1L, Long::sum);
        }

        if (daysByRate.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if (daysByRate.size() == 1) {
            Map.Entry<BigDecimal, Long> single = daysByRate.entrySet().iterator().next();
            return calculateProRataRoomPrice(single.getKey(), single.getValue(), totalDaysInMonth);
        }

        BigDecimal rawSum = BigDecimal.ZERO;
        BigDecimal maxRate = BigDecimal.ZERO;
        for (Map.Entry<BigDecimal, Long> entry : daysByRate.entrySet()) {
            BigDecimal rate = entry.getKey();
            long days = entry.getValue();
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0 || days <= 0) {
                continue;
            }
            if (rate.compareTo(maxRate) > 0) {
                maxRate = rate;
            }
            rawSum = rawSum.add(
                    rate.multiply(BigDecimal.valueOf(days))
                            .divide(BigDecimal.valueOf(totalDaysInMonth), 4, RoundingMode.HALF_UP)
            );
        }
        return roundVndAmount(rawSum, maxRate);
    }

    private boolean isHospitalizedOnDay(LocalDate day, List<LocalDate[]> hospitalIntervals) {
        for (LocalDate[] interval : hospitalIntervals) {
            if (!day.isBefore(interval[0]) && day.isBefore(interval[1])) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal resolveRoomRateForDay(
            LocalDate day,
            List<RoomTransferEvent> roomTransfers,
            BigDecimal currentRoomBasePrice
    ) {
        for (RoomTransferEvent transfer : roomTransfers) {
            if (transfer.transferDate().isAfter(day)) {
                return transfer.oldPrice();
            }
        }
        return currentRoomBasePrice;
    }

    private LocalDate parseLogDate(String dateText, Instant fallbackInstant) {
        try {
            return LocalDate.parse(dateText, LOG_DATE_FORMATTER);
        } catch (Exception ignored) {
            return fallbackInstant != null
                    ? fallbackInstant.atZone(ZoneId.systemDefault()).toLocalDate()
                    : null;
        }
    }

    private BigDecimal parseLogPrice(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw.replace(".", "").replace(",", "").trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private record RoomTransferEvent(LocalDate transferDate, BigDecimal oldPrice, BigDecimal newPrice) {
    }

    private BigDecimal calculateProRataRoomPrice(BigDecimal baseRoomPrice, long billableDays, int totalDaysInMonth) {
        if (baseRoomPrice == null || baseRoomPrice.compareTo(BigDecimal.ZERO) <= 0 || billableDays <= 0) {
            return BigDecimal.ZERO;
        }
        if (billableDays >= totalDaysInMonth) {
            return baseRoomPrice;
        }
        BigDecimal rawAmount = baseRoomPrice
                .multiply(BigDecimal.valueOf(billableDays))
                .divide(BigDecimal.valueOf(totalDaysInMonth), 2, RoundingMode.HALF_UP);
        return roundVndAmount(rawAmount, baseRoomPrice);
    }

    private BigDecimal roundVndAmount(BigDecimal rawAmount, BigDecimal referencePrice) {
        // Làm tròn đến hàng nghìn VNĐ khi đơn giá >= 1.000 VNĐ
        if (referencePrice.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            return rawAmount.divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(1000));
        }
        return rawAmount.setScale(0, RoundingMode.HALF_UP);
    }

    private LocalDate resolveMonthlyIssueDate(
            LocalDate startDate,
            LocalDate endDate,
            LocalDate effectiveStartDate,
            LocalDate today
    ) {
        if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
            return today.isBefore(effectiveStartDate) ? effectiveStartDate : today;
        }
        if (today.isBefore(startDate)) {
            return startDate;
        }
        return endDate;
    }


    @Override
    @Transactional
    public OrderResponse addServiceToResident(Integer residentId, Integer serviceId) {
        return addServiceToResident(residentId, serviceId, 1);
    }

    @Override
    @Transactional
    public OrderResponse addServiceToResident(Integer residentId, Integer serviceId, int quantity) {
        if (quantity < 1 || quantity > 30) {
            throw new BadRequestException("Số lượng dịch vụ phải từ 1 đến 30.");
        }

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cư dân ID " + residentId));


        if (!ResidentStatus.ACTIVE.name().equals(resident.getStatus())) {
            throw new BadRequestException("Cư dân không ở trạng thái lưu trú");
        }


        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ ID " + serviceId));


        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());


        List<Order> orders = orderRepository.findByResidentIdAndPaymentStatus(residentId, PaymentStatus.PENDING.name());


        Order targetOrder = null;
        for (Order o : orders) {
            if (o.getIssueDate() != null && !o.getIssueDate().isBefore(startOfMonth) && !o.getIssueDate().isAfter(endOfMonth)) {
                targetOrder = o;
                break;
            }
        }


        if (targetOrder == null) {
            targetOrder = new Order();
            targetOrder.setResident(resident);
            targetOrder.setIssueDate(today);
            targetOrder.setDueDate(today.plusDays(10));
            targetOrder.setPaymentStatus(PaymentStatus.PENDING.name());
            targetOrder.setRoomPrice(BigDecimal.ZERO);
            targetOrder.setServicesPrice(BigDecimal.ZERO);
            targetOrder.setMedicationsPrice(BigDecimal.ZERO);
            targetOrder.setTotalAmount(BigDecimal.ZERO);
            targetOrder = orderRepository.save(targetOrder);
        }


        BigDecimal unitPrice = service.getUnitPrice() != null ? service.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal detailAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        String itemName = quantity > 1
                ? service.getServiceName() + " (x" + quantity + ")"
                : service.getServiceName();

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(targetOrder);
        orderDetail.setService(service);
        orderDetail.setItemName(itemName);
        orderDetail.setAmount(detailAmount);
        OrderDetail savedDetail = orderDetailRepository.save(orderDetail);
        if (targetOrder.getOrderDetails() != null) {
            targetOrder.getOrderDetails().add(savedDetail);
        }


        BigDecimal currentServices = targetOrder.getServicesPrice() != null ? targetOrder.getServicesPrice() : BigDecimal.ZERO;
        BigDecimal newServices = currentServices.add(detailAmount);
        targetOrder.setServicesPrice(newServices);
        BigDecimal currentRoom = targetOrder.getRoomPrice() != null ? targetOrder.getRoomPrice() : BigDecimal.ZERO;
        BigDecimal currentMeds = targetOrder.getMedicationsPrice() != null ? targetOrder.getMedicationsPrice() : BigDecimal.ZERO;
        targetOrder.setTotalAmount(currentRoom.add(newServices).add(currentMeds));


        Order savedOrder = orderRepository.save(targetOrder);
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> getAllCareServices() {
        return serviceRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    @Transactional
    public Service createCareService(String serviceName, BigDecimal unitPrice, String description) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new BadRequestException("Tên dịch vụ không được để trống.");
        }
        String cleanName = serviceName.trim();
        if (serviceRepository.existsByServiceName(cleanName)) {
            throw new BadRequestException("Dịch vụ '" + cleanName + "' đã tồn tại trong danh mục.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Đơn giá dịch vụ phải lớn hơn 0.");
        }
        Service service = new Service();
        service.setServiceName(cleanName);
        service.setUnitPrice(unitPrice);
        service.setDescription(description != null && !description.isBlank() ? description.trim() : null);
        return serviceRepository.save(service);
    }

    @Override
    @Transactional
    public void removeServiceDetail(Integer orderDetailId) {
        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ phát sinh ID " + orderDetailId));
        Order order = detail.getOrder();
        if (order == null || !isUnpaidStatus(order.getPaymentStatus())) {
            throw new BadRequestException("Chỉ được gỡ dịch vụ khi hóa đơn chưa thanh toán (UNPAID/PENDING).");
        }

        BigDecimal detailAmount = detail.getAmount() != null ? detail.getAmount() : BigDecimal.ZERO;
        BigDecimal currentServices = order.getServicesPrice() != null ? order.getServicesPrice() : BigDecimal.ZERO;
        BigDecimal newServices = currentServices.subtract(detailAmount).max(BigDecimal.ZERO);
        BigDecimal currentRoom = order.getRoomPrice() != null ? order.getRoomPrice() : BigDecimal.ZERO;
        BigDecimal currentMeds = order.getMedicationsPrice() != null ? order.getMedicationsPrice() : BigDecimal.ZERO;

        if (order.getOrderDetails() != null) {
            order.getOrderDetails().removeIf(od -> od.getId() != null && od.getId().equals(orderDetailId));
        }
        orderDetailRepository.delete(detail);

        List<OrderDetail> remaining = orderDetailRepository.findByOrderId(order.getId()).stream()
                .filter(od -> od.getId() != null && !od.getId().equals(orderDetailId))
                .toList();

        if (remaining.isEmpty()
                && currentRoom.compareTo(BigDecimal.ZERO) == 0
                && currentMeds.compareTo(BigDecimal.ZERO) == 0) {
            orderRepository.delete(order);
        } else {
            order.setServicesPrice(newServices);
            order.setTotalAmount(currentRoom.add(newServices).add(currentMeds));
            orderRepository.save(order);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(String paymentStatus) {
        List<Order> orders;
        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            orders = orderRepository.findByPaymentStatus(paymentStatus.trim().toUpperCase());
        } else {
            orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "issueDate", "id"));
        }
        return orders.stream().map(orderMapper::toResponse).toList();
    }


    @Override
    @Transactional(readOnly = true)
    public long countUnpaidOrders() {
        return orderRepository.countByPaymentStatus(PaymentStatus.PENDING.name());
    }


    @Override
    @Transactional(readOnly = true)
    public long countPaidOrders() {
        return orderRepository.countByPaymentStatus(PaymentStatus.PAID.name());
    }


    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalCollectedRevenue() {
        return orderRepository.findByPaymentStatus(PaymentStatus.PAID.name())
                .stream()
                .map(Order::getTotalAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenueBetweenDates(LocalDate startDate, LocalDate endDate) {
        BigDecimal revenue = orderRepository.sumRevenueBetweenDates(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
}

