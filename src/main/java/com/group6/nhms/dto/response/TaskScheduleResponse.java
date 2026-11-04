package com.group6.nhms.dto.response;

import java.time.LocalDate;

public class TaskScheduleResponse {

    private Integer id;
    private Integer staffId;
    private String staffName;
    private String role;
    private Integer roomId;
    private String roomNumber;
    private LocalDate workDate;
    private String shift;
    private String status;
    private String note;

    public TaskScheduleResponse() {
    }

    public TaskScheduleResponse(Integer id, Integer staffId, String staffName, String role, Integer roomId, String roomNumber, LocalDate workDate, String shift, String status, String note) {
        this.id = id;
        this.staffId = staffId;
        this.staffName = staffName;
        this.role = role;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.workDate = workDate;
        this.shift = shift;
        this.status = status;
        this.note = note;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
