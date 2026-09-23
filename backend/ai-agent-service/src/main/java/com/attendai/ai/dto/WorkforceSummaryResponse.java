package com.attendai.ai.dto;

import java.util.List;

public record WorkforceSummaryResponse(
        String fromDate,
        String toDate,
        int totalEmployees,
        int employeesCovered,
        double averageAttendancePct,
        long presentDays,
        long absentDays,
        long lateDays,
        double totalWorkingHours,
        double previousAttendancePct,
        double attendanceChangePctPoints,
        List<Anomaly> anomalies,
        List<EmployeeInsight> employees,
        List<DepartmentInsight> departments) {

    public record Anomaly(Long employeeId, String employeeName, String department,
                          String type, double metric, double threshold,
                          String period, String severity) {}

    public record EmployeeInsight(Long employeeId, String employeeName, String department,
                                  double attendancePct, long presentDays, long absentDays,
                                  long lateDays, double workingHours, double previousAttendancePct,
                                  double changePctPoints, String status) {}

    public record DepartmentInsight(String department, int employeeCount,
                                    double averageAttendancePct, long lateDays,
                                    long absentDays, double attendanceChangePctPoints,
                                    int anomalyCount) {}
}