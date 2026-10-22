package com.group6.nhms.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class TaskScheduleCreateRequest {

    @NotNull(message = "ID Nhân viên không được để trống")
    private Integer staffId;

    private Integer roomId;

    @NotNull(message = "Ngày phân công không được để trống")
    private LocalDate workDate;

    @NotNull(message = "Ca trực không được để trống")
    private String shift; // MORNING, AFTERNOON, NIGHT

    private String note;

    public TaskScheduleCreateRequest() {
    }

    public TaskScheduleCreateRequest(Integer staffId, Integer roomId, LocalDate workDate, String shift, String note) {
        this.staffId = staffId;
        this.roomId = roomId;
        this.workDate = workDate;
        this.shift = shift;
        this.note = note;
    }

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
