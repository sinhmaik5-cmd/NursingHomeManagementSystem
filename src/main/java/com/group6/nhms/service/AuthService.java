package com.group6.nhms.service;

import com.group6.nhms.dto.request.AccountCreateRequest;
import com.group6.nhms.dto.request.AuthRequest;
import com.group6.nhms.dto.response.AccountResponse;
import com.group6.nhms.dto.response.AuthResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {

    AccountResponse register(AccountCreateRequest request);

    AuthResponse login(AuthRequest request);

    AccountResponse getCurrentUser(Authentication authentication);
}
