package com.attendai.leave.client.dto;

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
