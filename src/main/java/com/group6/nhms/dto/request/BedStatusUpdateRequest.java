package com.group6.nhms.dto.request;

import jakarta.validation.constraints.NotBlank;

public class BedStatusUpdateRequest {

    @NotBlank(message = "Trạng thái giường không được để trống")
    private String status;

    public BedStatusUpdateRequest() {
    }

    public BedStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
