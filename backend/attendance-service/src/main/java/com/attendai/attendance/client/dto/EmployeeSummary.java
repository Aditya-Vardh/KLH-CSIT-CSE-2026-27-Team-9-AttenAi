package com.attendai.attendance.client.dto;

/**
 * Mirror of employee-service EmployeeSummary — kept local to avoid cross-module coupling.
 */
public record EmployeeSummary(
        Long id,
        Long userId,
        String employeeCode,
        String fullName,
        String email,
        String department,
        String designation,
        Integer annualLeaveQuota
) {}
