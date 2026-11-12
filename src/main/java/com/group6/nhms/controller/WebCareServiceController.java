package com.group6.nhms.controller;

import com.group6.nhms.dto.response.OrderDetailResponse;
import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Service;
import com.group6.nhms.service.OrderService;
import com.group6.nhms.service.ResidentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller dành cho Điều dưỡng quản lý và gắn dịch vụ chăm sóc phát sinh cho cư dân.
 */
@Controller
@RequestMapping("/health/services")
public class WebCareServiceController {

    private final OrderService orderService;
    private final ResidentService residentService;

    public WebCareServiceController(OrderService orderService, ResidentService residentService) {
        this.orderService = orderService;
        this.residentService = residentService;
    }

    @GetMapping
    public String showCareServices(
            @RequestParam(required = false) Integer residentId,
            Model model
    ) {
        List<ResidentResponse> activeResidents = residentService.getAllResidents(null, "ACTIVE");
        List<Service> catalogServices = orderService.getAllCareServices();

        ResidentResponse selectedResident = null;
        if (residentId != null) {
            selectedResident = activeResidents.stream()
                    .filter(r -> r.getId().equals(residentId))
                    .findFirst()
                    .orElse(null);
        }
        if (selectedResident == null && !activeResidents.isEmpty()) {
            selectedResident = activeResidents.get(0);
        }

        List<CareServiceItemView> assignedItems = new ArrayList<>();
        BigDecimal pendingServicesTotal = BigDecimal.ZERO;
        BigDecimal allServicesTotal = BigDecimal.ZERO;

        if (selectedResident != null) {
            List<OrderResponse> orders = orderService.getOrdersByResident(selectedResident.getId());
            for (OrderResponse order : orders) {
                boolean unpaid = "PENDING".equalsIgnoreCase(order.getPaymentStatus())
                        || "UNPAID".equalsIgnoreCase(order.getPaymentStatus());
                BigDecimal orderServiceAmount = order.getServicesPrice() != null ? order.getServicesPrice() : BigDecimal.ZERO;
                allServicesTotal = allServicesTotal.add(orderServiceAmount);
                if (unpaid) {
                    pendingServicesTotal = pendingServicesTotal.add(orderServiceAmount);
                }

                if (order.getOrderDetails() != null) {
                    for (OrderDetailResponse detail : order.getOrderDetails()) {
                        assignedItems.add(new CareServiceItemView(
                                detail.getId(),
                                order.getId(),
                                order.getIssueDate(),
                                order.getPaymentStatus(),
                                detail.getServiceName(),
                                detail.getAmount(),
                                unpaid
                        ));
                    }
                }
            }
        }

        model.addAttribute("activeResidents", activeResidents);
        model.addAttribute("selectedResident", selectedResident);
        model.addAttribute("catalogServices", catalogServices);
        model.addAttribute("assignedItems", assignedItems);
        model.addAttribute("pendingServicesTotal", pendingServicesTotal);
        model.addAttribute("allServicesTotal", allServicesTotal);
        model.addAttribute("activeMenu", "care-services");

        return "health/services";
    }

    @PostMapping("/assign")
    public String assignServiceToResident(
            @RequestParam Integer residentId,
            @RequestParam Integer serviceId,
            @RequestParam(defaultValue = "1") int quantity,
            RedirectAttributes redirectAttributes
    ) {
        try {
            OrderResponse updated = orderService.addServiceToResident(residentId, serviceId, quantity);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã gắn dịch vụ chăm sóc phát sinh cho cư dân " + updated.getResidentName()
                            + " (Hóa đơn #HĐ-" + updated.getId() + ") thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi gắn dịch vụ: " + e.getMessage());
        }
        return "redirect:/health/services?residentId=" + residentId;
    }

    @PostMapping("/catalog/create")
    public String createCareService(
            @RequestParam String serviceName,
            @RequestParam BigDecimal unitPrice,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer residentId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Service created = orderService.createCareService(serviceName, unitPrice, description);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã thêm dịch vụ mới '" + created.getServiceName() + "' vào danh mục!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thêm dịch vụ: " + e.getMessage());
        }
        return residentId != null
                ? "redirect:/health/services?residentId=" + residentId
                : "redirect:/health/services";
    }

    @PostMapping("/details/{detailId}/remove")
    public String removeServiceDetail(
            @PathVariable Integer detailId,
            @RequestParam(required = false) Integer residentId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.removeServiceDetail(detailId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã gỡ dịch vụ phát sinh khỏi hóa đơn chưa thanh toán!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi gỡ dịch vụ: " + e.getMessage());
        }
        return residentId != null
                ? "redirect:/health/services?residentId=" + residentId
                : "redirect:/health/services";
    }

    public record CareServiceItemView(
            Integer detailId,
            Integer orderId,
            LocalDate issueDate,
            String paymentStatus,
            String serviceName,
            BigDecimal amount,
            boolean unpaid
    ) {
    }
}
