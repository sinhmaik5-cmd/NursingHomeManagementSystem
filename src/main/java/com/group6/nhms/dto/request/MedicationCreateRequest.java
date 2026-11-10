package com.group6.nhms.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;


public class MedicationCreateRequest {
    @NotBlank(message = "Tên thuốc không được để trống")
    private String medName;


    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer quantityInStock;


    @NotNull(message = "Hạn sử dụng không được để trống")
    @Future(message = "Hạn sử dụng phải là một ngày trong tương lai")
    private LocalDate expiryDate;


    private String supplier;


    @NotNull(message = "Đơn giá thuốc không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá thuốc không được âm")
    private BigDecimal unitPrice;


    public MedicationCreateRequest() {
    }


    public MedicationCreateRequest(String medName, Integer quantityInStock, LocalDate expiryDate, String supplier, BigDecimal unitPrice) {
        this.medName = medName;
        this.quantityInStock = quantityInStock;
        this.expiryDate = expiryDate;
        this.supplier = supplier;
        this.unitPrice = unitPrice;
    }


    // Getters and Setters
    public String getMedName() { return medName; }
    public void setMedName(String medName) { this.medName = medName; }


    public Integer getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(Integer quantityInStock) { this.quantityInStock = quantityInStock; }


    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }


    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }


    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}

