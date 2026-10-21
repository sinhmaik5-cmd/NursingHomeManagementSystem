package com.group6.nhms.dto.response;

public class AccountResponse {

    private Integer id;
    private String username;
    private String role;
    private Integer referenceId;
    private Boolean isActive;

    public AccountResponse() {
    }

    public AccountResponse(Integer id, String username, String role, Integer referenceId, Boolean isActive) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.referenceId = referenceId;
        this.isActive = isActive;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}