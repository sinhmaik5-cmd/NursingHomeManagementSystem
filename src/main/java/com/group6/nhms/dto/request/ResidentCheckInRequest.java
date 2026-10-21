package com.group6.nhms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ResidentCheckInRequest {

    @NotBlank(message = "Họ tên cư dân không được để trống")
    private String fullName;

    private String gender;

    private LocalDate dateOfBirth;

    @NotNull(message = "Vui lòng chọn ID người thân bảo lãnh")
    private Integer relativeId;

    @NotNull(message = "Vui lòng chọn ID giường trống")
    private Integer bedId;

    private String medicalHistory;

    public ResidentCheckInRequest() {
    }

    public ResidentCheckInRequest(String fullName, String gender, LocalDate dateOfBirth, Integer relativeId, Integer bedId, String medicalHistory) {
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.relativeId = relativeId;
        this.bedId = bedId;
        this.medicalHistory = medicalHistory;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String  getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getRelativeId() {
        return relativeId;
    }

    public void setRelativeId(Integer relativeId) {
        this.relativeId = relativeId;
    }

    public Integer getBedId() {
        return bedId;
    }

    public void setBedId(Integer bedId) {
        this.bedId = bedId;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
}