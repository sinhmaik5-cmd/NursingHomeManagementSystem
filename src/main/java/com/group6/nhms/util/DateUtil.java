package com.group6.nhms.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static final String DATE_FORMAT_VN = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT_VN = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_VN);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_VN);
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_ISO);

    private DateUtil() {
        // Class tiện ích không khởi tạo instance
    }

    /**
     * Format LocalDate sang chuỗi "dd/MM/yyyy"
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }

    /**
     * Format LocalDateTime sang chuỗi "dd/MM/yyyy HH:mm:ss"
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Parse chuỗi "dd/MM/yyyy" hoặc "yyyy-MM-dd" sang LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            if (dateStr.contains("-")) {
                return LocalDate.parse(dateStr, ISO_DATE_FORMATTER);
            }
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tính tuổi của Cư dân dựa vào ngày sinh
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}