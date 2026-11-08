package com.group6.nhms.controller;

import com.group6.nhms.dto.response.NotificationSummaryDto;
import com.group6.nhms.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalNotificationAdvice {

    private final NotificationService notificationService;

    public GlobalNotificationAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("notificationData")
    public NotificationSummaryDto populateNotificationData(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new NotificationSummaryDto(false, 0, List.of());
        }
        try {
            return notificationService.getNotificationsForUser(authentication);
        } catch (Exception e) {
            return new NotificationSummaryDto(false, 0, List.of());
        }
    }
}
