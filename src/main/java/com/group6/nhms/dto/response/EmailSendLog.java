package com.group6.nhms.dto.response;


import java.math.BigDecimal;
import java.time.LocalDateTime;


public class EmailSendLog {
    private String id;
    private Integer relativeId;
    private String relativeName;
    private String recipientEmail;
    private String subject;
    private String contentHtml;
    private String contentText;
    private LocalDateTime sentAt;
    private String senderStaffName;
    private String status; // "SUCCESS", "FAILED"
    private int unpaidOrdersCount;
    private BigDecimal unpaidTotalAmount;
    private int pendingIncidentsCount;
    private String residentNames;


    public EmailSendLog() {
    }


    public EmailSendLog(String id, Integer relativeId, String relativeName, String recipientEmail,
                        String subject, String contentHtml, String contentText, LocalDateTime sentAt,
                        String senderStaffName, String status, int unpaidOrdersCount,
                        BigDecimal unpaidTotalAmount, int pendingIncidentsCount, String residentNames) {
        this.id = id;
        this.relativeId = relativeId;
        this.relativeName = relativeName;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.contentHtml = contentHtml;
        this.contentText = contentText;
        this.sentAt = sentAt;
        this.senderStaffName = senderStaffName;
        this.status = status;
        this.unpaidOrdersCount = unpaidOrdersCount;
        this.unpaidTotalAmount = unpaidTotalAmount;
        this.pendingIncidentsCount = pendingIncidentsCount;
        this.residentNames = residentNames;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }


    public Integer getRelativeId() { return relativeId; }
    public void setRelativeId(Integer relativeId) { this.relativeId = relativeId; }


    public String getRelativeName() { return relativeName; }
    public void setRelativeName(String relativeName) { this.relativeName = relativeName; }


    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }


    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }


    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }


    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }


    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }


    public String getSenderStaffName() { return senderStaffName; }
    public void setSenderStaffName(String senderStaffName) { this.senderStaffName = senderStaffName; }


    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


    public int getUnpaidOrdersCount() { return unpaidOrdersCount; }
    public void setUnpaidOrdersCount(int unpaidOrdersCount) { this.unpaidOrdersCount = unpaidOrdersCount; }


    public BigDecimal getUnpaidTotalAmount() { return unpaidTotalAmount; }
    public void setUnpaidTotalAmount(BigDecimal unpaidTotalAmount) { this.unpaidTotalAmount = unpaidTotalAmount; }


    public int getPendingIncidentsCount() { return pendingIncidentsCount; }
    public void setPendingIncidentsCount(int pendingIncidentsCount) { this.pendingIncidentsCount = pendingIncidentsCount; }


    public String getResidentNames() { return residentNames; }
    public void setResidentNames(String residentNames) { this.residentNames = residentNames; }
}

