package com.group6.nhms.service.impl;

import com.group6.nhms.dto.response.EmailSendLog;
import com.group6.nhms.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final int MAX_LOGS = 100;

    private final ConcurrentLinkedDeque<EmailSendLog> recentLogs = new ConcurrentLinkedDeque<>();
    private final Map<Integer, LocalDateTime> lastSentMap = new ConcurrentHashMap<>();

    @Override
    public boolean sendNotificationEmail(
            Integer relativeId,
            String toEmail,
            String recipientName,
            String subject,
            String htmlContent,
            String textContent,
            String senderStaffName,
            int unpaidOrdersCount,
            BigDecimal unpaidAmount,
            int pendingIncidentsCount,
            String residentNames
    ) {
        LocalDateTime now = LocalDateTime.now();
        String logId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        log.info("================================================================================");
        log.info("[EMAIL DISPATCH - NHMS CARE] ID: #{} | To: {} <{}> | Sender: {}",
                logId, recipientName, toEmail, senderStaffName);
        log.info("Subject: {}", subject);
        log.info("Summary: Cư dân: {} | HĐ chưa trả: {} (Tổng: {} VNĐ) | Sự cố chưa xử lý: {}",
                residentNames, unpaidOrdersCount, unpaidAmount, pendingIncidentsCount);
        log.info("Content Preview:\n{}", textContent);
        log.info("================================================================================");

        EmailSendLog sendLog = new EmailSendLog(
                logId,
                relativeId,
                recipientName,
                toEmail,
                subject,
                htmlContent,
                textContent,
                now,
                senderStaffName,
                "SUCCESS",
                unpaidOrdersCount,
                unpaidAmount,
                pendingIncidentsCount,
                residentNames
        );

        recentLogs.addFirst(sendLog);
        while (recentLogs.size() > MAX_LOGS) {
            recentLogs.removeLast();
        }

        if (relativeId != null) {
            lastSentMap.put(relativeId, now);
        }

        return true;
    }

    @Override
    public List<EmailSendLog> getRecentLogs() {
        return new ArrayList<>(recentLogs);
    }

    @Override
    public LocalDateTime getLastSentTime(Integer relativeId) {
        return relativeId != null ? lastSentMap.get(relativeId) : null;
    }
}
