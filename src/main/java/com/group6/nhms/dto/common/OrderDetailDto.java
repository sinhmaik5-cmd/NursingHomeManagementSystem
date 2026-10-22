package com.group6.nhms.dto.common;

import java.math.BigDecimal;

public class OrderDetailDto {
    private Integer id;
    private Integer serviceId;
    private String serviceName;
    private BigDecimal amount;

    public OrderDetailDto() {
    }

    public OrderDetailDto(Integer id, Integer serviceId, String serviceName, BigDecimal amount) {
        this.id = id;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.amount = amount;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}