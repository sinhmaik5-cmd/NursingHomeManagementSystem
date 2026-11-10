package com. group6. nhms. service;

import com.group6.nhms.dto.response.EmailSendLog;

import java.math.BigDecimal;
import java. time. LocalDateTime;
import java.util.List;

public interface EmailService {

    boolean sendNotificationEmail (
            Integer relativeId,
            String toEmail,
            String recipientName,
            String subject,
            String htmlContent,
            String textContent
            String senderStaffName,
            int unpaidOrdersCount,
            BigDecimal unpaidAmount,
            int pendingIncidentsCount,
            String residentNames

    List<EmailSendLog> getRecentLogs();

    LocalDateTime getLastSentTime (Integer relativeId);
}