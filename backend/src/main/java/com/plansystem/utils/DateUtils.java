package com.plansystem.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Date utility methods.
 */
public final class DateUtils {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("yyyy-'W'ww");

    private DateUtils() {
    }

    /**
     * Get the start of current week (Monday).
     */
    public static LocalDate getWeekStart() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Get the start of week for given date.
     */
    public static LocalDate getWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Get the end of current week (Sunday).
     */
    public static LocalDate getWeekEnd() {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * Get the end of week for given date.
     */
    public static LocalDate getWeekEnd(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * Get the start of current month.
     */
    public static LocalDate getMonthStart() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * Get the start of month for given date.
     */
    public static LocalDate getMonthStart(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    /**
     * Get the end of current month.
     */
    public static LocalDate getMonthEnd() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Get the end of month for given date.
     */
    public static LocalDate getMonthEnd(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Get current week number in year (ISO week, Monday as first day).
     */
    public static int getWeekNumber(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return date.get(weekFields.weekOfWeekBasedYear());
    }

    /**
     * Format LocalDateTime to string.
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    /**
     * Format LocalDate to string.
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    /**
     * Parse date string to LocalDate.
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * Parse datetime string to LocalDateTime.
     * Supports both ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss) and
     * space-separated format (yyyy-MM-dd HH:mm:ss).
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        String trimmed = dateTimeStr.trim();
        // Try ISO 8601 format first (frontend standard)
        if (trimmed.contains("T")) {
            // Handle optional seconds and timezone suffix
            if (trimmed.length() == 16) {
                return LocalDateTime.parse(trimmed, ISO_DATETIME_FORMATTER);
            }
            // Handle ISO format with seconds (19 chars: yyyy-MM-ddTHH:mm:ss)
            if (trimmed.length() >= 19) {
                return LocalDateTime.parse(trimmed.substring(0, 19));
            }
            return LocalDateTime.parse(trimmed);
        }
        // Fall back to space-separated format
        return LocalDateTime.parse(trimmed, DATETIME_FORMATTER);
    }

    /**
     * Generate week period label like "2026-W25".
     */
    public static String getWeekPeriod(LocalDate date) {
        return date.format(WEEK_FORMATTER);
    }

    /**
     * Generate month period label like "2026-06".
     */
    public static String getMonthPeriod(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}
