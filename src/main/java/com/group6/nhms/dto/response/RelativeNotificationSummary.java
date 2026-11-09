package com.group6.nhms.dto.response;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class RelativeNotificationSummary {
    private Integer relativeId;
    private String relativeName;
    private String relativePhone;
    private String relativeEmail;
    private String relationship;
    private List<ResidentPendingNotification> residents = new ArrayList<>();
    private BigDecimal totalUnpaidAmount = BigDecimal.ZERO;
    private int totalUnpaidOrdersCount = 0;
    private int totalPendingIncidentsCount = 0;
    private LocalDateTime lastSentTime;
    private String previewEmailSubject;
    private String previewEmailHtml;
    private String previewEmailText;


    public RelativeNotificationSummary() {
    }


    public RelativeNotificationSummary(Integer relativeId, String relativeName, String relativePhone,
                                       String relativeEmail, String relationship) {
        this.relativeId = relativeId;
        this.relativeName = relativeName;
        this.relativePhone = relativePhone;
        this.relativeEmail = relativeEmail;
        this.relationship = relationship;
    }


    public Integer getRelativeId() { return relativeId; }
    public void setRelativeId(Integer relativeId) { this.relativeId = relativeId; }


    public String getRelativeName() { return relativeName; }
    public void setRelativeName(String relativeName) { this.relativeName = relativeName; }


    public String getRelativePhone() { return relativePhone; }
    public void setRelativePhone(String relativePhone) { this.relativePhone = relativePhone; }


    public String getRelativeEmail() { return relativeEmail; }
    public void setRelativeEmail(String relativeEmail) { this.relativeEmail = relativeEmail; }


    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }


    public List<ResidentPendingNotification> getResidents() { return residents; }
    public void setResidents(List<ResidentPendingNotification> residents) {
        this.residents = residents != null ? residents : new ArrayList<>();
        recalculateTotals();
    }


    public void addResident(ResidentPendingNotification res) {
        if (this.residents == null) {
            this.residents = new ArrayList<>();
        }
        this.residents.add(res);
        recalculateTotals();
    }


    public void recalculateTotals() {
        this.totalUnpaidAmount = BigDecimal.ZERO;
        this.totalUnpaidOrdersCount = 0;
        this.totalPendingIncidentsCount = 0;


        if (this.residents != null) {
            for (ResidentPendingNotification res : this.residents) {
                if (res.getTotalUnpaidAmount() != null) {
                    this.totalUnpaidAmount = this.totalUnpaidAmount.add(res.getTotalUnpaidAmount());
                }
                if (res.getUnpaidOrders() != null) {
                    this.totalUnpaidOrdersCount += res.getUnpaidOrders().size();
                }
                if (res.getPendingIncidents() != null) {
                    this.totalPendingIncidentsCount += res.getPendingIncidents().size();
                }
            }
        }
    }


    public BigDecimal getTotalUnpaidAmount() { return totalUnpaidAmount; }
    public void setTotalUnpaidAmount(BigDecimal totalUnpaidAmount) { this.totalUnpaidAmount = totalUnpaidAmount; }


    public int getTotalUnpaidOrdersCount() { return totalUnpaidOrdersCount; }
    public void setTotalUnpaidOrdersCount(int totalUnpaidOrdersCount) { this.totalUnpaidOrdersCount = totalUnpaidOrdersCount; }


    public int getTotalPendingIncidentsCount() { return totalPendingIncidentsCount; }
    public void setTotalPendingIncidentsCount(int totalPendingIncidentsCount) { this.totalPendingIncidentsCount = totalPendingIncidentsCount; }


    public boolean isHasPendingItems() {
        return totalUnpaidOrdersCount > 0 || totalPendingIncidentsCount > 0;
    }


    public boolean isHasEmail() {
        return relativeEmail != null && !relativeEmail.trim().isBlank();
    }


    public LocalDateTime getLastSentTime() { return lastSentTime; }
    public void setLastSentTime(LocalDateTime lastSentTime) { this.lastSentTime = lastSentTime; }


    public String getPreviewEmailSubject() { return previewEmailSubject; }
    public void setPreviewEmailSubject(String previewEmailSubject) { this.previewEmailSubject = previewEmailSubject; }


    public String getPreviewEmailHtml() { return previewEmailHtml; }
    public void setPreviewEmailHtml(String previewEmailHtml) { this.previewEmailHtml = previewEmailHtml; }


    public String getPreviewEmailText() { return previewEmailText; }
    public void setPreviewEmailText(String previewEmailText) { this.previewEmailText = previewEmailText; }
}

