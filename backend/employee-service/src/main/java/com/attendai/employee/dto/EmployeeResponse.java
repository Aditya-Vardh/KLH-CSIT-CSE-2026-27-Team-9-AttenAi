package com.attendai.employee.dto;

import com.attendai.employee.entity.Employee;
import com.attendai.employee.entity.EmployeeStatus;
import java.time.Instant;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        Long userId,
        String employeeCode,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String address,
        LocalDate dateOfBirth,
        LocalDate joiningDate,
        LocalDate leavingDate,
        EmployeeStatus status,
        DepartmentSummary department,
        DesignationSummary designation,
        String profilePicture,
        Integer annualLeaveQuota,
        Instant createdAt,
        Instant updatedAt
) {
    public record DepartmentSummary(Long id, String name) {}
    public record DesignationSummary(Long id, String title, String grade) {}

    public static EmployeeResponse from(Employee e) {
        DepartmentSummary dept = e.getDepartment() == null ? null
                : new DepartmentSummary(e.getDepartment().getId(), e.getDepartment().getName());
        DesignationSummary desig = e.getDesignation() == null ? null
                : new DesignationSummary(e.getDesignation().getId(), e.getDesignation().getTitle(), e.getDesignation().getGrade());
        return new EmployeeResponse(
                e.getId(), e.getUserId(), e.getEmployeeCode(),
                e.getFirstName(), e.getLastName(),
                e.getFirstName() + " " + e.getLastName(),
                e.getEmail(), e.getPhone(), e.getAddress(),
                e.getDateOfBirth(), e.getJoiningDate(), e.getLeavingDate(),
                e.getStatus(), dept, desig,
                e.getProfilePicture(), e.getAnnualLeaveQuota(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
