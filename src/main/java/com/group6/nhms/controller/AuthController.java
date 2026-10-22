package com.group6.nhms.controller;

import com.group6.nhms.dto.request.AccountCreateRequest;
import com.group6.nhms.dto.request.AuthRequest;
import com.group6.nhms.dto.response.AccountResponse;
import com.group6.nhms.dto.response.AuthResponse;
import com.group6.nhms.entity.Account;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ConflictException;
import com.group6.nhms.mapper.AccountMapper;
import com.group6.nhms.repository.AccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthController(
            AccountRepository accountRepository,
            AccountMapper accountMapper,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager
    ) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    // 1. Đăng ký tài khoản người dùng mới (Mã hóa mật khẩu với BCrypt)
    @PostMapping("/register")
    public ResponseEntity<AccountResponse> register(@Valid @RequestBody AccountCreateRequest request) {
        if (accountRepository.existsByUsername(request.getUsername().trim())) {
            throw new ConflictException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
        }

        Account account = new Account();
        account.setUsername(request.getUsername().trim());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setRole(request.getRole().trim().toUpperCase());
        account.setReferenceId(request.getReferenceId());
        account.setIsActive(true);

        Account saved = accountRepository.save(account);
        return new ResponseEntity<>(accountMapper.toResponse(saved), HttpStatus.CREATED);
    }

    // 2. Đăng nhập và xác thực tài khoản qua API
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Account account = accountRepository.findByUsername(request.getUsername().trim())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy thông tin tài khoản"));

            AuthResponse response = new AuthResponse();
            response.setUsername(account.getUsername());
            response.setRole(account.getRole());
            response.setReferenceId(account.getReferenceId());
            response.setTokenType("Session / Basic");
            response.setAccessToken("Authenticated-Session");

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException ex) {
            throw new BadRequestException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }
    }

    // 3. Lấy thông tin tài khoản đang đăng nhập hiện tại
    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("Chưa đăng nhập");
        }
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản"));
        return ResponseEntity.ok(accountMapper.toResponse(account));
    }
}
