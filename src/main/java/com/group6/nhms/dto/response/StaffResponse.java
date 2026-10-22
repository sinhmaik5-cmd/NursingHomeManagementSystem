package com.group6.nhms.dto.response;

public class StaffResponse {
    private Integer id;
    private String fullName;
    private String jobTitle;
    private String phone;
    private String specialization;

    public StaffResponse() {
    }

    public StaffResponse(Integer id, String fullName, String jobTitle, String phone, String specialization) {
        this.id = id;
        this.fullName = fullName;
        this.jobTitle = jobTitle;
        this.phone = phone;
        this.specialization = specialization;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}