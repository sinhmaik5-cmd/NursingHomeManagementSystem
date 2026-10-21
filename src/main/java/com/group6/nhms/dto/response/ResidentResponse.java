package com.group6.nhms.dto.response;

import java.time.LocalDate;

public class ResidentResponse {

    private Integer id;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String status; // OCCUPIED, DISCHARGED, DECEASED

    private Integer relativeId;
    private String relativeName;
    private String relativePhone;

    private Integer bedId;
    private String bedNumber;
    private String roomNumber;

    public ResidentResponse() {
    }

    public ResidentResponse(Integer id, String fullName, String gender, LocalDate dateOfBirth, String status, Integer relativeId, String relativeName, String relativePhone, Integer bedId, String bedNumber, String roomNumber) {
        this.id = id;
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
        this.relativeId = relativeId;
        this.relativeName = relativeName;
        this.relativePhone = relativePhone;
        this.bedId = bedId;
        this.bedNumber = bedNumber;
        this.roomNumber = roomNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRelativeId() {
        return relativeId;
    }

    public void setRelativeId(Integer relativeId) {
        this.relativeId = relativeId;
    }

    public String getRelativeName() {
        return relativeName;
    }

    public  void setRelativeName(String relativeName) {
        this.relativeName = relativeName;
    }

    public String getRelativePhone() {
        return relativePhone;
    }

    public void setRelativePhone(String relativePhone) {
        this.relativePhone = relativePhone;
    }

    public Integer getBedId() {
        return bedId;
    }

    public void setBedId(Integer bedId) {
        this.bedId = bedId;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}