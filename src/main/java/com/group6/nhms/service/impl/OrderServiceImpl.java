package com.group6.nhms.service.impl;

import com.group6.nhms.dto.response.OrderResponse;
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
import com.group6.nhms.repository.OrderDetailRepository;
import com.group6.nhms.repository.OrderRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.ServiceRepository;
import com.group6.nhms.service.OrderService;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@org.springframework.stereotype.Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ResidentRepository residentRepository;
    private final HealthRecordDetailRepository healthRecordDetailRepository;
    private final ServiceRepository serviceRepository;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            ResidentRepository residentRepository,
            HealthRecordDetailRepository healthRecordDetailRepository,
            ServiceRepository serviceRepository,
            OrderMapper orderMapper
    ) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.residentRepository = residentRepository;
        this.healthRecordDetailRepository = healthRecordDetailRepository;
        this.serviceRepository = serviceRepository;
        this.orderMapper = orderMapper;
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

        if (orderRepository.existsByResidentIdAndIssueDateBetween(residentId, startDate, endDate)) {
            throw new BadRequestException("Cư dân đã có hóa đơn trong tháng " + month + "/" + year);
        }

        BigDecimal roomPrice = (resident.getBed() != null && resident.getBed().getRoom() != null)
                ? resident.getBed().getRoom().getBasePrice()
                : BigDecimal.ZERO;

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

        Order monthlyOrder = new Order();
        monthlyOrder.setResident(resident);
        monthlyOrder.setIssueDate(LocalDate.now());
        monthlyOrder.setDueDate(LocalDate.now().plusDays(10));
        monthlyOrder.setPaymentStatus(PaymentStatus.PENDING.name());
        monthlyOrder.setRoomPrice(roomPrice);
        monthlyOrder.setServicesPrice(servicesPrice);
        monthlyOrder.setMedicationsPrice(medicationsPrice);
        monthlyOrder.setTotalAmount(totalAmount);

        Order saved = orderRepository.save(monthlyOrder);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse addServiceToResident(Integer residentId, Integer serviceId) {
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
            targetOrder.setDueDate(today.plusDays(7));
            targetOrder.setPaymentStatus(PaymentStatus.PENDING.name());
            targetOrder.setRoomPrice(BigDecimal.ZERO);
            targetOrder.setServicesPrice(BigDecimal.ZERO);
            targetOrder.setMedicationsPrice(BigDecimal.ZERO);
            targetOrder.setTotalAmount(BigDecimal.ZERO);
            targetOrder = orderRepository.save(targetOrder);
        }

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(targetOrder);
        orderDetail.setService(service);
        orderDetail.setAmount(service.getUnitPrice());
        orderDetailRepository.save(orderDetail);

        BigDecimal currentServices = targetOrder.getServicesPrice() != null ? targetOrder.getServicesPrice() : BigDecimal.ZERO;
        BigDecimal newServices = currentServices.add(service.getUnitPrice() != null ? service.getUnitPrice() : BigDecimal.ZERO);
        targetOrder.setServicesPrice(newServices);
        BigDecimal currentRoom = targetOrder.getRoomPrice() != null ? targetOrder.getRoomPrice() : BigDecimal.ZERO;
        BigDecimal currentMeds = targetOrder.getMedicationsPrice() != null ? targetOrder.getMedicationsPrice() : BigDecimal.ZERO;
        targetOrder.setTotalAmount(currentRoom.add(newServices).add(currentMeds));

        Order savedOrder = orderRepository.save(targetOrder);
        return orderMapper.toResponse(savedOrder);
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
