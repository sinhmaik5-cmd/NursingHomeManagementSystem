package com.group6.nhms.security;

import com.group6.nhms.entity.Account;
import com.group6.nhms.repository.AccountRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với tên đăng nhập: " + username));

        if (Boolean.FALSE.equals(account.getIsActive())) {
            throw new DisabledException("Tài khoản '" + username + "' đã bị vô hiệu hóa");
        }

        String role = account.getRole();
        if (role == null || role.isBlank()) {
            role = "STAFF";
        }
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        return new User(
                account.getUsername(),
                account.getPasswordHash(),
                Boolean.TRUE.equals(account.getIsActive()),
                true,
                true,
                true,
                authorities
        );
    }
}
