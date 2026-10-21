package com.group6.nhms.dto.response;

public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private String username;
    private String role; // ADMIN, STAFF, RELATIVE
    private Integer referenceId; // ID của Staff hoặc Relative tương ứng

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String tokenType, String username, String role, Integer referenceId) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.username = username;
        this.role = role;
        this.referenceId = referenceId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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