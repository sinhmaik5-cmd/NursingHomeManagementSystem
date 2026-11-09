package com.group6.nhms.dto.request;

import jakarta.validation.constraints.*;

public class BedTransferRequest {@NotNull(message = "ID Cư dân không được để trống")
private Integer residentId;


    @NotNull(message = "Vui lòng chọn giường mới")
    private Integer newBedId;


    @NotBlank(message = "Lý do chuyển giường không được để trống")
    private String reason;


    public BedTransferRequest() {
    }


    public BedTransferRequest(Integer residentId, Integer newBedId, String reason) {
        this.residentId = residentId;
        this.newBedId = newBedId;
        this.reason = reason;
    }


    public Integer getResidentId() { return residentId; }
    public void setResidentId(Integer residentId) { this.residentId = residentId; }


    public Integer getNewBedId() { return newBedId; }
    public void setNewBedId(Integer newBedId) { this.newBedId = newBedId; }


    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

}
