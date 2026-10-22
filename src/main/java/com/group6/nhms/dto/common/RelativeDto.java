package com.group6.nhms.dto.common;

import jakarta.validation.constraints.NotBlank;

public class RelativeDto {
    private Integer id;
    
    @NotBlank(message = "Tên người bảo lãnh không được để trống")
    private String fullName;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;
    
    private String email;
    private String address;
    
    @NotBlank(message = "Mối quan hệ không được để trống (VD: Con trai, Vợ)")
    private String relationship;

    public RelativeDto() {
    }

    public RelativeDto(Integer id, String fullName, String phoneNumber, String email, String address, String relationship) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.relationship = relationship;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
}