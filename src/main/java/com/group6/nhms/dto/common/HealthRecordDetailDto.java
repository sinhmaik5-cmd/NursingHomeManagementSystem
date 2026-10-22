package com.group6.nhms.dto.common;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class HealthRecordDetailDto {
    private Integer id;
    
    @NotNull(message = "Vui lòng chọn thuốc")
    private Integer medicationId;
    
    private String medicationName;
    
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    private Integer quantity;
    
    private String dosage;
    private String frequency;

    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public HealthRecordDetailDto() {
    }

    public HealthRecordDetailDto(Integer id, Integer medicationId, String medicationName, Integer quantity, String dosage, String frequency) {
        this.id = id;
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.quantity = quantity;
        this.dosage = dosage;
        this.frequency = frequency;
    }

    public HealthRecordDetailDto(Integer id, Integer medicationId, String medicationName, Integer quantity, String dosage, String frequency, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.id = id;
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.quantity = quantity;
        this.dosage = dosage;
        this.frequency = frequency;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getMedicationId() { return medicationId; }
    public void setMedicationId(Integer medicationId) { this.medicationId = medicationId; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
