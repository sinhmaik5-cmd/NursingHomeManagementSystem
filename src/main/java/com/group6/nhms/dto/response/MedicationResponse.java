package com.group6.nhms.dto.response;


import java.math.BigDecimal;
import java.time.LocalDate;


public class MedicationResponse {
    private Integer id;
    private String medName;
    private Integer quantityInStock;
    private LocalDate expiryDate;
    private String supplier;
    private BigDecimal unitPrice;


    public MedicationResponse() {
    }


    public MedicationResponse(Integer id, String medName, Integer quantityInStock, LocalDate expiryDate, String supplier) {
        this.id = id;
        this.medName = medName;
        this.quantityInStock = quantityInStock;
        this.expiryDate = expiryDate;
        this.supplier = supplier;
    }


    public MedicationResponse(Integer id, String medName, Integer quantityInStock, LocalDate expiryDate, String supplier, BigDecimal unitPrice) {
        this.id = id;
        this.medName = medName;
        this.quantityInStock = quantityInStock;
        this.expiryDate = expiryDate;
        this.supplier = supplier;
        this.unitPrice = unitPrice;
    }


    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }


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


    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }


    public boolean isNearExpiry() {
        return expiryDate != null && !isExpired() && expiryDate.isBefore(LocalDate.now().plusDays(30));
    }


    public boolean isLowStock() {
        return quantityInStock != null && quantityInStock < 10 && quantityInStock > 0;
    }


    public boolean isOutOfStock() {
        return quantityInStock == null || quantityInStock <= 0;
    }
}

