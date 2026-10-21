package com.group6.nhms.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class HealthRecordCreateRequest {

    @NotNull(message = "ID Cư dân không được để trống")
    private Integer residentId;

    private String bloodPressure;
    private Integer heartRate;
    private Double bloodSugar;
    private Double temperature;

    private String diagnosis;
    private String notes;

    private List<MedicationItemRequest> medications;

    public HealthRecordCreateRequest() {
    }

    public HealthRecordCreateRequest(Integer residentId, String bloodPressure, Integer heartRate, Double bloodSugar, Double temperature, String diagnosis, String notes, List<MedicationItemRequest> medications) {
        this.residentId = residentId;
        this.bloodPressure = bloodPressure;
        this.heartRate = heartRate;
        this.bloodSugar = bloodSugar;
        this.temperature = temperature;
        this.diagnosis = diagnosis;
        this.notes = notes;
        this.medications = medications;
    }

    public Integer getResidentId() {
        return residentId;
    }

    public void setResidentId(Integer residentId) {
        this.residentId = residentId;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Double getBloodSugar() {
        return bloodSugar;
    }

    public void setBloodSugar(Double bloodSugar) {
        this.bloodSugar = bloodSugar;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<MedicationItemRequest> getMedications() {
        return medications;
    }

    public void setMedications(List<MedicationItemRequest> medications) {
        this.medications = medications;
    }

    public static class MedicationItemRequest {
        @NotNull(message = "ID Thuốc không được để trống")
        private Integer medicationId;

        @NotNull(message = "Số lượng thuốc phải lớn hơn 0")
        private Integer quantityRequested;

        private String  dosage;

        public MedicationItemRequest() {
        }

        public MedicationItemRequest(Integer medicationId, Integer quantityRequested, String dosage) {
            this.medicationId = medicationId;
            this.quantityRequested = quantityRequested;
            this.dosage = dosage;
        }

        public Integer getMedicationId() {
            return medicationId;
        }

        public void setMedicationId(Integer medicationId) {
            this.medicationId = medicationId;
        }

        public Integer getQuantityRequested() {
            return quantityRequested;
        }

        public void setQuantityRequested(Integer quantityRequested) {
            this.quantityRequested = quantityRequested;
        }

        public String getDosage() {
            return dosage;
        }

        public void setDosage(String dosage) {
            this.dosage = dosage;
        }
    }
}