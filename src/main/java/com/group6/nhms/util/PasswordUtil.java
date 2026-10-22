package com.group6.nhms.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /**
     * Mã hóa mật khẩu thô sang chuỗi BCrypt Hash
     */
    public static String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Kiểm tra mật khẩu thô có khớp với chuỗi đã mã hóa trong DB không
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}