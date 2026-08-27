package com.attendai.ai.dto;

import java.util.List;

public record AnalyticsQueryResponse(
        String question,
        String insight,
        List<String> recommendations,
        List<EmployeeStat> stats
) {
    public record EmployeeStat(Long employeeId, String name, double attendancePct,
            long lateDays, long absentDays, String rating) {}
}
