package com.group6.nhms.dto.response;

import java.math.BigDecimal;

public class MedicationResponse {

    private Integer id;
    private String name;
    private String unit; // Viên, Hộp, Chai
    private Integer stockQuantity;
    private BigDecimal unitPrice;

    public MedicationResponse() {
    }

    public MedicationResponse(Integer id, String name, String unit, Integer stockQuantity, BigDecimal unitPrice) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
        this.unitPrice = unitPrice;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}