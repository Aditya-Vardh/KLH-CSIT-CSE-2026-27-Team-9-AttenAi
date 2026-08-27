package com.attendai.leave.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record LeaveRequestDto(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Leave type is required")
        Long leaveTypeId,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @Size(max = 1000, message = "Reason must be at most 1000 characters")
        String reason,

        @Size(max = 500)
        String documentUrl
) {}
