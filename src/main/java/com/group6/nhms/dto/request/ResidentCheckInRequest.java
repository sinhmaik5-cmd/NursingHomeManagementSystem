package com.group6.nhms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ResidentCheckInRequest {

    // --- Thông tin Cư dân ---
    @NotBlank(message = "Họ tên cư dân không được để trống")
    private String fullName;

    private String gender;

    private LocalDate dateOfBirth;

    private String medicalHistory; // Tiền sử bệnh lý

    // --- Thông tin Phân giường ---
    @NotNull(message = "Vui lòng chọn giường cho cư dân")
    private Integer bedId;

    // --- Thông tin Người thân bảo lãnh ---
    // Nếu chọn người thân đã có sẵn trong hệ thống
    private Integer relativeId;

    // Nếu tạo người thân mới ngay khi Check-in
    private String relativeFullName;
    private String relativePhone;
    private String relativeAddress;
    private String relationship; // Mối quan hệ (Con, Cháu, Anh/Chị...)

    public ResidentCheckInRequest() {
    }

    public ResidentCheckInRequest(String fullName, String gender, LocalDate dateOfBirth, String medicalHistory, Integer bedId, Integer relativeId, String relativeFullName, String relativePhone, String relativeAddress, String relationship) {
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.medicalHistory = medicalHistory;
        this.bedId = bedId;
        this.relativeId = relativeId;
        this.relativeFullName = relativeFullName;
        this.relativePhone = relativePhone;
        this.relativeAddress = relativeAddress;
        this.relationship = relationship;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public Integer getBedId() { return bedId; }
    public void setBedId(Integer bedId) { this.bedId = bedId; }

    public Integer getRelativeId() { return relativeId; }
    public void setRelativeId(Integer relativeId) { this.relativeId = relativeId; }

    public String getRelativeFullName() { return relativeFullName; }
    public void setRelativeFullName(String relativeFullName) { this.relativeFullName = relativeFullName; }

    public String getRelativePhone() { return relativePhone; }
    public void setRelativePhone(String relativePhone) { this.relativePhone = relativePhone; }

    public String getRelativeAddress() { return relativeAddress; }
    public void setRelativeAddress(String relativeAddress) { this.relativeAddress = relativeAddress; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
}