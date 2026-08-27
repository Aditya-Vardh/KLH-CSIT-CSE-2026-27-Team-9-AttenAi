package com.attendai.attendance.dto;

import com.attendai.attendance.entity.Attendance;
import com.attendai.attendance.entity.AttendanceStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceResponse(
        Long id,
        Long employeeId,
        LocalDate attendanceDate,
        LocalTime checkInTime,
        LocalTime checkOutTime,
        Double workingHours,
        AttendanceStatus status,
        LocalTime expectedCheckIn,
        boolean lateArrival,
        int lateMinutes,
        String note,
        boolean aiGenerated,
        Instant createdAt,
        Instant updatedAt
) {
    public static AttendanceResponse from(Attendance a) {
        return new AttendanceResponse(
                a.getId(), a.getEmployeeId(), a.getAttendanceDate(),
                a.getCheckInTime(), a.getCheckOutTime(), a.getWorkingHours(),
                a.getStatus(), a.getExpectedCheckIn(), a.isLateArrival(),
                a.getLateMinutes(), a.getNote(), a.isAiGenerated(),
                a.getCreatedAt(), a.getUpdatedAt());
    }
}
