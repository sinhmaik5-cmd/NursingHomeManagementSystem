package com.group6.nhms.controller;

import com.group6.nhms.dto.response.*;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.service.OrderService;
import com.group6.nhms.service.RelativeNotificationService;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.ResidentService;
import com.group6.nhms.service.StaffService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/billing")
public class WebBillingController {

    private final OrderService orderService;
    private final ResidentService residentService;
    private final RelativeService relativeService;
    private final RelativeNotificationService relativeNotificationService;
    private final StaffService staffService;

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0");

    public WebBillingController(
            OrderService orderService,
            ResidentService residentService,
            RelativeService relativeService
    ) {
        this(orderService, residentService, relativeService, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public WebBillingController(
            OrderService orderService,
            ResidentService residentService,
            RelativeService relativeService,
            RelativeNotificationService relativeNotificationService,
            StaffService staffService
    ) {
        this.orderService = orderService;
        this.residentService = residentService;
        this.relativeService = relativeService;
        this.relativeNotificationService = relativeNotificationService;
        this.staffService = staffService;
    }

    // 1. Bảng chốt hóa đơn hàng tháng (Lễ tân xử lý thu phí, Giám đốc giám sát doanh thu)
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

    // 2. Chốt hóa đơn viện phí tháng mới cho cư dân (Chỉ Lễ tân kiêm Thu ngân)
    @PostMapping("/generate")
    public String generateMonthlyBill(
            @RequestParam Integer residentId,
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_RECEPTIONIST"))) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Bạn không có thẩm quyền chốt hóa đơn viện phí. Nghiệp vụ này do Lễ tân / Thu ngân phụ trách.");
            return "redirect:/billing/monthly";
        }

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

    // 4. Xác nhận thanh toán hóa đơn (PAID - Chỉ Lễ tân kiêm Thu ngân)
    @PostMapping("/orders/{id}/pay")
    public String confirmPayment(
            @PathVariable Integer id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_RECEPTIONIST"))) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Chỉ Lễ tân kiêm Thu ngân mới có thẩm quyền xác nhận thu tiền viện phí.");
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

    // 5. Bảng điều khiển Gửi thông báo viện phí & sự cố cho thân nhân (Chỉ Lễ tân)
    @GetMapping("/notifications")
    public String relativeNotifications(
            Authentication authentication,
            Model model
    ) {
        List<RelativeNotificationSummary> summaries = relativeNotificationService.getAllNotificationSummaries();
        List<EmailSendLog> recentLogs = relativeNotificationService.getRecentLogs();

        long eligibleCount = summaries.stream().filter(s -> s.isHasPendingItems() && s.isHasEmail()).count();
        long missingEmailCount = summaries.stream().filter(s -> s.isHasPendingItems() && !s.isHasEmail()).count();
        BigDecimal totalUnpaidAll = summaries.stream()
                .map(RelativeNotificationSummary::getTotalUnpaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalPendingIncidents = summaries.stream()
                .mapToInt(RelativeNotificationSummary::getTotalPendingIncidentsCount)
                .sum();

        model.addAttribute("summaries", summaries);
        model.addAttribute("recentLogs", recentLogs);
        model.addAttribute("eligibleCount", eligibleCount);
        model.addAttribute("missingEmailCount", missingEmailCount);
        model.addAttribute("totalUnpaidAll", totalUnpaidAll);
        model.addAttribute("totalPendingIncidents", totalPendingIncidents);
        model.addAttribute("activeMenu", "relative-notifications");

        return "billing/notifications";
    }

    // 6. Gửi email thông báo cho 1 thân nhân cụ thể (Chỉ Lễ tân)
    @PostMapping("/notifications/send/{relativeId}")
    public String sendNotificationToRelative(
            @PathVariable Integer relativeId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String staffName = resolveStaffDisplayName(authentication);
            relativeNotificationService.sendNotificationToRelative(relativeId, staffName);
            RelativeNotificationSummary summary = relativeNotificationService.getNotificationSummaryForRelative(relativeId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã gửi email thông báo viện phí & sự cố đến thân nhân '" + summary.getRelativeName() +
                            "' (" + summary.getRelativeEmail() + ") thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể gửi email: " + e.getMessage());
        }
        return "redirect:/billing/notifications";
    }

    // 7. Gửi email thông báo 1 lần cho TẤT CẢ thân nhân có khoản chưa thanh toán hoặc sự cố tồn đọng (Chỉ Lễ tân)
    @PostMapping("/notifications/send-all")
    public String sendNotificationToAll(
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String staffName = resolveStaffDisplayName(authentication);
            BatchSendResult result = relativeNotificationService.sendNotificationsToAll(staffName);
            if (result.getSuccessCount() == 0 && result.getTotalEligible() == 0) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Hiện tại không có thân nhân nào có hóa đơn chưa thanh toán hoặc sự cố tồn đọng cần gửi thông báo.");
            } else {
                String emailsStr = String.join(", ", result.getRecipientEmails());
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã gửi email thành công cho " + result.getSuccessCount() + " thân nhân (" + emailsStr + ")! " +
                                "Tổng viện phí nhắc thu: " + (result.getTotalUnpaidReminded() != null ? CURRENCY_FORMAT.format(result.getTotalUnpaidReminded()) + " VNĐ" : "0 VNĐ") +
                                ", Tổng sự cố thông báo: " + result.getTotalIncidentsReported() + ".");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi gửi email hàng loạt: " + e.getMessage());
        }
        return "redirect:/billing/notifications";
    }

    private String resolveStaffDisplayName(Authentication authentication) {
        if (authentication != null) {
            try {
                Staff staff = staffService.resolveCurrentStaff(authentication);
                if (staff != null && staff.getFullName() != null) {
                    return staff.getFullName();
                }
            } catch (Exception ignored) {
            }
            return authentication.getName();
        }
        return "Lễ tân cơ sở";
    }
}
