package com.group6.nhms.controller;

import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Account;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.enums.RoleEnum;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.repository.RelativeRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.ResidentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class WebProfileController {

    private final AccountRepository accountRepository;
    private final StaffRepository staffRepository;
    private final RelativeRepository relativeRepository;
    private final ResidentService residentService;
    private final PasswordEncoder passwordEncoder;

    public WebProfileController(
            AccountRepository accountRepository,
            StaffRepository staffRepository,
            RelativeRepository relativeRepository,
            ResidentService residentService,
            PasswordEncoder passwordEncoder
    ) {
        this.accountRepository = accountRepository;
        this.staffRepository = staffRepository;
        this.relativeRepository = relativeRepository;
        this.residentService = residentService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Account account = accountRepository.findByUsername(username).orElse(null);
        if (account == null) {
            return "redirect:/login";
        }

        Staff staff = null;
        Relative relative = null;
        List<ResidentResponse> sponsoredResidents = List.of();

        boolean isRelative = account.getRole() != null &&
                (account.getRole().equalsIgnoreCase(RoleEnum.RELATIVE.name()) ||
                 account.getRole().equalsIgnoreCase("ROLE_" + RoleEnum.RELATIVE.name()));

        if (isRelative) {
            if (account.getReferenceId() != null) {
                relative = relativeRepository.findById(account.getReferenceId()).orElse(null);
                if (relative != null) {
                    sponsoredResidents = residentService.getResidentsByRelative(relative.getId());
                }
            }
        } else {
            if (account.getReferenceId() != null) {
                staff = staffRepository.findById(account.getReferenceId()).orElse(null);
            }
        }

        model.addAttribute("account", account);
        model.addAttribute("staff", staff);
        model.addAttribute("relative", relative);
        model.addAttribute("sponsoredResidents", sponsoredResidents);
        model.addAttribute("isRelative", isRelative);
        model.addAttribute("activeMenu", "profile");

        return "profile/index";
    }

    @PostMapping("/update-info")
    public String updateProfileInfo(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String citizenId,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String relationship,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Account account = accountRepository.findByUsername(authentication.getName()).orElse(null);
        if (account == null) {
            return "redirect:/login";
        }

        if (fullName == null || fullName.trim().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Họ và tên không được để trống!");
            return "redirect:/profile";
        }
        if (phone == null || phone.trim().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số điện thoại không được để trống!");
            return "redirect:/profile";
        }

        boolean isRelative = account.getRole() != null &&
                (account.getRole().equalsIgnoreCase(RoleEnum.RELATIVE.name()) ||
                 account.getRole().equalsIgnoreCase("ROLE_" + RoleEnum.RELATIVE.name()));

        if (isRelative) {
            if (account.getReferenceId() != null) {
                Relative relative = relativeRepository.findById(account.getReferenceId()).orElse(null);
                if (relative != null) {
                    relative.setFullName(fullName.trim());
                    relative.setPhoneNumber(phone.trim());
                    if (email != null) relative.setEmail(email.trim());
                    if (address != null) relative.setAddress(address.trim());
                    if (relationship != null) relative.setRelationship(relationship.trim());
                    relativeRepository.save(relative);
                    redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ người thân thành công!");
                }
            }
        } else {
            if (account.getReferenceId() != null) {
                Staff staff = staffRepository.findById(account.getReferenceId()).orElse(null);
                if (staff != null) {
                    staff.setFullName(fullName.trim());
                    staff.setPhone(phone.trim());
                    if (email != null) staff.setEmail(email.trim());
                    if (citizenId != null) staff.setCitizenId(citizenId.trim());
                    if (specialization != null) {
                        staff.setSpecialization(specialization.trim());
                    }
                    staffRepository.save(staff);
                    redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ nhân viên thành công!");
                }
            }
        }

        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Account account = accountRepository.findByUsername(authentication.getName()).orElse(null);
        if (account == null) {
            return "redirect:/login";
        }

        if (currentPassword == null || !passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không chính xác!");
            return "redirect:/profile";
        }

        if (newPassword == null || newPassword.trim().length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "redirect:/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xác nhận mật khẩu mới không trùng khớp!");
            return "redirect:/profile";
        }

        if (currentPassword.equals(newPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới không được trùng với mật khẩu hiện tại!");
            return "redirect:/profile";
        }

        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu tài khoản thành công!");
        return "redirect:/profile";
    }
}
