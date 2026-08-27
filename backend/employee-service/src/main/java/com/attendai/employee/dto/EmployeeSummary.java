package com.attendai.employee.dto;

/**
 * Lightweight projection used by other microservices (Attendance, Leave)
 * that call the employee-service via internal endpoints.
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
