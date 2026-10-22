package com.group6.nhms.dto.response;

public class StaffResponse {

    private Integer id;
    private String fullName;
    private String phone;
    private String position; // DOCTOR, NURSE, CARETAKER

    public StaffResponse() {
    }

    public StaffResponse(Integer id, String fullName, String phone, String position) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.position = position;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}