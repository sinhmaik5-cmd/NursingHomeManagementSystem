package com.group6.nhms.controller;

import com.group6.nhms.entity.Account;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.repository.RelativeRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.StaffService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUserAdvice {

    private final StaffService staffService;
    private final RelativeService relativeService;
    private final AccountRepository accountRepository;
    private final StaffRepository staffRepository;
    private final RelativeRepository relativeRepository;

    public GlobalUserAdvice(
            StaffService staffService,
            RelativeService relativeService,
            AccountRepository accountRepository,
            StaffRepository staffRepository,
            RelativeRepository relativeRepository
    ) {
        this.staffService = staffService;
        this.relativeService = relativeService;
        this.accountRepository = accountRepository;
        this.staffRepository = staffRepository;
        this.relativeRepository = relativeRepository;
    }

    @ModelAttribute("currentUserFullName")
    public String populateCurrentUserFullName(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "";
        }

        try {
            // 1. Try resolving via StaffService
            Staff staff = staffService.resolveCurrentStaff(authentication);
            if (staff != null && staff.getFullName() != null && !staff.getFullName().isBlank()) {
                return staff.getFullName();
            }

            // 2. Try resolving via RelativeService
            Relative relative = relativeService.resolveCurrentRelative(authentication);
            if (relative != null && relative.getFullName() != null && !relative.getFullName().isBlank()) {
                return relative.getFullName();
            }

            // 3. Fallback: check Account directly by username
            String username = authentication.getName();
            Account account = accountRepository.findByUsername(username).orElse(null);
            if (account != null && account.getReferenceId() != null) {
                if ("RELATIVE".equalsIgnoreCase(account.getRole()) || "ROLE_RELATIVE".equalsIgnoreCase(account.getRole())) {
                    Relative rel = relativeRepository.findById(account.getReferenceId()).orElse(null);
                    if (rel != null && rel.getFullName() != null && !rel.getFullName().isBlank()) {
                        return rel.getFullName();
                    }
                } else {
                    Staff st = staffRepository.findById(account.getReferenceId()).orElse(null);
                    if (st != null && st.getFullName() != null && !st.getFullName().isBlank()) {
                        return st.getFullName();
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return authentication.getName();
    }

    @ModelAttribute("currentUserRoleLabel")
    public String populateCurrentUserRoleLabel(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "";
        }

        String authority = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        return switch (authority.replace("ROLE_", "").toUpperCase()) {
            case "CENTER_MANAGER" -> "Giám đốc cơ sở";
            case "RECEPTIONIST" -> "Lễ tân & Thu phí";
            case "DOCTOR" -> "Bác sĩ chuyên khoa";
            case "NURSE" -> "Điều dưỡng viên";
            case "RELATIVE" -> "Người thân cư dân";
            default -> authority.replace("ROLE_", "");
        };
    }
}
