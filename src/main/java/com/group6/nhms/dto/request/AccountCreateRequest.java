package com.group6.nhms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AccountCreateRequest {

    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
    private String password;

    @NotBlank(message = "Role không được để trống")
    private String role; // ADMIN, STAFF, RELATIVE

    @NotNull(message = "Reference ID không được để trống")
    private Integer  referenceId; // ID của Staff hoặc Relative

    public AccountCreateRequest() {
    }

    public AccountCreateRequest(String username, String password, String role, Integer referenceId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.referenceId = referenceId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }
}