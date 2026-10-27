package com.group6.nhms.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        boolean isRelative = false;

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_RELATIVE".equalsIgnoreCase(role)) {
                isRelative = true;
                break;
            }
        }

        // Nếu là tài khoản Người thân -> Điều hướng thẳng đến Cổng Người Thân
        if (isRelative) {
            response.sendRedirect(request.getContextPath() + "/portal/dashboard");
        } else {
            // Các nhân viên nội bộ (CENTER_MANAGER, RECEPTIONIST, DOCTOR, NURSE) -> Vào Dashboard chính
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}
