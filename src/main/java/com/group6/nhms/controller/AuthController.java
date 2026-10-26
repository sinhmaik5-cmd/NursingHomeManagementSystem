package com.group6.nhms.controller;

import com.group6.nhms.dto.request.AccountCreateRequest;
import com.group6.nhms.dto.request.AuthRequest;
import com.group6.nhms.dto.response.AccountResponse;
import com.group6.nhms.dto.response.AuthResponse;
import com.group6.nhms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 1. Đăng ký tài khoản người dùng mới (Mã hóa mật khẩu với BCrypt)
    @PostMapping("/register")
    public ResponseEntity<AccountResponse> register(@Valid @RequestBody AccountCreateRequest request) {
        AccountResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Đăng nhập và xác thực tài khoản qua API
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // 3. Lấy thông tin tài khoản đang đăng nhập hiện tại
    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getCurrentUser(Authentication authentication) {
        AccountResponse response = authService.getCurrentUser(authentication);
        return ResponseEntity.ok(response);
    }
}
