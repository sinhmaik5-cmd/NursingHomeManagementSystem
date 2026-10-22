package com.group6.nhms.dto.response;

import java.math.BigDecimal;

public class ServiceResponse {
    private Integer id;
    private String serviceName;
    private String description;
    private BigDecimal unitPrice;

    public ServiceResponse() {
    }

    public ServiceResponse(Integer id, String serviceName, String description, BigDecimal unitPrice) {
        this.id = id;
        this.serviceName = serviceName;
        this.description = description;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}