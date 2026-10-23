package com.group6.nhms.controller.web;

import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Order;
import com.group6.nhms.enums.PaymentStatus;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.mapper.OrderMapper;
import com.group6.nhms.mapper.ResidentMapper;
import com.group6.nhms.repository.OrderRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.service.OrderService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/billing")
public class WebBillingController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ResidentRepository residentRepository;
    private final OrderMapper orderMapper;
    private final ResidentMapper residentMapper;

    public WebBillingController(
            OrderService orderService,
            OrderRepository orderRepository,
            ResidentRepository residentRepository,
            OrderMapper orderMapper,
            ResidentMapper residentMapper
    ) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.residentRepository = residentRepository;
        this.orderMapper = orderMapper;
        this.residentMapper = residentMapper;
    }

    // 1. Bảng chốt hóa đơn hàng tháng
    @GetMapping("/monthly")
    public String monthlyBilling(
            @RequestParam(required = false) String status,
            Model model
    ) {
        List<Order> ordersList;
        if (status != null && !status.trim().isEmpty()) {
            ordersList = orderRepository.findByPaymentStatus(status.trim().toUpperCase());
        } else {
            ordersList = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "issueDate", "id"));
        }

        List<OrderResponse> orders = ordersList.stream()
                .map(orderMapper::toResponse)
                .toList();

        List<ResidentResponse> activeResidents = residentRepository.findByStatus(ResidentStatus.ACTIVE.name())
                .stream()
                .map(residentMapper::toResponse)
                .toList();

        long unpaidCount = orderRepository.countByPaymentStatus(PaymentStatus.PENDING.name());
        long paidCount = orderRepository.countByPaymentStatus(PaymentStatus.PAID.name());

        BigDecimal totalCollected = ordersList.stream()
                .filter(o -> PaymentStatus.PAID.name().equalsIgnoreCase(o.getPaymentStatus()))
                .map(Order::getTotalAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate now = LocalDate.now();

        model.addAttribute("orders", orders);
        model.addAttribute("activeResidents", activeResidents);
        model.addAttribute("unpaidCount", unpaidCount);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("totalCollected", totalCollected);
        model.addAttribute("currentMonth", now.getMonthValue());
        model.addAttribute("currentYear", now.getYear());
        model.addAttribute("filterStatus", status);
        model.addAttribute("activeMenu", "billing-monthly");

        return "billing/monthly";
    }

    // 2. Chốt hóa đơn viện phí tháng mới cho cư dân
    @PostMapping("/generate")
    public String generateMonthlyBill(
            @RequestParam Integer residentId,
            @RequestParam int year,
            @RequestParam int month,
            RedirectAttributes redirectAttributes
    ) {
        try {
            OrderResponse res = orderService.createMonthlySettlement(residentId, year, month);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã chốt hóa đơn tháng " + month + "/" + year + " cho cư dân " + res.getResidentName() + " (Tổng tiền: " + res.getTotalAmount() + " VNĐ)!");
            return "redirect:/billing/orders/" + res.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể chốt hóa đơn tháng: " + e.getMessage());
            return "redirect:/billing/monthly";
        }
    }

    // 3. Chi tiết hóa đơn
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        OrderResponse order = orderService.getOrderById(id);
        ResidentResponse resident = null;
        if (order.getResidentId() != null) {
            resident = residentRepository.findById(order.getResidentId())
                    .map(residentMapper::toResponse)
                    .orElse(null);
        }

        model.addAttribute("order", order);
        model.addAttribute("resident", resident);
        model.addAttribute("activeMenu", "billing-monthly");

        return "billing/order-detail";
    }

    // 4. Xác nhận thanh toán hóa đơn (PAID)
    @PostMapping("/orders/{id}/pay")
    public String confirmPayment(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.confirmPayment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận thanh toán (PAID) thành công cho hóa đơn #" + id + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xác nhận thanh toán: " + e.getMessage());
        }
        return "redirect:/billing/orders/" + id;
    }
}
