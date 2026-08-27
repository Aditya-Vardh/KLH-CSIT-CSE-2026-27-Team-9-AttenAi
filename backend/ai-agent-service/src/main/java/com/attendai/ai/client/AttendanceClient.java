package com.attendai.ai.client;

import com.attendai.ai.client.dto.AttendanceRecord;
import com.attendai.ai.client.dto.MonthlySummary;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "attendance-service", path = "/api/attendance")
public interface AttendanceClient {

    @GetMapping("/monthly/{employeeId}")
    MonthlySummary getMonthlySummary(@PathVariable Long employeeId,
            @RequestParam int year, @RequestParam int month,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role);

    @GetMapping("/late-arrivals")
    List<AttendanceRecord> getLateArrivals(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role);

    @PutMapping("/manual")
    AttendanceRecord markAiAttendance(@RequestBody Object request,
            @RequestHeader("X-Auth-User-Email") String email,
            @RequestHeader("X-Auth-User-Role") String role);
}
