package com.group6.nhms.dto.request;


public class HospitalTransferRequest {


    private String hospitalName;
    private String emergencyReason;
    private String transportInfo;
    private String notes;


    public HospitalTransferRequest() {
    }


    public HospitalTransferRequest(String hospitalName, String emergencyReason, String transportInfo, String notes) {
        this.hospitalName = hospitalName;
        this.emergencyReason = emergencyReason;
        this.transportInfo = transportInfo;
        this.notes = notes;
    }


    public String getHospitalName() {
        return hospitalName;
    }


    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }


    public String getEmergencyReason() {
        return emergencyReason;
    }


    public void setEmergencyReason(String emergencyReason) {
        this.emergencyReason = emergencyReason;
    }


    public String getTransportInfo() {
        return transportInfo;
    }


    public void setTransportInfo(String transportInfo) {
        this.transportInfo = transportInfo;
    }


    public String getNotes() {
        return notes;
    }


    public void setNotes(String notes) {
        this.notes = notes;
    }
}

