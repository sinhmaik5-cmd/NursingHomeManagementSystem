package com.group6.nhms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class HealthRecordResponse {

    private Integer id;
    private Integer residentId;
    private String residentName;
    private String staffName;
    private LocalDateTime recordDate;

    private String bloodPressure;
    private Integer heartRate;
    private Double bloodSugar;
    private Double temperature;
    private String diagnosis;

    private List<MedicationItemResponse> medications;

    public HealthRecordResponse() {
    }

    public HealthRecordResponse(Integer id, Integer residentId, String residentName, String staffName, LocalDateTime recordDate, String bloodPressure, Integer heartRate, Double bloodSugar, Double temperature, String diagnosis, List<MedicationItemResponse> medications) {
        this.id = id;
        this.residentId = residentId;
        this.residentName = residentName;
        this.staffName = staffName;
        this.recordDate = recordDate;
        this.bloodPressure = bloodPressure;
        this.heartRate = heartRate;
        this.bloodSugar = bloodSugar;
        this.temperature = temperature;
        this.diagnosis = diagnosis;
        this.medications = medications;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getResidentId() {
        return residentId;
    }

    public void setResidentId(Integer residentId) {
        this.residentId = residentId;
    }

    public String getResidentName() {
        return residentName;
    }

    public void setResidentName(String residentName) {
        this.residentName = residentName;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public LocalDateTime getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDateTime recordDate) {
        this.recordDate = recordDate;
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

    public List<MedicationItemResponse> getMedications() {
        return medications;
    }

    public void setMedications(List<MedicationItemResponse> medications) {
        this.medications = medications;
    }

    public static class MedicationItemResponse {
        private Integer medicationId;
        private String medicationName;
        private Integer quantity;
        private String dosage;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public MedicationItemResponse() {
        }

        public MedicationItemResponse(Integer medicationId, String medicationName, Integer quantity, String dosage) {
            this.medicationId = medicationId;
            this.medicationName = medicationName;
            this.quantity = quantity;
            this.dosage = dosage;
        }

        public MedicationItemResponse(Integer medicationId, String medicationName, Integer quantity, String dosage, BigDecimal unitPrice, BigDecimal totalPrice) {
            this.medicationId = medicationId;
            this.medicationName = medicationName;
            this.quantity = quantity;
            this.dosage = dosage;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }

        public Integer getMedicationId() {
            return medicationId;
        }

        public void setMedicationId(Integer medicationId) {
            this.medicationId = medicationId;
        }

        public String getMedicationName() {
            return medicationName;
        }

        public void setMedicationName(String medicationName) {
            this.medicationName = medicationName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getDosage() {
            return dosage;
        }

        public void setDosage(String dosage) {
            this.dosage = dosage;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }
}
