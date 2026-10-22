package com.group6.nhms.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ServiceCreateRequest {
    @NotBlank(message = "Tên dịch vụ không được để trống")
    private String serviceName;
    
    private String description;
    
    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá không được âm")
    private BigDecimal unitPrice;

    public ServiceCreateRequest() {
    }

    public ServiceCreateRequest(String serviceName, String description, BigDecimal unitPrice) {
        this.serviceName = serviceName;
        this.description = description;
        this.unitPrice = unitPrice;
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