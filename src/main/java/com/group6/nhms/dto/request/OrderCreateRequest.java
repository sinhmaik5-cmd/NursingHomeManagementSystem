package com.group6.nhms.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OrderCreateRequest {

    @NotNull(message = "ID Cư dân không được để trống")
    private Integer residentId;

    private String note;

    private List<Integer> serviceIds;

    public OrderCreateRequest() {
    }

    public OrderCreateRequest(Integer residentId, String note, List<Integer> serviceIds) {
        this.residentId = residentId;
        this.note = note;
        this.serviceIds = serviceIds;
    }

    public Integer getResidentId() {
        return residentId;
    }

    public void setResidentId(Integer residentId) {
        this.residentId = residentId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<Integer> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<Integer> serviceIds) {
        this.serviceIds = serviceIds;
    }
}