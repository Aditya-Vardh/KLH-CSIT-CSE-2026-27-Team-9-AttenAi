package com.attendai.attendance.dto;

import com.attendai.attendance.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * HR/Admin override — create or update an attendance record directly.
 */
public record ManualAttendanceRequest(
        @NotNull Long employeeId,
        @NotNull LocalDate attendanceDate,
        LocalTime checkInTime,
        LocalTime checkOutTime,
        @NotNull AttendanceStatus status,
        String note
) {}
