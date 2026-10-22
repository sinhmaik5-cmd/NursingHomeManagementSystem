package com.group6.nhms.dto.request;

import jakarta.validation.constraints.NotBlank;

public class StaffCreateRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Chức danh không được để trống")
    private String jobTitle;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String specialization;

    public StaffCreateRequest() {
    }

    public StaffCreateRequest(String fullName, String jobTitle, String phone, String specialization) {
        this.fullName = fullName;
        this.jobTitle = jobTitle;
        this.phone = phone;
        this.specialization = specialization;
    }

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}