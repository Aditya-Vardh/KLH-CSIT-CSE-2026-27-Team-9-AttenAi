package com.attendai.attendance.dto;

import java.util.List;
import java.util.Map;

/**
 * Aggregated attendance analytics returned to the frontend / AI agent.
 */
public record AnalyticsResponse(
        String period,                            // e.g. "2026-08"
        long totalEmployees,
        double averageAttendancePercentage,
        long totalPresentDays,
        long totalAbsentDays,
        long totalLateDays,
        double totalWorkingHours,

        /** Per-day breakdown: date → (present count, absent count, late count). */
        List<DailyBreakdown> dailyBreakdown,

        /** Per-employee summary for the period. */
        List<EmployeeAttendanceStat> employeeStats
) {
    public record DailyBreakdown(
            String date,
            long present,
            long absent,
            long late
    ) {}

    public record EmployeeAttendanceStat(
            Long employeeId,
            String employeeName,
            long presentDays,
            long lateDays,
            double attendancePercentage,
            String rating              // EXCELLENT / GOOD / AVERAGE / POOR
    ) {}
}
