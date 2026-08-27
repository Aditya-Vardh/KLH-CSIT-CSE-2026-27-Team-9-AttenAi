package com.attendai.attendance.dto;

/**
 * Monthly attendance summary for a single employee.
 */
public record MonthlySummary(
        Long employeeId,
        int year,
        int month,
        long totalWorkingDays,
        long presentDays,
        long absentDays,
        long lateDays,
        double attendancePercentage,
        double totalWorkingHours,
        double averageDailyHours
) {}
