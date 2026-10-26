package com.group6.nhms.controller.web;

import com.group6.nhms.dto.request.StaffAccountCreateRequest;
import com.group6.nhms.dto.response.StaffResponse;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/management/staff")
public class WebStaffManagementController {

    private final StaffService staffService;

    public WebStaffManagementController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public String listStaff(Model model) {
        List<StaffResponse> staffList = staffService.getAllStaff();
        model.addAttribute("staffList", staffList);
        if (!model.containsAttribute("createRequest")) {
            model.addAttribute("createRequest", new StaffAccountCreateRequest());
        }
        model.addAttribute("activeMenu", "staff-management");
        return "management/staff-list";
    }

    @PostMapping("/create")
    public String createStaff(
            @Valid @ModelAttribute("createRequest") StaffAccountCreateRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("staffList", staffService.getAllStaff());
            model.addAttribute("activeMenu", "staff-management");
            model.addAttribute("showCreateModal", true);
            return "management/staff-list";
        }

        try {
            Staff staff = staffService.createStaffWithAccount(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm nhân viên " + staff.getFullName() + " (" + staff.getJobTitle() + ") và cấp tài khoản @" + request.getUsername() + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/management/staff";
    }

    @PostMapping("/{staffId}/toggle-status")
    public String toggleStatus(
            @PathVariable Integer staffId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            staffService.toggleStaffAccountStatus(staffId, authentication);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/management/staff";
    }

    @PostMapping("/{staffId}/reset-password")
    public String resetPassword(
            @PathVariable Integer staffId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            staffService.resetStaffPassword(staffId, "123456");
            redirectAttributes.addFlashAttribute("successMessage", "Đã đặt lại mật khẩu nhân viên về mặc định (123456) thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/management/staff";
    }
}
