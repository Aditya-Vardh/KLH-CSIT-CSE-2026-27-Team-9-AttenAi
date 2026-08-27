package com.attendai.ai.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record NlAttendanceResponse(
        String intent,           // LATE_ARRIVAL | ABSENT | PRESENT | UNKNOWN
        LocalDate date,
        LocalTime expectedArrival,
        int lateMinutes,
        String extractedNote,
        String actionTaken,      // ATTENDANCE_MARKED | NOTIFICATION_SENT | MANUAL_REVIEW_REQUIRED
        String message,
        Long attendanceId
) {}
