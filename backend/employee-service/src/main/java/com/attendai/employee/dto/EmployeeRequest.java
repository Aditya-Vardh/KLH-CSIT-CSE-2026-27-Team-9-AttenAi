package com.attendai.employee.dto;

import com.attendai.employee.entity.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EmployeeRequest(
        Long userId,

        @NotBlank(message = "Employee code is required")
        @Size(max = 20, message = "Employee code must be at most 20 characters")
        @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "Employee code must contain only uppercase letters, digits and hyphens")
        String employeeCode,

        @NotBlank(message = "First name is required")
        @Size(max = 80, message = "First name must be at most 80 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 80, message = "Last name must be at most 80 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @Pattern(regexp = "^[+0-9\\-\\s()]{7,20}$", message = "Phone number is invalid")
        String phone,

        @Size(max = 500, message = "Address must be at most 500 characters")
        String address,

        LocalDate dateOfBirth,

        @NotNull(message = "Joining date is required")
        LocalDate joiningDate,

        EmployeeStatus status,

        Long departmentId,

        Long designationId,

        @Size(max = 500)
        String profilePicture,

        Integer annualLeaveQuota
) {}
