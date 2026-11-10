package com.group6.nhms.controller;

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
    public String listStaff(
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        List<StaffResponse> staffList = staffService.getAllStaff();
        if (keyword != null && !keyword.isBlank()) {
            String lowerKw = keyword.trim().toLowerCase();
            staffList = staffList.stream()
                    .filter(s -> (s.getFullName() != null && s.getFullName().toLowerCase().contains(lowerKw))
                            || (s.getUsername() != null && s.getUsername().toLowerCase().contains(lowerKw))
                            || (s.getPhone() != null && s.getPhone().contains(lowerKw))
                            || (s.getSpecialization() != null && s.getSpecialization().toLowerCase().contains(lowerKw)))
                    .toList();
            model.addAttribute("keyword", keyword.trim());
        }
        populateStaffListModel(model, staffList);
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
            populateStaffListModel(model, staffService.getAllStaff());
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

    /**
     * Nạp danh sách nhân viên và thống kê số lượng theo từng vai trò vào Model.
     */
    private void populateStaffListModel(Model model, List<StaffResponse> staffList) {
        model.addAttribute("staffList", staffList);
        long doctorCount = staffList.stream().filter(s -> "DOCTOR".equalsIgnoreCase(s.getJobTitle())).count();
        long nurseCount = staffList.stream().filter(s -> "NURSE".equalsIgnoreCase(s.getJobTitle())).count();
        long receptionistCount = staffList.stream().filter(s -> "RECEPTIONIST".equalsIgnoreCase(s.getJobTitle())).count();
        model.addAttribute("doctorCount", doctorCount);
        model.addAttribute("nurseCount", nurseCount);
        model.addAttribute("receptionistCount", receptionistCount);
    }
}
