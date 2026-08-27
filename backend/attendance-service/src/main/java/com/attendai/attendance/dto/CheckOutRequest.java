package com.attendai.attendance.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record CheckOutRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        /** If null, uses LocalTime.now(). */
        LocalTime checkOutTime,

        String note
) {}
