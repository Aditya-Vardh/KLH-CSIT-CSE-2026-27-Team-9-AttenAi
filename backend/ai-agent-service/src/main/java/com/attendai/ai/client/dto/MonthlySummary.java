package com.attendai.ai.client.dto;

public record MonthlySummary(Long employeeId, int year, int month,
        long totalWorkingDays, long presentDays, long absentDays,
        long lateDays, double attendancePercentage,
        double totalWorkingHours, double averageDailyHours) {}
