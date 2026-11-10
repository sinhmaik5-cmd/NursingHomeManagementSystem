package com.group6.nhms.service;


import com.group6.nhms.dto.response.NotificationSummaryDto;
import org.springframework.security.core.Authentication;


public interface NotificationService {


    NotificationSummaryDto getNotificationsForUser(Authentication authentication);
}
