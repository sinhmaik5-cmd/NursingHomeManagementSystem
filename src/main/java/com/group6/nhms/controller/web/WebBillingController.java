package com.group6.nhms.controller.web;

import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.service.OrderService;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.ResidentService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
    private final ResidentService residentService;
    private final RelativeService relativeService;

    public WebBillingController(
            OrderService orderService,
            ResidentService residentService,
            RelativeService relativeService
    ) {
        this.orderService = orderService;
        this.residentService = residentService;
        this.relativeService = relativeService;
    }

    // 1. Bảng chốt hóa đơn hàng tháng
    @GetMapping("/monthly")
    public String monthlyBilling(
            @RequestParam(required = false) String status,
            Model model
    ) {
        List<OrderResponse> orders = orderService.getAllOrders(status);
        List<ResidentResponse> activeResidents = residentService.getAllResidents(null, "ACTIVE");

        long unpaidCount = orderService.countUnpaidOrders();
        long paidCount = orderService.countPaidOrders();
        BigDecimal totalCollected = orderService.getTotalCollectedRevenue();

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
    public String orderDetail(@PathVariable Integer id, Authentication authentication, Model model) {
        OrderResponse order = orderService.getOrderById(id);
        ResidentResponse resident = null;

        boolean isRelative = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RELATIVE"));
        if (isRelative) {
            Relative currentRelative = relativeService.resolveCurrentRelative(authentication);
            if (currentRelative == null || order.getResidentId() == null) {
                throw new AccessDeniedException("Bạn không có quyền xem hóa đơn này");
            }

            resident = residentService.getResidentById(order.getResidentId());
            if (resident.getRelativeId() == null || !resident.getRelativeId().equals(currentRelative.getId())) {
                throw new AccessDeniedException("Bạn không có quyền xem hóa đơn này");
            }
        } else if (order.getResidentId() != null) {
            try {
                resident = residentService.getResidentById(order.getResidentId());
            } catch (Exception ignored) {
            }
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
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        // Bảo vệ phía server: Người thân không được phép xác nhận thanh toán
        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RELATIVE"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Người thân không có quyền xác nhận thanh toán.");
            return "redirect:/billing/orders/" + id;
        }

        try {
            orderService.confirmPayment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận thanh toán (PAID) thành công cho hóa đơn #" + id + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xác nhận thanh toán: " + e.getMessage());
        }
        return "redirect:/billing/orders/" + id;
    }
}
