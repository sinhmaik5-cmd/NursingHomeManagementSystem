package com.group6.nhms.service.impl;

import com.group6.nhms.dto.request.AccountCreateRequest;
import com.group6.nhms.dto.request.AuthRequest;
import com.group6.nhms.dto.response.AccountResponse;
import com.group6.nhms.dto.response.AuthResponse;
import com.group6.nhms.entity.Account;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ConflictException;
import com.group6.nhms.mapper.AccountMapper;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(
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

    @Override
    @Transactional
    public AccountResponse register(AccountCreateRequest request) {
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
        return accountMapper.toResponse(saved);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
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

            return response;
        } catch (BadCredentialsException ex) {
            throw new BadRequestException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("Chưa đăng nhập");
        }
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản"));
        return accountMapper.toResponse(account);
    }
}
