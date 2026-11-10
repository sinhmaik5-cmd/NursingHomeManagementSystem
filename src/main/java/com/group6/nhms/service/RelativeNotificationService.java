package com.group6.nhms.service;


import com.group6.nhms.dto.response.BatchSendResult;
import com.group6.nhms.dto.response.EmailSendLog;
import com.group6.nhms.dto.response.RelativeNotificationSummary;


import java.util.List;


public interface RelativeNotificationService {


    List<RelativeNotificationSummary> getAllNotificationSummaries();


    RelativeNotificationSummary getNotificationSummaryForRelative(Integer relativeId);


    boolean sendNotificationToRelative(Integer relativeId, String senderStaffName);


    BatchSendResult sendNotificationsToAll(String senderStaffName);


    List<EmailSendLog> getRecentLogs();
}

