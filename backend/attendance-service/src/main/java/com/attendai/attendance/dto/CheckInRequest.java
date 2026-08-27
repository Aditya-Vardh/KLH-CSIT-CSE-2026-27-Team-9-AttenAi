package com.attendai.attendance.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CheckInRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        /** If null, uses LocalDate.now(). Allows backdating by HR. */
        LocalDate attendanceDate,

        /** If null, uses LocalTime.now(). */
        LocalTime checkInTime,

        String note
) {}
