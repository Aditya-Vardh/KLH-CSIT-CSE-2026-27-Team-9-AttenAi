package com.attendai.ai.client.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceRecord(Long id, Long employeeId, LocalDate attendanceDate,
        LocalTime checkInTime, LocalTime checkOutTime, Double workingHours,
        String status, boolean lateArrival, int lateMinutes, String note) {}
