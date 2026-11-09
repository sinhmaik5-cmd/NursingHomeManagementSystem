package com.group6.nhms.dto.request;

import jakarta.validation.constraints.NotBlank;

public class BedUpdateRequest {
    @NotBlank(message = "Mã số giường không được để trống")
    private String bedNumber;


    @NotBlank(message = "Trạng thái giường không được để trống")
    private String status;


    public BedUpdateRequest() {
    }


    public BedUpdateRequest(String bedNumber, String status) {
        this.bedNumber = bedNumber;
        this.status = status;
    }


    public String getBedNumber() {
        return bedNumber;
    }


    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }

}
